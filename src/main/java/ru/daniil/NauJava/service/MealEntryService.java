package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.response.NutritionSumResponse;

import java.util.List;

public interface MealEntryService {
    /**
     * Создает один "MealEntry"
     * @param meal приём пищи
     * @param product продукт питания
     * @param quantity количество
     * @return сохранённую сущность
     */
    MealEntry createMealEntry(Meal meal, Product product, Integer quantity);

    /**
     * Создает "MealEntry" для каждого продукта
     * @param meal приём пищи
     * @param productNames название продукта
     * @param quantities вес съеденных продуктов (в граммах)
     * @return список записей о съеденных продуктах
     */
    List<MealEntry> createMealEntries(Meal meal, List<String> productNames, List<Integer> quantities);

    /**
     * Возвращает все записи о съеденных продуктах в течении 1 приёма пищи
     * @param mealId id приёма пищи
     * @return список MealEntry
     */
    List<MealEntry> getAllByMealId(Long mealId);

    /**
     * Возвращает суммарные питательные ценности в пределах 1 приёма пищи
     * @param mealId id приёма пищи
     * @return NutritionSumResponse с данными о суммарном потреблении
     */
    NutritionSumResponse getNutritionSumByMealId(Long mealId);

    /**
     * Возвращает суммарные питательные ценности в пределах 1 дня
     * @param dailyReportId id приёма пищи
     * @return NutritionSumResponse с данными о суммарном потреблении
     */
    NutritionSumResponse getNutritionSumByDailyReportId(Long dailyReportId);

    /**
     * Удаляет приём пищи по его id
     * @param mealId id приёма пищи
     */
    void deleteByMealId(Long mealId);
}
