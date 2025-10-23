package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealRepository extends CrudRepository<Meal, Long> {
    List<Meal> findByDailyReportId(Long dailyReportId);

    List<Meal> findByDailyReportUserIdAndMealType(Long userId, String mealType);

    List<Meal> findByDailyReportUserIdAndDailyReportReportDate(Long userId, LocalDate reportDate);

    @Query("SELECT me.product.id FROM MealEntry me WHERE me.id = :mealEntryId")
    List<Long> findProductIdByMealEntryId(@Param("mealEntryId") Long mealEntryId);

    boolean existsByDailyReportIdAndMealType(Long dailyReportId, String mealType);

    long countByDailyReportIdAndMealType(Long dailyReportId, String mealType);
}
