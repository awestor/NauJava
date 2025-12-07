package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RepositoryRestResource(path = "meals")
public interface MealRepository extends CrudRepository<Meal, Long> {
    List<Meal> findByDailyReportId(Long dailyReportId);

    List<Meal> findByDailyReportUserIdAndMealType(Long userId, MealType mealType);

    List<Meal> findByDailyReportUserIdAndDailyReportReportDate(Long userId, LocalDate reportDate);

    @Query("SELECT me.product.id FROM MealEntry me WHERE me.id = :mealEntryId")
    List<Long> findProductIdByMealEntryId(@Param("mealEntryId") Long mealEntryId);

    boolean existsByDailyReportIdAndMealType(Long dailyReportId, MealType mealType);

    long countByDailyReportIdAndMealType(Long dailyReportId, MealType mealType);

    @Query("SELECT MAX(m.eatenAt) FROM Meal m WHERE m.dailyReport.user.id = :userId")
    LocalDateTime findLastMealActivityByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT m.dailyReport.user.id) FROM Meal m WHERE m.eatenAt >= :after")
    Long countUsersWithActivityAfter(@Param("after") LocalDateTime after);

    @Query("SELECT DISTINCT dr.user.id FROM Meal m JOIN m.dailyReport dr " +
            "WHERE m.eatenAt BETWEEN :start AND :end")
    List<Long> findDistinctUserIdsWithMealsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(m) FROM Meal m JOIN m.dailyReport dr " +
            "WHERE dr.user.id IN :userIds AND m.eatenAt BETWEEN :start AND :end")
    Long countMealsForUsersBetweenDates(
            @Param("userIds") List<Long> userIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
