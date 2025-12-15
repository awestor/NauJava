package ru.daniil.NauJava.service.admin;

import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.response.ReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportService {
    /**
     * Создает новый отчет за указанный промежуток времени
     * @param startDate начало промежутка
     * @param endDate конец промежутка
     * @return ID созданного отчета
     */
    Long createReport(LocalDate startDate, LocalDate endDate);

    /**
     * Проверяет, существует ли отчет за указанный промежуток времени
     * @param startDate начало промежутка
     * @param endDate начало промежутка
     * @return true или false
     */
    boolean reportExistsForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Получает содержимое отчета по ID
     * @param reportId ID отчета
     * @return содержимое отчета или null если отчет не найден
     */
    String getReportContent(Long reportId);

    /**
     * Получает статус отчета по ID
     * @param reportId ID отчета
     * @return статус отчета или null если отчет не найден
     */
    ReportStatus getReportStatus(Long reportId);

    /**
     * Странично получает отчёты
     * @param page страница
     * @param size размер выборки
     * @return список отчётов обёрнутых в ReportResponse
     */
    List<ReportResponse> getPaginateReports(Integer page, Integer size);

    /**
     * Получает последний созданный отчет
     * @return последний созданный отчёт
     */
    Optional<Report> getLatestReport();

    /**
     * Возвращает отчёт по его id
     * @param Id идентификатор отчёта
     * @return отчёт или null
     */
    Optional<Report> getReportById(Long Id);

    /**
     * Считает количество отчётов, что были созданы в системе
     * @return количество отчётов
     */
    Long countReports();

    /**
     * Асинхронно формирует отчет с использованием нескольких потоков
     * @param reportId id отчёта
     */
    void generateReportAsync(Long reportId);
}
