package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MealService {
    /**
     * Считает пользователей, что потребляли пищу после указанной даты
     * @param after дата, после которой будет идти подсчёт
     * @return число пользователей
     */
    Long countUsersWithActivityAfter(LocalDateTime after);

    /**
     * Получает дату последнего потребления пищи по id пользователя
     * @param userId id пользователя
     * @return дата последнего приёма пищи
     */
    LocalDateTime getLastMealActivityByUserId(Long userId);

    /**
     * Создает новый прием пищи с указанными продуктами для пользователя.
     * Выполняется в рамках единой транзакции.
     * @param mealTypeName название типа приёма пищи
     * @param productNames список названий продуктов
     * @param quantities список весов потреблённой продукции
     * @return созданный приём пищи
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
     * Получает все приёмы пищи по дате
     * @param date дата приёма пищи
     * @return список приёмов пищи
     */
    List<Meal> getMealsForDate(LocalDate date);

    List<Long> findDistinctUserIdsWithMealsBetween(LocalDateTime start, LocalDateTime end);
    Long countMealsForUsersBetweenDates(List<Long> activeUserIds, LocalDateTime start, LocalDateTime end);

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
