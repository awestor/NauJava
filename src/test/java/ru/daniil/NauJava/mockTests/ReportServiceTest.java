package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.repository.*;
import ru.daniil.NauJava.service.DailyReportService;
import ru.daniil.NauJava.service.MealService;
import ru.daniil.NauJava.service.ProductService;
import ru.daniil.NauJava.service.UserService;
import ru.daniil.NauJava.service.admin.ReportServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private MealService mealService;

    @Mock
    private DailyReportService dailyReportService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Report report;
    private Report completedReport;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);

        report = new Report(startDate, endDate);
        report.setId(1L);
        report.setStatus(ReportStatus.CREATED);
        report.setContent("Отчет формируется...");
        report.setCreatedAt(LocalDateTime.now());

        completedReport = new Report(startDate, endDate);
        completedReport.setId(2L);
        completedReport.setStatus(ReportStatus.COMPLETED);
        completedReport.setContent("Отчет сформирован");
        completedReport.setCreatedAt(LocalDateTime.now().minusDays(1));
        completedReport.setCompletedAt(LocalDateTime.now());
        completedReport.setTotalExecutionTime(1500L);
        completedReport.setTotalUsersRegistered(10L);
        completedReport.setTotalProductsCreated(25L);
        completedReport.setAverageMealsPerActiveUser(2.5);
        completedReport.setTotalDailyReportsCreated(30L);
        completedReport.setActiveUsersCount(8L);
    }

    @Test
    void reportExistsForPeriod_WhenReportExists_ShouldReturnTrue() {
        when(reportRepository.existsByReportPeriodStartAndReportPeriodEnd(startDate, endDate))
                .thenReturn(true);

        boolean exists = reportService.reportExistsForPeriod(startDate, endDate);

        assertThat(exists).isTrue();
        verify(reportRepository, times(1))
                .existsByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    @Test
    void reportExistsForPeriod_WhenReportNotExists_ShouldReturnFalse() {
        when(reportRepository.existsByReportPeriodStartAndReportPeriodEnd(startDate, endDate))
                .thenReturn(false);

        boolean exists = reportService.reportExistsForPeriod(startDate, endDate);

        assertThat(exists).isFalse();
        verify(reportRepository, times(1))
                .existsByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    @Test
    void findReportForPeriod_WhenReportExists_ShouldReturnReport() {
        when(reportRepository.findByReportPeriodStartAndReportPeriodEnd(startDate, endDate))
                .thenReturn(Optional.of(report));

        Optional<Report> result = reportService.findReportForPeriod(startDate, endDate);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getStatus()).isEqualTo(ReportStatus.CREATED);
        verify(reportRepository, times(1))
                .findByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    @Test
    void findReportForPeriod_WhenReportNotExists_ShouldReturnEmpty() {
        when(reportRepository.findByReportPeriodStartAndReportPeriodEnd(startDate, endDate))
                .thenReturn(Optional.empty());

        Optional<Report> result = reportService.findReportForPeriod(startDate, endDate);

        assertThat(result).isEmpty();
        verify(reportRepository, times(1))
                .findByReportPeriodStartAndReportPeriodEnd(startDate, endDate);
    }

    @Test
    void createReport_WhenNewReport_ShouldCreateAndReturnId() {
        LocalDate today = LocalDate.now();
        when(reportRepository.findByReportPeriodStartAndReportPeriodEnd(startDate, today))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report savedReport = invocation.getArgument(0);
            savedReport.setId(3L);
            return savedReport;
        });

        Long reportId = reportService.createReport(startDate, today);

        assertThat(reportId).isNotNull();
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void createReport_WhenExistingReportForToday_ShouldUpdateAndReturnId() {
        LocalDate today = LocalDate.now();
        Report existingReport = new Report(startDate, today);
        existingReport.setId(1L);
        existingReport.setStatus(ReportStatus.COMPLETED);

        when(reportRepository.findByReportPeriodStartAndReportPeriodEnd(startDate, today))
                .thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any(Report.class))).thenReturn(existingReport);

        Long reportId = reportService.createReport(startDate, today);

        assertThat(reportId).isEqualTo(1L);
        verify(reportRepository, times(1)).save(existingReport);
    }

    @Test
    void getReportContent_WhenReportExists_ShouldReturnContent() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        String content = reportService.getReportContent(1L);

        assertThat(content).isEqualTo("Отчет формируется...");
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void getReportContent_WhenReportNotExists_ShouldReturnNull() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        String content = reportService.getReportContent(999L);

        assertThat(content).isNull();
        verify(reportRepository, times(1)).findById(999L);
    }

    @Test
    void getReportStatus_WhenReportExists_ShouldReturnStatus() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportStatus status = reportService.getReportStatus(1L);

        assertThat(status).isEqualTo(ReportStatus.CREATED);
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void getReportStatus_WhenReportNotExists_ShouldReturnNull() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        ReportStatus status = reportService.getReportStatus(999L);

        assertThat(status).isNull();
        verify(reportRepository, times(1)).findById(999L);
    }

    @Test
    void getAllReports_ShouldReturnAllReportsSorted() {
        List<Report> reports = Arrays.asList(completedReport, report);
        when(reportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(reports);

        List<Report> result = reportService.getAllReports();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L); // completedReport должен быть первым
        assertThat(result.get(1).getId()).isEqualTo(1L); // report должен быть вторым
        verify(reportRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getReportById_WhenReportExists_ShouldReturnReport() {
        when(reportRepository.findById(2L)).thenReturn(Optional.of(completedReport));

        Optional<Report> result = reportService.getReportById(2L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        assertThat(result.get().getStatus()).isEqualTo(ReportStatus.COMPLETED);
        verify(reportRepository, times(1)).findById(2L);
    }

    @Test
    void getReportById_WhenReportNotExists_ShouldReturnEmpty() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Report> result = reportService.getReportById(999L);

        assertThat(result).isEmpty();
        verify(reportRepository, times(1)).findById(999L);
    }

    @Test
    void countReports_ShouldReturnCount() {
        when(reportRepository.count()).thenReturn(5L);

        Long count = reportService.countReports();

        assertThat(count).isEqualTo(5L);
        verify(reportRepository, times(1)).count();
    }

    @Test
    void getPaginateReports_ShouldReturnPaginatedReports() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> page = new PageImpl<>(List.of(report, completedReport), pageable, 2);
        when(reportRepository.findAllWithPagination(any(Pageable.class))).thenReturn(page);

        List<ru.daniil.NauJava.response.ReportResponse> result = reportService.getPaginateReports(0, 10);

        assertThat(result).hasSize(2);
        verify(reportRepository, times(1)).findAllWithPagination(any(Pageable.class));
    }

    @Test
    void getLatestReport_ShouldReturnMostRecentReport() {
        when(reportRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(completedReport));

        Optional<Report> result = reportService.getLatestReport();

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        verify(reportRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }

    @Test
    void generateReportAsync_WhenReportExists_ShouldStartProcessing() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        when(userService.countByCreatedAtBetween(any(), any())).thenReturn(5L);
        when(productService.countByCreatedAtBetween(any(), any())).thenReturn(10L);
        when(dailyReportService.countByCreatedAtBetween(any(), any())).thenReturn(15L);
        when(mealService.findDistinctUserIdsWithMealsBetween(any(), any())).thenReturn(List.of(1L, 2L, 3L));
        when(mealService.countMealsForUsersBetweenDates(anyList(), any(), any())).thenReturn(20L);

        reportService.generateReportAsync(1L);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(reportRepository, atLeastOnce()).save(any(Report.class));
    }

    @Test
    void countUsersRegistered_ShouldReturnCorrectCount() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(userService.countByCreatedAtBetween(start, end)).thenReturn(3L);
        when(productService.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(dailyReportService.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(mealService.findDistinctUserIdsWithMealsBetween(any(), any())).thenReturn(List.of());
        when(mealService.countMealsForUsersBetweenDates(anyList(), any(), any())).thenReturn(0L);

        reportService.generateReportAsync(1L);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userService, atLeastOnce()).countByCreatedAtBetween(any(), any());
    }

    @Test
    void getAllReports_WhenEmpty_ShouldReturnEmptyList() {
        when(reportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<Report> result = reportService.getAllReports();

        assertThat(result).isEmpty();
        verify(reportRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getLatestReport_WhenNoReports_ShouldReturnEmpty() {
        when(reportRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        Optional<Report> result = reportService.getLatestReport();

        assertThat(result).isEmpty();
        verify(reportRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }
}
