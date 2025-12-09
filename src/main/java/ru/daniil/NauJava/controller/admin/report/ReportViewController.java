package ru.daniil.NauJava.controller.admin.report;

import com.github.dockerjava.api.exception.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.service.admin.ReportService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/view/reports")
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
    @GetMapping("/list")
    public String showReportsPage(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "8") int size,
                                  Model model) {
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("periodStart", LocalDate.now().minusDays(7).toString());
        model.addAttribute("periodEnd", LocalDate.now().toString());

        model.addAttribute("currentPage", 0);
        model.addAttribute("pageSize", 8);
        model.addAttribute("totalElements", 0);
        model.addAttribute("totalPages", 0);
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

        if (status == null) {
            throw new NotFoundException("Отчет не найден");
        }

        model.addAttribute("reportId", reportId);
        model.addAttribute("status", status);
        model.addAttribute("content", content);

        return "report-view";
    }
}
