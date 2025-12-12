package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class DailyReportRepositoryTest {

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private DailyReport todayReport;

    @BeforeEach
    void setUp() {
        dailyReportRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("test@example.com", "testuser", "password123");
        userRepository.save(testUser);

        todayReport = new DailyReport(testUser, LocalDate.now());
        todayReport.setTotalCaloriesConsumed(2000.0);
        dailyReportRepository.save(todayReport);

        DailyReport yesterdayReport = new DailyReport(testUser, LocalDate.now().minusDays(1));
        yesterdayReport.setTotalCaloriesConsumed(1800.0);
        dailyReportRepository.save(yesterdayReport);
    }

    @Test
    void findByUserIdAndReportDate_WhenExists_ShouldReturnReport() {
        Optional<DailyReport> report = dailyReportRepository.findByUserIdAndReportDate(
                testUser.getId(),
                LocalDate.now()
        );

        assertThat(report).isPresent();
        assertThat(report.get().getReportDate()).isEqualTo(LocalDate.now());
        assertThat(report.get().getTotalCaloriesConsumed()).isEqualTo(2000.0);
    }

    @Test
    void findByUserIdAndReportDate_WhenNotExists_ShouldReturnEmpty() {
        Optional<DailyReport> report = dailyReportRepository.findByUserIdAndReportDate(
                testUser.getId(),
                LocalDate.now().plusDays(1)
        );

        assertThat(report).isEmpty();
    }

    @Test
    void findDailyReportsByCaloriesRange_ShouldReturnFilteredReports() {
        List<DailyReport> reports = dailyReportRepository.findDailyReportsByCaloriesRange(
                testUser.getId(),
                1900.0,
                2100.0
        );

        assertThat(reports).isNotEmpty();
        assertThat(reports).extracting(DailyReport::getTotalCaloriesConsumed)
                .allMatch(calories -> calories >= 1900.0 && calories <= 2100.0);
    }

    @Test
    void findDailyReportsByCaloriesRange_WhenNoMatches_ShouldReturnEmpty() {
        List<DailyReport> reports = dailyReportRepository.findDailyReportsByCaloriesRange(
                testUser.getId(),
                3000.0,
                4000.0
        );

        assertThat(reports).isEmpty();
    }

    @Test
    void findByUserIdAndDateRange_ShouldReturnReportsInRange() {
        List<DailyReport> reports = dailyReportRepository.findByUserIdAndDateRange(
                testUser.getId(),
                LocalDate.now().minusDays(2),
                LocalDate.now()
        );

        assertThat(reports).hasSize(2);
        assertThat(reports).extracting(DailyReport::getReportDate)
                .containsExactlyInAnyOrder(LocalDate.now(), LocalDate.now().minusDays(1));
    }

    @Test
    void findByUserIdAndDateRange_WhenWrongRange_ShouldReturnEmpty() {
        List<DailyReport> reports = dailyReportRepository.findByUserIdAndDateRange(
                testUser.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        assertThat(reports).isEmpty();
    }

    @Test
    void findByUserIdAndReportDateBetween_ShouldReturnReports() {
        List<DailyReport> reports = dailyReportRepository.findByUserIdAndReportDateBetween(
                testUser.getId(),
                LocalDate.now().minusDays(2),
                LocalDate.now()
        );

        assertThat(reports).hasSize(2);
    }

    @Test
    void countByCreatedAtBetween_ShouldCountCorrectly() {
        Long count = dailyReportRepository.countByCreatedAtBetween(
                java.time.LocalDateTime.now().minusDays(1),
                java.time.LocalDateTime.now().plusDays(1)
        );

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByCreatedAtBetween_WhenNoReports_ShouldReturnZero() {
        Long count = dailyReportRepository.countByCreatedAtBetween(
                java.time.LocalDateTime.now().plusDays(10),
                java.time.LocalDateTime.now().plusDays(20)
        );

        assertThat(count).isZero();
    }

    @Test
    void saveDailyReport_WithNullUser_ShouldThrowException() {
        DailyReport report = new DailyReport();
        report.setReportDate(LocalDate.now());

        assertThatThrownBy(() -> dailyReportRepository.save(report))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveDailyReport_WithNullDate_ShouldThrowException() {
        DailyReport report = new DailyReport();
        report.setUser(testUser);

        assertThatThrownBy(() -> dailyReportRepository.save(report))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateDailyReport_ShouldPersistChanges() {
        todayReport.setTotalCaloriesConsumed(2500.0);
        todayReport.setGoalAchieved(false);
        dailyReportRepository.save(todayReport);

        DailyReport updatedReport = dailyReportRepository.findById(todayReport.getId()).orElseThrow();
        assertThat(updatedReport.getTotalCaloriesConsumed()).isEqualTo(2500.0);
        assertThat(updatedReport.getGoalAchieved()).isFalse();
    }

    @Test
    void deleteDailyReport_ShouldRemoveReport() {
        Long reportId = todayReport.getId();
        dailyReportRepository.delete(todayReport);

        boolean exists = dailyReportRepository.existsById(reportId);
        assertThat(exists).isFalse();
    }

    @Test
    void createReportsForDifferentUsers_ShouldSucceed() {
        User anotherUser = new User("another@example.com", "anotheruser", "password");
        userRepository.save(anotherUser);

        DailyReport anotherReport = new DailyReport(anotherUser, LocalDate.now());
        assertThatCode(() -> dailyReportRepository.save(anotherReport))
                .doesNotThrowAnyException();
    }
}
