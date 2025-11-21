package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.openqa.selenium.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.repository.MealRepository;
import ru.daniil.NauJava.repository.MealTypeRepository;
import ru.daniil.NauJava.request.NutritionSumResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealServiceImpl implements MealService {
    private static final Logger logger = LoggerFactory.getLogger(MealServiceImpl.class);

    private final MealRepository mealRepository;
    private final MealEntityService mealEntryService;
    private final MealTypeRepository mealTypeRepository;
    private final UserService userService;
    private final DailyReportService dailyReportService;
    private final PlatformTransactionManager transactionManager;

    public MealServiceImpl(MealRepository mealRepository,
                           MealEntityService mealEntryService,
                           MealTypeRepository mealTypeRepository,
                           UserService userService,
                           DailyReportService dailyReportService,
                           PlatformTransactionManager transactionManager) {
        this.mealRepository = mealRepository;
        this.mealEntryService = mealEntryService;
        this.mealTypeRepository = mealTypeRepository;
        this.userService = userService;
        this.dailyReportService = dailyReportService;
        this.transactionManager = transactionManager;
    }

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
    @Override
    public Meal createMealWithProducts(String userEmail, String mealTypeName,
                                       List<String> productNames, List<Integer> quantities) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        LocalDate today = LocalDate.now();

        try {
            User user = userService.findUserByEmail(userEmail).orElse(null);
            if (user == null) {
                logger.error("Пользователь с email '{}' не найден. Транзакция откатывается.", userEmail);
                transactionManager.rollback(status);
                throw new IllegalArgumentException("Пользователь не найден: " + userEmail);
            }

            logger.info("Начало создания приема пищи для пользователя: {}, тип: {}", user.getLogin(), mealTypeName);

            DailyReport dailyReport = dailyReportService.getOrCreateDailyReport(user, today);

            MealType mealType = mealTypeRepository.findByName(mealTypeName)
                    .orElseThrow(() -> new NotFoundException("Указанный тип приёма пищи не найден в системе"));
            Meal meal = createMeal(dailyReport, mealType);
            logger.debug("Создан прием пищи ID: {}", meal.getId());

            List<MealEntry> mealEntries = mealEntryService.createMealEntries(meal, productNames, quantities);
            for (MealEntry mealEntry : mealEntries) {
                meal.addMealEntry(mealEntry);
            }

            dailyReportService.recalculateDailyReportTotals(dailyReport.getId());

            transactionManager.commit(status);

            logger.info("Успешно создан прием пищи ID: {} с {} продуктами для пользователя: {}",
                    meal.getId(), mealEntries.size(), user.getLogin());

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
    private Meal createMeal(DailyReport dailyReport, MealType mealType) {
        Meal meal = new Meal(dailyReport, mealType);
        return mealRepository.save(meal);
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

    @Transactional
    public NutritionSumResponse getNutritionSum(Long mealId) {
        return mealEntryService.getNutritionSumByMealId(mealId);
    }
}
