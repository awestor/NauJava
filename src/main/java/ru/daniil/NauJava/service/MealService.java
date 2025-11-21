package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Meal;

import java.util.List;

public interface MealService {
    /**
     * Создает новый прием пищи с указанными продуктами для пользователя.
     * Выполняется в рамках единой транзакции.
     */
    Meal createMealWithProducts(String userEmail,
                                String mealTypeName,
                                List<String> productNames,
                                List<Integer> quantities);

    /**
     * Получает все приемы пищи для пользователя за сегодня
     * @param userEmail электронная почта пользователя
     * @return список приёмов пищи
     */
    List<Meal> getTodayMeals(String userEmail);
}
