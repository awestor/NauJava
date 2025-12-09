package ru.daniil.NauJava.controller.dailyReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.response.CalendarDayResponse;
import ru.daniil.NauJava.response.DailyReportResponse;
import ru.daniil.NauJava.service.DailyReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-reports")
public class DailyReportApiController {

    private final DailyReportService dailyReportService;

    private static final Logger logger = LoggerFactory.getLogger(DailyReportApiController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    public DailyReportApiController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    /**
     * Используется в профиле для получения дней когда пользователь потреблял калории
     * @param year год
     * @param month месяц
     * @return список из дат когда пользователь потреблял калории
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDayResponse>> getCalendarData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        try {
            appLogger.info("GET /api/daily-reports/calendar | Получение календаря для профиля пользователя");

            LocalDate targetDate = LocalDate.now();
            if (year != null && month != null) {
                targetDate = LocalDate.of(year, month, 1);
            }

            List<CalendarDayResponse> calendarData = dailyReportService.getCalendarDataForMonth(targetDate);
            return ResponseEntity.ok(calendarData);
        } catch (Exception e) {
            logger.warn("Получение календаря для профиля пользователя прошло неудачно с ошибкой:{}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Используется в шаблоне CalendarActivity для получения данных по тому сколько и
     * когда пользователь потреблял калории, а также какие лимиты потребления у него есть
     * @param year год
     * @param month месяц
     * @return список дат и величин потребления калорий
     */
    @GetMapping
    @RequestMapping("/data")
    public ResponseEntity<List<DailyReportResponse>> getDailyReports(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            appLogger.info("GET /api/daily-reports/data | Получение статистики по пользовательскому потреблению");

            List<DailyReportResponse> reports = dailyReportService.getDailyReportsForMonth(year, month);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.warn("Получение статистики прошло неудачно с ошибкой:{}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
