package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Meal;

import java.util.List;
import java.util.Optional;

public interface MealService {
    /**
     * Создает новый прием пищи с указанными продуктами для пользователя.
     * Выполняется в рамках единой транзакции.
     */
    Meal createMealWithProducts(String mealTypeName,
                                List<String> productNames,
                                List<Integer> quantities);

    /**
     * Обновляет запись о приёме пищи
     * @param mealId id приёма пищи
     * @param mealTypeName тип приёма пищи
     * @param productNames названия потреблённых продуктов
     * @param quantities потреблённый вес
     * @return обновлённая сущность
     */
    Meal updateMealWithProducts(Long mealId, String mealTypeName,
                                List<String> productNames,
                                List<Integer> quantities);

    /**
     * Получает приём пищи по id
     * @param mealId id приёма пищи
     * @return приём пищи или null
     */
    Optional<Meal> getMealById(Long mealId);

    /**
     * Получает все приемы пищи для пользователя за сегодня
     * @param userEmail электронная почта пользователя
     * @return список приёмов пищи
     */
    List<Meal> getTodayMeals(String userEmail);

    /**
     * Получает все приёмы пищи по id дневного отчёта
     * @param dailyReportId id дневного отчёта
     * @return список найденных приёмов пищи
     */
    List<Meal> getByDailyReportId(Long dailyReportId);

    /**
     * Удаляет из БД приём пищи
     * @param mealId id приёма пищи
     */
    void deleteCurrentMeal(Long mealId);

    /**
     * Подсчитывает и обновляет суммарные питательные ценности всех приёмов пищи
     * имеющие ссылку на указанный дневной отчёт
     * @param dailyReportId id дневного отчёта
     */
    void updateNutritionSum(Long dailyReportId);
}
