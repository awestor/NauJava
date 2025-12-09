package ru.daniil.NauJava.controller.admin.report;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.request.create.CreateReportRequest;
import ru.daniil.NauJava.response.ReportCreationResponse;
import ru.daniil.NauJava.response.ReportDataResponse;
import ru.daniil.NauJava.response.ReportResponse;
import ru.daniil.NauJava.service.admin.ReportService;
import ru.daniil.NauJava.service.admin.ReportServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/api/reports")
public class ReportApiController {

    private final ReportService reportService;

    private static final Logger logger = LoggerFactory.getLogger(ReportApiController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    public ReportApiController(ReportServiceImpl reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/page")
    public ResponseEntity<List<ReportResponse>> getReportsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size)
    {
        try {
            appLogger.info("GET /admin/api/reports/page | Получение страницы отчётов");
            if (page < 0) {
                page = 0;
            }

            if (!Arrays.asList(8, 16, 32, 48).contains(size)) {
                size = 8;
            }

            List<ReportResponse> reports = reportService.getPaginateReports(page, size);

            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Ошибка при получении страницы отчётов");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Возвращает суммарное количество отчётов в БД
     * @return количество отчётов
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Long> getTotalReportsCount() {
        try {
            long count = reportService.countReports();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Ошибка при получении количества отчётов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Возвращает последний созданный отчёт
     * @return данные последнего отчёта
     */
    @GetMapping("/latest")
    public ResponseEntity<Report> getLatestReport() {
        try {
            Optional<Report> reports = reportService.getLatestReport();

            return reports.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("При получении последнего отчёта возникла ошибка: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создает новый отчет и запускает его формирование
     * @return ID созданного отчета
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportCreationResponse> generateReport(
            @Valid @RequestBody CreateReportRequest request) {
        try {
            appLogger.info("GET /admin/api/reports/generate | генерация нового отчёта");
            if (request.getStartDate().isAfter(request.getEndDate())) {
                ReportCreationResponse response = new ReportCreationResponse(
                        null, "error",
                        "Дата начала не может быть позже даты окончания",
                        request.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        , request.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                );
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate today = LocalDate.now();
            if (!request.getEndDate().isEqual(today) &&
                    reportService.reportExistsForPeriod(request.getStartDate(), request.getEndDate())) {
                ReportCreationResponse response = new ReportCreationResponse(
                        null, "error",
                        "Отчет за указанный период уже существует",
                        request.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        request.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                );
                return ResponseEntity.badRequest().body(response);
            }

            Long reportId = reportService.createReport(request.getStartDate(), request.getEndDate());

            reportService.generateReportAsync(reportId);

            ReportCreationResponse response = new ReportCreationResponse(
                    reportId,
                    "success",
                    "Формирование отчета начато",
                    request.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    request.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при формировании нового отчёта: {}", e.getMessage());
            ReportCreationResponse response = new ReportCreationResponse(
                    null,
                    "error",
                    "Ошибка при создании отчета: " + e.getMessage(),
                    request.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    request.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
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
        try {
            ReportStatus status = reportService.getReportStatus(reportId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(status.name());
        } catch (Exception e) {
            logger.error("Ошибка при получении статуса отчёта {}: {}", reportId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получает содержимое отчета
     * @param reportId ID отчёта
     * @return содержимое отчёта
     */
    @GetMapping("/{reportId}/content")
    @ResponseBody
    public ResponseEntity<String> getReportContent(@PathVariable Long reportId) {
        try {
            appLogger.info("GET /admin/api/reports/{reportId}/content | Получение содержимого отчёта");
            String content = reportService.getReportContent(reportId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            logger.error("Ошибка при получении содержимого отчёта {}: {}", reportId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Возвращает информацию по определённому отчёту
     * @param reportId id отчёта
     * @return данные отчёта
     */
    @GetMapping("/{reportId}/data")
    @ResponseBody
    public ResponseEntity<ReportDataResponse> getReportData(@PathVariable Long reportId) {
        try {
            appLogger.info("GET /admin/api/reports/{reportId}/data | Получение даты за которые формировался отчёт");
            Report report = reportService.getReportById(reportId).orElse(null);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }
            ReportDataResponse response = new ReportDataResponse(
                    report.getStatus().toString(),
                    report.getReportPeriodStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    report.getReportPeriodEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    report.getTotalExecutionTime()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении данных отчёта {}: {}", reportId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkReportExists(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest().body(false);
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(false);
            }

            boolean exists = reportService.reportExistsForPeriod(startDate, endDate);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            System.err.println("Error checking report existence: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Скачивание отчёта в формате CSV
     * @param reportId id отчёта для скачивания
     * @return файл в формате .csv
     */
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReportCsv(@PathVariable Long reportId) {
        try {
            Report report = reportService.getReportById(reportId)
                    .orElseThrow(() -> new EntityNotFoundException("Отчёт не найден"));

            if (report.getStatus() != ReportStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(null);
            }

            String csvContent = generateCsvContent(report);
            byte[] contentBytes = csvContent.getBytes(StandardCharsets.UTF_8);

            String filename = String.format("report_%s_%s_%s.csv",
                    report.getReportPeriodStart().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    report.getReportPeriodEnd().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            ByteArrayResource resource = new ByteArrayResource(contentBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentBytes.length))
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .body(resource);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Повторно рассчитывает данные для отчёта
     * @param reportId id отчёта
     * @return код ответа 200 или 500
     */
    @PostMapping("/{reportId}/retry")
    @ResponseBody
    public ResponseEntity<Void> retryReport(@PathVariable Long reportId) {
        try {
            reportService.generateReportAsync(reportId);

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Ошибка при повторном формировании отчёта {}: {}", reportId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Генерирует CSV содержимое отчёта
     * @param report содержимое отчёта
     * @return содержимое файла
     */
    private String generateCsvContent(Report report) {
        StringBuilder csv = new StringBuilder();

        // Заголовок
        csv.append("Параметр,Значение,Единица измерения,Примечание\n");

        // Основные данные
        csv.append(String.format("Период отчёта (начало),%s,дата,\n",
                report.getReportPeriodStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        csv.append(String.format("Период отчёта (окончание),%s,дата,\n",
                report.getReportPeriodEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

        if (report.getTotalUsersRegistered() != null) {
            csv.append(String.format("Зарегистрировано пользователей,%d,чел.,\n",
                    report.getTotalUsersRegistered()));
        }

        if (report.getTotalProductsCreated() != null) {
            csv.append(String.format("Создано продуктов,%d,ед.,\n",
                    report.getTotalProductsCreated()));
        }

        if (report.getTotalDailyReportsCreated() != null) {
            csv.append(String.format("Создано ежедневных отчётов,%d,ед.,\n",
                    report.getTotalDailyReportsCreated()));
        }

        if (report.getActiveUsersCount() != null) {
            csv.append(String.format("Активных пользователей,%d,чел.,\n",
                    report.getActiveUsersCount()));
        }

        if (report.getAverageMealsPerActiveUser() != null) {
            csv.append(String.format("Среднее количество приёмов пищи,%.2f,ед./пользователя,\n",
                    report.getAverageMealsPerActiveUser()));
        }

        if (report.getTotalExecutionTime() != null) {
            csv.append(String.format("Время формирования,%d,мс,\n",
                    report.getTotalExecutionTime()));
            csv.append(String.format("Время формирования,%.2f,сек.,\n",
                    report.getTotalExecutionTime() / 1000.0));
        }

        csv.append(String.format("Дата создания отчёта,%s,дата/время,\n",
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));

        if (report.getCompletedAt() != null) {
            csv.append(String.format("Дата завершения отчёта,%s,дата/время,\n",
                    report.getCompletedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
        }

        csv.append(String.format("Статус отчёта,%s,,\n", report.getStatus()));

        return csv.toString();
    }
}