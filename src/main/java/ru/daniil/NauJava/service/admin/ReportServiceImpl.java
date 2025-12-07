package ru.daniil.NauJava.service.admin;

import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final MealRepository mealRepository;
    private final DailyReportRepository dailyReportRepository;

    public ReportServiceImpl(ReportRepository reportRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             MealRepository mealRepository,
                             DailyReportRepository dailyReportRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.mealRepository = mealRepository;
        this.dailyReportRepository = dailyReportRepository;
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

    /**
     * Создает новый отчет за указанный промежуток времени
     * @return ID созданного отчета
     */
    @Transactional
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


    /**
     * Получает содержимое отчета по ID
     * @param reportId ID отчета
     * @return содержимое отчета или null если отчет не найден
     */
    public String getReportContent(Long reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getContent)
                .orElse(null);
    }

    /**
     * Получает статус отчета по ID
     * @param reportId ID отчета
     * @return статус отчета или null если отчет не найден
     */
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

    /**
     * Получает последний созданный отчет
     * @return
     */
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

    /**
     * Асинхронно формирует отчет с использованием нескольких потоков
     * @param reportId
     */
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
     * @param start
     * @param end
     * @return
     */
    private Long countUsersRegistered(LocalDateTime start, LocalDateTime end) {
        return userRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * Подсчет созданных продуктов за период
     * @param start
     * @param end
     * @return
     */
    private Long countProductsCreated(LocalDateTime start, LocalDateTime end) {
        return productRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * Подсчет созданных daily reports за период
     * @param start
     * @param end
     * @return
     */
    private Long countDailyReportsCreated(LocalDateTime start, LocalDateTime end) {
        return dailyReportRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * Расчет статистики по приемам пищи
     * @param start
     * @param end
     * @return
     */
    private MealStats calculateMealStats(LocalDateTime start, LocalDateTime end) {
        List<Long> activeUserIds = mealRepository.findDistinctUserIdsWithMealsBetween(start, end);

        if (activeUserIds.isEmpty()) {
            return new MealStats(0L, 0.0, 0L);
        }

        Long totalMeals = mealRepository.countMealsForUsersBetweenDates(activeUserIds, start, end);

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