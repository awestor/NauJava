package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;

import java.time.LocalDate;

public interface DailyReportService {
    /**
     * Получает или создает DailyReport для пользователя на указанную дату
     * @param user объект с данными по пользователю
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return полученный объект сущности DailyReport
     */
    DailyReport getOrCreateDailyReport(User user, LocalDate reportDate);

    /**
     * Пересчитывает общие показатели для DailyReport
     * @param dailyReportId идентификатор отчёта для перерасчёта
     */
    void recalculateDailyReportTotals(Long dailyReportId);
}
