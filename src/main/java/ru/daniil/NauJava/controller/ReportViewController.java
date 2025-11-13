package ru.daniil.NauJava.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.entity.ReportStatus;
import ru.daniil.NauJava.service.ReportService;

import java.util.List;

@Controller
@RequestMapping("/view/reports")
public class ReportViewController {
    private final ReportService reportService;

    public ReportViewController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Отображает базовую страницу с отчетами
     * @param model название шаблона для отображения
     * @return страница с данными
     */
    @GetMapping("/")
    public String showReportsPage(Model model) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        return "reports";
    }

    /**
     * Отображает страницу просмотра отчета
     * @param reportId ID отчёта
     * @param model название шаблона для отображения
     * @return страница с данными
     */
    @GetMapping("/{reportId}")
    public String viewReport(@PathVariable Long reportId, Model model) {
        ReportStatus status = reportService.getReportStatus(reportId);
        String content = reportService.getReportContent(reportId);

        model.addAttribute("reportId", reportId);
        model.addAttribute("status", status);
        model.addAttribute("content", content);

        return "report-view";
    }
}
