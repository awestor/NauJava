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
import ru.daniil.NauJava.repository.MealRepository;
import ru.daniil.NauJava.repository.MealTypeRepository;
import ru.daniil.NauJava.request.NutritionSumResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MealServiceImpl implements MealService {
    private static final Logger logger = LoggerFactory.getLogger(MealServiceImpl.class);

    private final MealRepository mealRepository;
    private final MealEntryService mealEntryService;
    private final MealTypeRepository mealTypeRepository;
    private final UserService userService;
    private final DailyReportService dailyReportService;
    private final PlatformTransactionManager transactionManager;

    public MealServiceImpl(MealRepository mealRepository,
                           MealEntryService mealEntryService,
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

    @Override
    public Meal createMealWithProducts(String mealTypeName,
                                       List<String> productNames, List<Integer> quantities) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        LocalDate today = LocalDate.now();

        try {
            logger.info("Начало создания приема пищи для пользователя, тип: {}",  mealTypeName);

            DailyReport dailyReport = dailyReportService.getOrCreateDailyReportAuth(today);

            MealType mealType = mealTypeRepository.findByName(mealTypeName)
                    .orElseThrow(() -> new NotFoundException("Указанный тип приёма пищи не найден в системе"));
            Meal meal = createMeal(dailyReport, mealType);
            logger.debug("Создан прием пищи ID: {}", meal.getId());

            Map<String, Integer> productQuantities = new HashMap<>();
            for (int i = 0; i < productNames.size(); i++) {
                String productName = productNames.get(i);
                Integer quantity = quantities.get(i);
                productQuantities.merge(productName, quantity, Integer::sum);
            }

            List<String> uniqueProductNames = new ArrayList<>(productQuantities.keySet());
            List<Integer> summedQuantities = new ArrayList<>(productQuantities.values());

            List<MealEntry> mealEntries = mealEntryService.createMealEntries(meal, uniqueProductNames, summedQuantities);
            for (MealEntry mealEntry : mealEntries) {
                meal.addMealEntry(mealEntry);
            }

            transactionManager.commit(status);

            logger.info("Успешно создан прием пищи ID: {} с {} продуктами для пользователя",
                    meal.getId(), mealEntries.size());

            return meal;

        } catch (Exception ex) {
            if (!status.isCompleted()) {
                logger.error("Откат транзакции для пользователя. Причина: {}", ex.getMessage());
                transactionManager.rollback(status);
            } else {
                logger.error("Транзакция уже завершена для пользователя. Причина: {}", ex.getMessage());
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
        User user = userService.getAuthUser().orElse(null);
        if (user == null) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        return mealRepository.findByDailyReportUserIdAndDailyReportReportDate(user.getId(), today);
    }

    @Transactional
    public List<Meal> getByDailyReportId(Long dailyReportId){
        return mealRepository.findByDailyReportId(dailyReportId);
    }

    @Override
    public void deleteCurrentMeal(Long mealId) {
        Meal meal = mealRepository.findById(mealId).orElseThrow();
        mealRepository.deleteById(mealId);
        updateNutritionSum(meal.getDailyReport().getId());
    }

    @Transactional
    public NutritionSumResponse getNutritionSum(Long mealId) {
        return mealEntryService.getNutritionSumByMealId(mealId);
    }

    @Transactional
    @Override
    public void updateNutritionSum(Long dailyReportId) {
        DailyReport dailyReport = dailyReportService.getOrCreateDailyReportById(dailyReportId);
        dailyReportService.recalculateDailyReportTotals(dailyReport);
    }

    @Transactional
    @Override
    public Meal updateMealWithProducts(Long mealId, String mealTypeName,
                                       List<String> productNames, List<Integer> quantities) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        MealType mealType = mealTypeRepository.findByName(mealTypeName)
                .orElseThrow(() -> new RuntimeException("Meal type not found"));
        meal.setMealType(mealType);

        mealEntryService.deleteByMealId(mealId);
        meal.getMealEntries().clear();

        mealEntryService.createMealEntries(meal, productNames, quantities);
        return mealRepository.save(meal);
    }

    @Transactional
    @Override
    public Optional<Meal> getMealById(Long mealId){
        return mealRepository.findById(mealId);
    }

    /**
     * Получение последней активности пользователя по приёмам пищи
     */
    public LocalDateTime getLastMealActivityByUserId(Long userId) {
        return mealRepository.findLastMealActivityByUserId(userId);
    }

    /**
     * Подсчет пользователей с активностью после указанной даты
     */
    public Long countUsersWithActivityAfter(LocalDateTime after) {
        return mealRepository.countUsersWithActivityAfter(after);
    }

    /**
     * Получение приёмов пищи по отчёту
     */
    public List<Meal> getMealsByDailyReportId(Long dailyReportId) {
        return mealRepository.findByDailyReportId(dailyReportId);
    }
}
