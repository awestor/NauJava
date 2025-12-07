package ru.daniil.NauJava.controller.admin.report;

import com.github.dockerjava.api.exception.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.response.ReportResponse;
import ru.daniil.NauJava.service.admin.ReportService;

import java.time.LocalDate;
import java.util.List;

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
        List<ReportResponse> response = reportService.getPaginateReports(page, size);
        Long totalElement = reportService.countReports();
        model.addAttribute("reports", response);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElement);
        model.addAttribute("totalPages", (totalElement == 0) ?
                 0 : (totalElement / size + 1));
        model.addAttribute("today", LocalDate.now());
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
