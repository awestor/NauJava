package ru.daniil.NauJava.controller.report;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.request.ReportCreationResponse;
import ru.daniil.NauJava.service.ReportService;

@Controller
@RequestMapping("/admin/api/reports")
public class ReportApiController {

    private final ReportService reportService;

    public ReportApiController(ReportService reportService) {
        this.reportService = reportService;
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
}