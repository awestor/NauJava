package ru.daniil.NauJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.entity.ReportStatus;
import ru.daniil.NauJava.request.ReportCreationResponse;
import ru.daniil.NauJava.service.ReportService;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Отображает страницу с отчетами
     */
    @GetMapping
    public String showReportsPage(Model model) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        return "reports";
    }

    /**
     * Создает новый отчет и запускает его формирование
     * @return ID созданного отчета
     */
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<ReportCreationResponse> generateReport() {
        try {
            Long reportId = reportService.createReport();
            reportService.generateReportAsync(reportId);

            ReportCreationResponse response = new ReportCreationResponse(
                    reportId,
                    "success",
                    "Report generation started"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ReportCreationResponse response = new ReportCreationResponse(
                    null,
                    "error",
                    "Failed to create report: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Получает статус отчета
     * @param reportId ID отчёта
     * @return название статуса
     */
    @GetMapping("/{reportId}/status")
    @ResponseBody
    public ResponseEntity<String> getReportStatus(@PathVariable Long reportId) {
        ReportStatus status = reportService.getReportStatus(reportId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status.name());
    }

    /**
     * Получает содержимое отчета
     * @param reportId ID отчёта
     * @return содержимое отчёта
     */
    @GetMapping("/{reportId}/content")
    @ResponseBody
    public ResponseEntity<String> getReportContent(@PathVariable Long reportId) {
        String content = reportService.getReportContent(reportId);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(content);
    }

    /**
     * Отображает страницу просмотра отчета
     * @param reportId ID отчёта
     * @param model название шаблона для отображения
     * @return страница с данными
     */
    @GetMapping("/{reportId}/view")
    public String viewReport(@PathVariable Long reportId, Model model) {
        ReportStatus status = reportService.getReportStatus(reportId);
        String content = reportService.getReportContent(reportId);

        model.addAttribute("reportId", reportId);
        model.addAttribute("status", status);
        model.addAttribute("content", content);

        return "report-view";
    }
}