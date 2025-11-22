package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.request.NutritionSumResponse;

import java.util.List;

public interface MealEntryService {
    /**
     * Создает "MealEntry" для каждого продукта
     * @param meal приём пищи
     * @param productNames название продукта
     * @param quantities вес съеденных продуктов (в граммах)
     * @return список записей о съеденных продуктах
     */
    List<MealEntry> createMealEntries(Meal meal, List<String> productNames, List<Integer> quantities);

    /**
     * Создает один "MealEntry"
     * @param meal приём пищи
     * @param product продукт питания
     * @param quantity количество
     * @return сохранённую сущность
     */
    MealEntry createMealEntry(Meal meal, Product product, Integer quantity);

    NutritionSumResponse getNutritionSumByMealId(Long mealId);

    NutritionSumResponse getNutritionSumByDailyReportId(Long dailyReportId);

    List<MealEntry> getAllByMealId(Long mealId);

    void deleteByMealId(Long mealId);
}
