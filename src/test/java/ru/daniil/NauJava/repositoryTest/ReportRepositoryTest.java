package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.Report;
import ru.daniil.NauJava.enums.ReportStatus;
import ru.daniil.NauJava.repository.ReportRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    private Report todayReport;
    private Report yesterdayReport;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();

        todayReport = new Report(
                LocalDate.now().minusDays(7),
                LocalDate.now()
        );
        todayReport.setStatus(ReportStatus.COMPLETED);
        todayReport.setTotalUsersRegistered(100L);
        reportRepository.save(todayReport);

        yesterdayReport = new Report(
                LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(8)
        );
        yesterdayReport.setStatus(ReportStatus.ERROR);
        yesterdayReport.setTotalProductsCreated(50L);
        reportRepository.save(yesterdayReport);
    }

    @Test
    void findAllByOrderByCreatedAtDesc_ShouldReturnSortedReports() {
        List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).getCreatedAt())
                .isAfterOrEqualTo(reports.get(1).getCreatedAt());
    }

    @Test
    void findAllByOrderByCreatedAtDesc_WhenNoReports_ShouldReturnEmpty() {
        reportRepository.deleteAll();

        List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();
        assertThat(reports).isEmpty();
    }

    @Test
    void findTopByOrderByCreatedAtDesc_ShouldReturnLatestReport() {
        Optional<Report> latestReport = reportRepository.findTopByOrderByCreatedAtDesc();

        assertThat(latestReport).isPresent();
        assertThat(latestReport.get().getCreatedAt())
                .isAfterOrEqualTo(yesterdayReport.getCreatedAt());
    }

    @Test
    void findTopByOrderByCreatedAtDesc_WhenNoReports_ShouldReturnEmpty() {
        reportRepository.deleteAll();

        Optional<Report> latestReport = reportRepository.findTopByOrderByCreatedAtDesc();
        assertThat(latestReport).isEmpty();
    }

    @Test
    void existsByReportPeriodStartAndReportPeriodEnd_WhenExists_ShouldReturnTrue() {
        boolean exists = reportRepository.existsByReportPeriodStartAndReportPeriodEnd(
                LocalDate.now().minusDays(7),
                LocalDate.now()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByReportPeriodStartAndReportPeriodEnd_WhenNotExists_ShouldReturnFalse() {
        boolean exists = reportRepository.existsByReportPeriodStartAndReportPeriodEnd(
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(20)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void findByReportPeriodStartAndReportPeriodEnd_WhenExists_ShouldReturnReport() {
        Optional<Report> report = reportRepository.findByReportPeriodStartAndReportPeriodEnd(
                LocalDate.now().minusDays(7),
                LocalDate.now()
        );

        assertThat(report).isPresent();
        assertThat(report.get().getReportPeriodStart()).isEqualTo(LocalDate.now().minusDays(7));
        assertThat(report.get().getReportPeriodEnd()).isEqualTo(LocalDate.now());
    }

    @Test
    void findByReportPeriodStartAndReportPeriodEnd_WhenNotExists_ShouldReturnEmpty() {
        Optional<Report> report = reportRepository.findByReportPeriodStartAndReportPeriodEnd(
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(20)
        );

        assertThat(report).isEmpty();
    }

    @Test
    void findAllLimited_ShouldReturnLimitedReports() {
        for (int i = 0; i < 50; i++) {
            Report report = new Report(
                    LocalDate.now().minusDays(i * 7),
                    LocalDate.now().minusDays(i * 7 - 1)
            );
            reportRepository.save(report);
        }

        List<Report> reports = reportRepository.findAllLimited();

        assertThat(reports).hasSizeLessThanOrEqualTo(48);
    }

    @Test
    void findAllWithPagination_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> page = reportRepository.findAllWithPagination(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    void saveReport_WithNullPeriodStart_ShouldThrowException() {
        Report report = new Report();
        report.setReportPeriodEnd(LocalDate.now());
        report.setStatus(ReportStatus.CREATED);

        assertThatThrownBy(() -> reportRepository.save(report))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveReport_WithNullPeriodEnd_ShouldThrowException() {
        Report report = new Report();
        report.setReportPeriodStart(LocalDate.now());
        report.setStatus(ReportStatus.CREATED);

        assertThatThrownBy(() -> reportRepository.save(report))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateReport_ShouldPersistChanges() {
        todayReport.setStatus(ReportStatus.ERROR);
        todayReport.setContent("Updated content");
        reportRepository.save(todayReport);

        Report updatedReport = reportRepository.findById(todayReport.getId()).orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.ERROR);
        assertThat(updatedReport.getContent()).isEqualTo("Updated content");
    }

    @Test
    void deleteReport_ShouldRemoveReport() {
        Long reportId = todayReport.getId();
        reportRepository.delete(todayReport);

        boolean exists = reportRepository.existsById(reportId);
        assertThat(exists).isFalse();
    }

    @Test
    void createReport_WithValidData_ShouldSucceed() {
        Report newReport = new Report(
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(20)
        );
        newReport.setStatus(ReportStatus.COMPLETED);
        newReport.setTotalExecutionTime(5000L);

        assertThatCode(() -> reportRepository.save(newReport))
                .doesNotThrowAnyException();
    }
}
