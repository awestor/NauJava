package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.response.CalendarDayResponse;
import ru.daniil.NauJava.response.DailyReportResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyReportService {
    /**
     * Получает дневной отчёт для авторизованного пользователя
     * @param reportDate дата за который создан дневной отчёт
     * @return дневной отчёт
     */
    Optional<DailyReport> getDailyReportAuth(LocalDate reportDate);

    /**
     * Получает или создает DailyReport для авторизованного пользователя на указанную дату
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return полученный объект сущности DailyReport
     */
    DailyReport getOrCreateDailyReportAuth(LocalDate reportDate);

    /**
     * Получает или создает DailyReport для пользователя на указанную дату
     * @param user объект с данными по пользователю
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return полученный объект сущности DailyReport
     */
    DailyReport getOrCreateDailyReport(User user, LocalDate reportDate);

    /**
     * Получает по его id или создаёт дневной отчёт в случае необходимости
     * @param dailyReportId id дневного отчёта
     * @return дневной отчёт
     */
    DailyReport getOrCreateDailyReportById(Long dailyReportId);

    /**
     * Получает данные по году и месяцу за которые
     * был поставлен флаг, что цель питания выполнена
     * @param targetDate год и месяц
     * @return список CalendarDayResponse состоящего из даты и флага достижения цели по питанию
     */
    List<CalendarDayResponse> getCalendarDataForMonth(LocalDate targetDate);

    /**
     * Получает данные по дневным отчётам за месяц в году
     * @param year год
     * @param month месяц
     * @return список DailyReport
     */
    List<DailyReportResponse> getDailyReportsForMonth(int year, int month);

    /**
     * Считает количество дневных отчётов зарегистрированных в системе в диапазоне дат
     * @param start начало диапазона дат
     * @param end конец диапазона дат
     * @return количество дневных отчётов
     */
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Пересчитывает общие показатели для DailyReport
     * @param dailyReport дневной отчёт
     */
    void recalculateDailyReportTotals(DailyReport dailyReport);
}
