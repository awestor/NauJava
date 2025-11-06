package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.repository.MealRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealServiceImpl implements MealService {
    private static final Logger logger = LoggerFactory.getLogger(MealServiceImpl.class);

    private final MealRepository mealRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserService userService;
    private final DailyReportService dailyReportService;
    private final ProductService productService;
    private final PlatformTransactionManager transactionManager;

    public MealServiceImpl(MealRepository mealRepository,
                           MealEntryRepository mealEntryRepository,
                           UserService userService,
                           DailyReportService dailyReportService,
                           ProductService productService,
                           PlatformTransactionManager transactionManager) {
        this.mealRepository = mealRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.userService = userService;
        this.dailyReportService = dailyReportService;
        this.productService = productService;
        this.transactionManager = transactionManager;
    }

    @Override
    public Meal createMealWithProducts(String userEmail, String mealType,
                                       List<String> productNames, List<Integer> quantities) {
        /*
        Данный метод обёрнут в транзакционную обёртку
        Список действий в классе:
        1. Проверка на существование пользователя
        2. Получение или создание DailyReport для сегодняшней даты
        3. Создание приема пищи
        4. Создание MealEntry для каждого продукта
        5. Пересчёт "totals" для DailyReport
        6. Коммит транзакции если все успешно
         */
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            User user = userService.findUserByEmail(userEmail).orElse(null);
            if (user == null) {
                logger.error("Пользователь с email '{}' не найден. Транзакция откатывается.", userEmail);
                transactionManager.rollback(status);
                throw new IllegalArgumentException("Пользователь не найден: " + userEmail);
            }

            logger.info("Начало создания приема пищи для пользователя: {}, тип: {}", user.getName(), mealType);

            LocalDate today = LocalDate.now();
            DailyReport dailyReport = dailyReportService.getOrCreateDailyReport(user, today);

            Meal meal = createMeal(dailyReport, mealType);
            logger.debug("Создан прием пищи ID: {}", meal.getId());

            List<MealEntry> mealEntries = createMealEntries(meal, productNames, quantities);
            for (MealEntry mealEntry : mealEntries) {
                meal.addMealEntry(mealEntry);
            }

            dailyReportService.recalculateDailyReportTotals(dailyReport.getId());

            transactionManager.commit(status);

            logger.info("Успешно создан прием пищи ID: {} с {} продуктами для пользователя: {}",
                    meal.getId(), mealEntries.size(), user.getName());

            return meal;

        } catch (Exception ex) {
            if (!status.isCompleted()) {
                logger.error("Откат транзакции для пользователя с email: {}. Причина: {}",
                        userEmail, ex.getMessage());
                transactionManager.rollback(status);
            } else {
                logger.error("Транзакция уже завершена для пользователя с email: {}. Причина: {}",
                        userEmail, ex.getMessage());
            }
            throw ex;
        }
    }

    /**
     * Создает приём пищи "Meal"
     * @param dailyReport отчёт по приёмам пищи сегодня
     * @param mealType тип приёма пищи
     * @return сохранённую сущность приёма пищи
     */
    private Meal createMeal(DailyReport dailyReport, String mealType) {
        Meal meal = new Meal(dailyReport, mealType);
        return mealRepository.save(meal);
    }

    /**
     * Создает "MealEntry" для каждого продукта
     * @param meal приём пищи
     * @param productNames название продукта
     * @param quantities вес съеденных продуктов
     * @return список записей о съеденных продуктах
     */
    private List<MealEntry> createMealEntries(Meal meal, List<String> productNames, List<Integer> quantities) {
        if (productNames.size() != quantities.size()) {
            throw new IllegalArgumentException("Количество продуктов и количеств не совпадает");
        }

        List<MealEntry> mealEntries = new ArrayList<>();

        for (int i = 0; i < productNames.size(); i++) {
            String productName = productNames.get(i);
            Integer quantity = quantities.get(i);

            // Ищем продукт через ProductService
            Product product = productService.findProductByName(productName);
            if (product == null) {
                throw new IllegalArgumentException("Продукт не найден: " + productName);
            }

            MealEntry mealEntry = createMealEntry(meal, product, quantity);
            mealEntries.add(mealEntry);

            logger.debug("Создан MealEntry для продукта: {}, количество: {}г", productName, quantity);
        }

        return mealEntries;
    }

    /**
     * Создает один "MealEntry"
     * @param meal приём пищи
     * @param product продукт питания
     * @param quantity количество
     * @return сохранённую сущность
     */
    private MealEntry createMealEntry(Meal meal, Product product, Integer quantity) {
        MealEntry mealEntry = new MealEntry(meal, product, quantity);

        return mealEntryRepository.save(mealEntry);
    }

    @Transactional
    @Override
    public List<Meal> getTodayMeals(String userEmail) {
        User user = userService.findUserByEmail(userEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        return mealRepository.findByDailyReportUserIdAndDailyReportReportDate(user.getId(), today);
    }
}
