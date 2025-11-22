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

    Meal updateMealWithProducts(Long mealId, String mealTypeName,
                                List<String> productNames,
                                List<Integer> quantities);

    Optional<Meal> getMealById(Long mealId);
    /**
     * Получает все приемы пищи для пользователя за сегодня
     * @param userEmail электронная почта пользователя
     * @return список приёмов пищи
     */
    List<Meal> getTodayMeals(String userEmail);

    List<Meal> getByDailyReportId(Long dailyReportId);

    void deleteCurrentMeal(Long mealId);

    void updateNutritionSum(Long dailyReportId);
}
