package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.DailyReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends CrudRepository<DailyReport, Long> {
    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);

    @Query("SELECT dr FROM DailyReport dr WHERE dr.user.id = :userId " +
            "AND dr.totalCaloriesConsumed >= :minCalories " +
            "AND dr.totalCaloriesConsumed <= :maxCalories")
    List<DailyReport> findDailyReportsByCaloriesRange(
            @Param("userId") Long userId,
            @Param("minCalories") Double minCalories,
            @Param("maxCalories") Double maxCalories);

    boolean existsByUserIdAndReportDate(Long userId, LocalDate reportDate);

    long countByUserIdAndIsGoalAchievedTrue(Long userId);

    long countByUserIdAndIsGoalAchievedFalse(Long userId);

    /**
     * Находит все DailyReport для пользователя за указанный период
     * @param userId ID пользователя
     * @param startDate начальная дата (включительно)
     * @param endDate конечная дата (включительно)
     * @return список отчетов за период
     */
    @Query("SELECT dr FROM DailyReport dr " +
            "WHERE dr.user.id = :userId " +
            "AND dr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY dr.reportDate")
    List<DailyReport> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<DailyReport> findByUserIdAndReportDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate);
}
