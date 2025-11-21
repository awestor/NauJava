package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.MealEntry;

import java.util.List;
import java.util.Map;

@Repository
@RepositoryRestResource(path = "mealEntries")
public interface MealEntryRepository extends CrudRepository<MealEntry, Long> {
    List<MealEntry> findByMealId(Long mealId);

    List<MealEntry> findByProductId(Long productId);

    long countByMealId(Long mealId);

    /**
     * Метод получения суммы полей калорийности, белков, жиров, углеводов по mealId
     * @return список объектов, где:
     * [0] - totalCalories (Double)
     * [1] - totalProteins (Double)
     * [2] - totalFats (Double)
     * [3] - totalCarbs (Double)
     */
    @Query("SELECT " +
            "COALESCE(SUM(me.calculatedCalories), 0), " +
            "COALESCE(SUM(me.calculatedProteins), 0), " +
            "COALESCE(SUM(me.calculatedFats), 0), " +
            "COALESCE(SUM(me.calculatedCarbs), 0) " +
            "FROM MealEntry me " +
            "WHERE me.meal.id = :mealId")
    List<Double> findNutritionSumByMealId(@Param("mealId") Long mealId);

    /**
     * Метод получения суммы полей калорийности, белков, жиров, углеводов по dailyReportId
     * @return список объектов, где:
     * [0] - totalCalories (Double)
     * [1] - totalProteins (Double)
     * [2] - totalFats (Double)
     * [3] - totalCarbs (Double)
     */
    @Query("SELECT " +
            "COALESCE(SUM(me.calculatedCalories), 0), " +
            "COALESCE(SUM(me.calculatedProteins), 0), " +
            "COALESCE(SUM(me.calculatedFats), 0), " +
            "COALESCE(SUM(me.calculatedCarbs), 0) " +
            "FROM MealEntry me " +
            "WHERE me.meal.dailyReport.id = :dailyReportId")
    List<Double> findNutritionSumByDailyReportId(@Param("dailyReportId") Long dailyReportId);
}
