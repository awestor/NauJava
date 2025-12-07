package ru.daniil.NauJava.service.admin;

import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.response.ReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportService {
    Long createReport(LocalDate startDate, LocalDate endDate);

    boolean reportExistsForPeriod(LocalDate startDate, LocalDate endDate);

    String getReportContent(Long reportId);

    ReportStatus getReportStatus(Long reportId);

    List<ReportResponse> getPaginateReports(Integer page, Integer size);

    Optional<Report> getLatestReport();

    Optional<Report> getReportById(Long Id);

    Long countReports();

    void generateReportAsync(Long reportId);
}
