package ru.daniil.NauJava.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends CrudRepository<Report, Long> {

    /**
     * Находит все отчеты, отсортированные по дате создания (новые сначала)
     */
    List<Report> findAllByOrderByCreatedAtDesc();

    Optional<Report> findTopByOrderByCreatedAtDesc();

    boolean existsByReportPeriodStartAndReportPeriodEnd(LocalDate startDate, LocalDate endDate);

    Optional<Report> findByReportPeriodStartAndReportPeriodEnd(LocalDate startDate, LocalDate endDate);

    /**
     * Получает все отчёты с ограничением 48 записей
     */
    @Query("SELECT r FROM Report r ORDER BY r.createdAt DESC LIMIT 48")
    List<Report> findAllLimited();

    /**
     * Пользовательская реализация с пагинацией
     */
    @Query("SELECT r FROM Report r ORDER BY r.createdAt DESC")
    Page<Report> findAllWithPagination(Pageable pageable);
}