package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
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

    @Transactional
    @Override
    public void recalculateDailyReportTotals(DailyReport dailyReport) {
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
            key = "'reports:' + #targetDate")
    @Override
    public List<CalendarDayResponse> getCalendarDataForMonth(LocalDate targetDate) {
        User user = userService.getAuthUser()
                .orElseThrow(() -> new RuntimeException("Пользователь не авторизован"));

        LocalDate startDate = targetDate.withDayOfMonth(1);
        LocalDate endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());

        List<DailyReport> reports = dailyReportRepository.findByUserIdAndDateRange(
                user.getId(), startDate, endDate);

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
            key = "'reports:' + #year + '-' + #month")
    @Override
    public List<DailyReportResponse> getDailyReportsForMonth(int year, int month) {
        User user = userService.getAuthUser()
                .orElseThrow(() -> new RuntimeException("Пользователь не авторизован"));
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<DailyReport> dailyReport = dailyReportRepository.findByUserIdAndReportDateBetween(
                user.getId(), startDate, endDate);

        return dailyReport.stream()
                .map(this::convertToResponse)
                .toList();
    }

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
}
