package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.response.CalendarDayResponse;
import ru.daniil.NauJava.response.DailyReportResponse;
import ru.daniil.NauJava.response.NutritionSumResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyReportServiceImpl implements DailyReportService {
    private final DailyReportRepository dailyReportRepository;
    private final MealEntryService mealEntityService;
    private final UserService userService;

    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD-LOGGER");

    @Autowired
    public DailyReportServiceImpl(DailyReportRepository dailyReportRepository,
                                  MealEntryService mealEntityService,
                                  UserService userService) {
        this.dailyReportRepository = dailyReportRepository;
        this.mealEntityService = mealEntityService;
        this.userService = userService;
    }

    @Transactional
    @Override
    public DailyReport getOrCreateDailyReportAuth(LocalDate reportDate) {
        User user = userService.getAuthUser().orElseThrow();
        return dailyReportRepository.findByUserIdAndReportDate(user.getId(), reportDate)
                .orElseGet(() -> createDailyReport(user, reportDate));
    }

    @Transactional
    @Override
    public Optional<DailyReport> getDailyReportAuth(LocalDate reportDate) {
        User user = userService.getAuthUser().orElseThrow();
        return dailyReportRepository.findByUserIdAndReportDate(user.getId(), reportDate);
    }

    @Transactional
    @Override
    public DailyReport getOrCreateDailyReport(User user, LocalDate reportDate) {
        return dailyReportRepository.findByUserIdAndReportDate(user.getId(), reportDate)
                .orElseGet(() -> createDailyReport(user, reportDate));
    }

    @Transactional
    @Override
    public void recalculateDailyReportTotals(DailyReport dailyReport) {
        methodLogger.info("{DailyReportServiceImpl.recalculateDailyReportTotals} |" +
                " Происходит расчёт суммы нутриентов по id dailyReport");
        NutritionSumResponse sumNutrition = mealEntityService.getNutritionSumByDailyReportId(dailyReport.getId());

        dailyReport.setTotalCaloriesConsumed(sumNutrition.getTotalCalories());
        dailyReport.setTotalProteinsConsumed(sumNutrition.getTotalProteins());
        dailyReport.setTotalFatsConsumed(sumNutrition.getTotalFats());
        dailyReport.setTotalCarbsConsumed(sumNutrition.getTotalCarbs());

        dailyReportRepository.save(dailyReport);
    }

    @Transactional
    @Override
    public DailyReport getOrCreateDailyReportById(Long dailyReportId) {
        User user = userService.getAuthUser().orElseThrow();
        return dailyReportRepository.findById(dailyReportId)
                .orElseGet(() -> createDailyReport(user, LocalDate.now()));
    }

    @Cacheable(value = "calendar-month",
            key = "'reports:' + #targetDate + '.' + #userId")
    @Override
    public List<CalendarDayResponse> getCalendarDataForMonth(LocalDate targetDate, Long userId) {
        LocalDate startDate = targetDate.withDayOfMonth(1);
        LocalDate endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());

        methodLogger.info("{DailyReportServiceImpl.getCalendarDataForMonth} |" +
                " Происходит вызов метода dailyReportRepository.findByUserIdAndDateRange с id:{}", userId);
        List<DailyReport> reports = dailyReportRepository.findByUserIdAndDateRange(
                userId, startDate, endDate);

        Map<LocalDate, Boolean> goalAchievedMap = reports.stream()
                .collect(Collectors.toMap(
                        DailyReport::getReportDate,
                        DailyReport::getGoalAchieved
                ));

        List<CalendarDayResponse> calendarData = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            Boolean isGoalAchieved = goalAchievedMap.get(currentDate);
            calendarData.add(new CalendarDayResponse(
                    currentDate.toString(),
                    Boolean.TRUE.equals(isGoalAchieved)
            ));
            currentDate = currentDate.plusDays(1);
        }

        return calendarData;
    }

    @Cacheable(value = "daily-reports",
            key = "'reports:' + #year + '-' + #month + '.' + #userId")
    @Override
    public List<DailyReportResponse> getDailyReportsForMonth(int year, int month, Long userId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        methodLogger.info("{DailyReportServiceImpl.getDailyReportsForMonth} |" +
                " Происходит вызов метода dailyReportRepository.findByUserIdAndReportDateBetween" +
                " с id:{}", userId);
        List<DailyReport> dailyReport = dailyReportRepository.findByUserIdAndReportDateBetween(
                userId, startDate, endDate);

        return dailyReport.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return dailyReportRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * Преобразует DailyReport в DailyReportResponse
     * @param report экземпляр DailyReport
     * @return DailyReportResponse
     */
    private DailyReportResponse convertToResponse(DailyReport report) {
        return new DailyReportResponse(
                    report.getId(),
                    report.getTotalCaloriesConsumed(),
                    report.getTotalProteinsConsumed(),
                    report.getTotalFatsConsumed(),
                    report.getTotalCarbsConsumed(),
                    report.getGoalAchieved(),
                    report.getReportDate().toString()
                );
    }

    /**
     * Создает новый DailyReport для пользователя
     * @param user объект с данными по пользователю
     * @param reportDate дата(год, месяц, день) за который формируется отчёт
     * @return созданная запись в БД и объект сущности DailyReport
     */
    private DailyReport createDailyReport(User user, LocalDate reportDate) {
        DailyReport dailyReport = new DailyReport(user, reportDate);

        return dailyReportRepository.save(dailyReport);
    }
}
