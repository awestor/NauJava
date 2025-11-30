package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.CalendarDayResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportService {
    /**
     * Получает или создает DailyReport для авторизованного пользователя на указанную дату
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return полученный объект сущности DailyReport
     */
    DailyReport getOrCreateDailyReportAuth(LocalDate reportDate);

    /**
     * Получает дневной отчёт для авторизованного пользователя
     * @param reportDate дата за который создан дневной отчёт
     * @return дневной отчёт
     */
    Optional<DailyReport> getDailyReportAuth(LocalDate reportDate);

    /**
     * Получает или создает DailyReport для пользователя на указанную дату
     * @param user объект с данными по пользователю
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return полученный объект сущности DailyReport
     */
    DailyReport getOrCreateDailyReport(User user, LocalDate reportDate);

    /**
     * Пересчитывает общие показатели для DailyReport
     * @param dailyReport дневной отчёт
     */
    void recalculateDailyReportTotals(DailyReport dailyReport);

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
     * Получает данные по дневным отчётам за год и месяц
     * @param year год
     * @param month месяц
     * @return список DailyReport
     */
    List<DailyReport> getDailyReportsForMonth(int year, int month);
}
