package ru.daniil.NauJava.service.admin;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.repository.*;
import ru.daniil.NauJava.response.ReportResponse;
import ru.daniil.NauJava.service.DailyReportService;
import ru.daniil.NauJava.service.MealService;
import ru.daniil.NauJava.service.ProductService;
import ru.daniil.NauJava.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final ProductService productService;
    private final MealService mealService;
    private final DailyReportService dailyReportService;

    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD-LOGGER");

    public ReportServiceImpl(ReportRepository reportRepository,
                             UserService userService,
                             ProductService productService,
                             MealService mealService,
                             DailyReportService dailyReportService) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.productService = productService;
        this.mealService = mealService;
        this.dailyReportService = dailyReportService;
    }

    /**
     * Проверяет существование отчета за указанный период
     */
    public boolean reportExistsForPeriod(LocalDate startDate, LocalDate endDate) {
        return reportRepository.existsByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    /**
     * Находит отчет за указанный период
     */
    public Optional<Report> findReportForPeriod(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    @Transactional
    @CacheEvict(value = "admin-reports-page", allEntries = true)
    public Long createReport(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        Optional<Report> existingReport = reportRepository
                .findByReportPeriodStartAndReportPeriodEnd(startDate, endDate);

        if (existingReport.isPresent() && endDate.isEqual(today)) {
            Report report = existingReport.get();
            report.setStatus(ReportStatus.CREATED);
            report.setContent("Отчет обновляется...");
            report.setCreatedAt(LocalDateTime.now());

            Report savedReport = reportRepository.save(report);
            return savedReport.getId();
        }

        Report report = new Report(startDate, endDate);
        report.setStatus(ReportStatus.CREATED);
        report.setContent("Отчет формируется...");

        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }

    public String getReportContent(Long reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getContent)
                .orElse(null);
    }

    public ReportStatus getReportStatus(Long reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getStatus)
                .orElse(null);
    }

    /**
     * Получает все отчеты, отсортированные по дате создания
     * @return список отчетов
     */
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    private Page<Report> getReportsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reportRepository.findAllWithPagination(pageable);
    }

    @Cacheable(value = "admin-reports-page",
            key = "'reports:' + #page + '-' + #size")
    @Override
    public List<ReportResponse> getPaginateReports(Integer page, Integer size) {
        Page<Report> reportPage = getReportsPage(page, size);

        return reportPage.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public Optional<Report> getLatestReport() {
        return reportRepository.findTopByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Report> getReportById(Long Id) {
        return reportRepository.findById(Id);
    }

    @Override
    public Long countReports() {
        return reportRepository.count();
    }

    @Override
    public void generateReportAsync(Long reportId) {
        CompletableFuture.runAsync(() -> {
            try {
                Report report = reportRepository.findById(reportId)
                        .orElseThrow(() -> new RuntimeException("Отчёт не найден"));

                report.setStatus(ReportStatus.PROCESSING);
                reportRepository.save(report);

                LocalDateTime startPeriod = report.getReportPeriodStart().atStartOfDay();
                LocalDateTime endPeriod = report.getReportPeriodEnd().atTime(23, 59, 59);

                long totalStartTime = System.currentTimeMillis();

                CompletableFuture<Long> usersFuture = CompletableFuture.supplyAsync(() ->
                        countUsersRegistered(startPeriod, endPeriod)
                );

                CompletableFuture<Long> productsFuture = CompletableFuture.supplyAsync(() ->
                        countProductsCreated(startPeriod, endPeriod)
                );

                CompletableFuture<MealStats> mealStatsFuture = CompletableFuture.supplyAsync(() ->
                        calculateMealStats(startPeriod, endPeriod)
                );

                CompletableFuture<Long> dailyReportsFuture = CompletableFuture.supplyAsync(() ->
                        countDailyReportsCreated(startPeriod, endPeriod)
                );

                CompletableFuture.allOf(usersFuture, productsFuture, mealStatsFuture, dailyReportsFuture)
                        .get(60, TimeUnit.SECONDS);

                Long totalUsers = usersFuture.get();
                Long totalProducts = productsFuture.get();
                MealStats mealStats = mealStatsFuture.get();
                Long totalDailyReports = dailyReportsFuture.get();

                long totalExecutionTime = System.currentTimeMillis() - totalStartTime;

                updateReportWithResults(report, totalUsers, totalProducts, mealStats,
                        totalDailyReports, totalExecutionTime);

            } catch (TimeoutException e) {
                handleReportError(reportId, "Произошёл таймаут при формировании отчета.");
            } catch (Exception e) {
                handleReportError(reportId, "Ошибка при формировании отчета: " + e.getMessage());
            }
        });
    }

    /**
     * Подсчет зарегистрированных пользователей за период
     * @param start начало периода
     * @param end конец периода
     * @return число пользователей
     */
    private Long countUsersRegistered(LocalDateTime start, LocalDateTime end) {
        return userService.countByCreatedAtBetween(start, end);
    }

    /**
     * Подсчет созданных продуктов за период
     * @param start начало периода
     * @param end конец периода
     * @return число продуктов
     */
    private Long countProductsCreated(LocalDateTime start, LocalDateTime end) {
        return productService.countByCreatedAtBetween(start, end);
    }

    /**
     * Подсчет созданных daily reports за период
     * @param start начало периода
     * @param end конец периода
     * @return число дневных отчётов
     */
    private Long countDailyReportsCreated(LocalDateTime start, LocalDateTime end) {
        return dailyReportService.countByCreatedAtBetween(start, end);
    }

    /**
     * Расчет статистики по приемам пищи в диапазоне дат
     * @param start начало диапазона дат
     * @param end конец диапазона дат
     * @return статистика по потреблению пользователя
     */
    private MealStats calculateMealStats(LocalDateTime start, LocalDateTime end) {
        List<Long> activeUserIds = mealService.findDistinctUserIdsWithMealsBetween(start, end);

        if (activeUserIds.isEmpty()) {
            return new MealStats(0L, 0.0, 0L);
        }

        Long totalMeals = mealService.countMealsForUsersBetweenDates(activeUserIds, start, end);

        Double averageMeals = totalMeals.doubleValue() / activeUserIds.size();

        return new MealStats((long) activeUserIds.size(), averageMeals, totalMeals);
    }

    /**
     * Обновление отчета с результатами
     */
    private void updateReportWithResults(Report report, Long totalUsers, Long totalProducts,
                                         MealStats mealStats, Long totalDailyReports,
                                         Long totalExecutionTime) {

        String content = String.format("""
            Отчет за период с %s по %s
            
            Статистика системы:
            - Зарегистрировано пользователей: %d
            - Создано продуктов: %d
            - Создано ежедневных отчетов (DailyReport): %d
            
            Статистика активности пользователей:
            - Активных пользователей: %d
            - Всего приемов пищи у активных пользователей: %d
            - Среднее количество приемов пищи на активного пользователя: %.2f
            
            Время формирования отчета: %d мс
            
            Отчет сформирован: %s
            """,
                report.getReportPeriodStart(),
                report.getReportPeriodEnd(),
                totalUsers,
                totalProducts,
                totalDailyReports,
                mealStats.activeUsersCount(),
                mealStats.totalMeals(),
                mealStats.averageMealsPerUser(),
                totalExecutionTime,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        );

        report.setStatus(ReportStatus.COMPLETED);
        report.setContent(content);
        report.setCompletedAt(LocalDateTime.now());
        report.setTotalExecutionTime(totalExecutionTime);
        report.setTotalUsersRegistered(totalUsers);
        report.setTotalProductsCreated(totalProducts);
        report.setAverageMealsPerActiveUser(mealStats.averageMealsPerUser());
        report.setTotalDailyReportsCreated(totalDailyReports);
        report.setActiveUsersCount(mealStats.activeUsersCount());

        reportRepository.save(report);
    }

    /**
     * Обработка ошибки при формировании отчета
     */
    private void handleReportError(Long reportId, String errorMessage) {
        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(ReportStatus.ERROR);
            report.setContent("Ошибка: " + errorMessage);
            reportRepository.save(report);
        });
    }

    private ReportResponse convertToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setStatus(report.getStatus().toString());
        if (report.getReportPeriodStart() != null) {
            response.setPeriodStart(report.getReportPeriodStart()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        }
        if (report.getReportPeriodEnd() != null) {
            response.setPeriodEnd(report.getReportPeriodEnd()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        }
        if (report.getCreatedAt() != null) {
            response.setCreatedAt(report.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        }
        if (report.getCompletedAt() != null) {
            response.setCompletedAt(report.getCompletedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        }
        response.setTotalExecutionTime(report.getTotalExecutionTime());
        return response;
    }

    private record MealStats(Long activeUsersCount, Double averageMealsPerUser, Long totalMeals) {}
}