package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.entity.ReportStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends CrudRepository<Report, Long> {

    /**
     * Находит все отчеты, отсортированные по дате создания (новые сначала)
     */
    List<Report> findAllByOrderByCreatedAtDesc();

    /**
     * Находит все отчеты с указанным статусом
     */
    List<Report> findByStatus(ReportStatus status);
}