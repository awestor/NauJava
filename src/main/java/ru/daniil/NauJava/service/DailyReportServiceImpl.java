package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.request.NutritionSumResponse;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class DailyReportServiceImpl implements DailyReportService {
    private final DailyReportRepository dailyReportRepository;
    private final MealEntityService mealEntityService;

    @Autowired
    public DailyReportServiceImpl(DailyReportRepository dailyReportRepository,
                                  MealEntityService mealEntityService) {
        this.dailyReportRepository = dailyReportRepository;
        this.mealEntityService = mealEntityService;
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
    public void recalculateDailyReportTotals(Long dailyReportId) {
        Optional<DailyReport> dailyReportOpt = dailyReportRepository.findById(dailyReportId);
        if (dailyReportOpt.isPresent()) {
            DailyReport dailyReport = dailyReportOpt.get();

            NutritionSumResponse sumNutrition = mealEntityService.getNutritionSumByDailyReportId(dailyReportId);

            dailyReport.setTotalCaloriesConsumed(sumNutrition.getTotalCalories());
            dailyReport.setTotalProteinsConsumed(sumNutrition.getTotalProteins());
            dailyReport.setTotalFatsConsumed(sumNutrition.getTotalFats());
            dailyReport.setTotalCarbsConsumed(sumNutrition.getTotalCarbs());

            dailyReportRepository.save(dailyReport);
        }
    }
}
