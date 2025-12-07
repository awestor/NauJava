package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.response.CalendarDayResponse;
import ru.daniil.NauJava.response.DailyReportResponse;
import ru.daniil.NauJava.response.NutritionSumResponse;
import ru.daniil.NauJava.service.DailyReportServiceImpl;
import ru.daniil.NauJava.service.MealEntryService;
import ru.daniil.NauJava.service.UserService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock
    private DailyReportRepository dailyReportRepository;

    @Mock
    private MealEntryService mealEntryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DailyReportServiceImpl dailyReportService;

    private User testUser;
    private DailyReport testDailyReport;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "testuser", "password");
        testUser.setId(1L);

        testDailyReport = new DailyReport(testUser, LocalDate.now());
        testDailyReport.setId(1L);
        testDailyReport.setTotalCaloriesConsumed(2000.0);
        testDailyReport.setTotalProteinsConsumed(80.0);
        testDailyReport.setTotalFatsConsumed(60.0);
        testDailyReport.setTotalCarbsConsumed(250.0);
        testDailyReport.setGoalAchieved(true);
    }

    @Test
    void getOrCreateDailyReportAuth_WhenReportExists_ShouldReturnExisting() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(testDailyReport));

        DailyReport result = dailyReportService.getOrCreateDailyReportAuth(LocalDate.now());

        assertNotNull(result);
        assertEquals(testDailyReport, result);
        verify(dailyReportRepository, never()).save(any(DailyReport.class));
    }

    @Test
    void getOrCreateDailyReportAuth_WhenReportNotExists_ShouldCreateNew() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(dailyReportRepository.save(any(DailyReport.class))).thenReturn(testDailyReport);

        DailyReport result = dailyReportService.getOrCreateDailyReportAuth(LocalDate.now());

        assertNotNull(result);
        verify(dailyReportRepository).save(any(DailyReport.class));
    }

    @Test
    void getOrCreateDailyReportAuth_WhenUserNotAuthenticated_ShouldThrowException() {
        when(userService.getAuthUser()).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            dailyReportService.getOrCreateDailyReportAuth(LocalDate.now());
        });
    }

    @Test
    void getDailyReportAuth_WhenReportExists_ShouldReturnOptional() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(testDailyReport));

        Optional<DailyReport> result = dailyReportService.getDailyReportAuth(LocalDate.now());

        assertTrue(result.isPresent());
        assertEquals(testDailyReport, result.get());
    }

    @Test
    void getDailyReportAuth_WhenReportNotExists_ShouldReturnEmpty() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        Optional<DailyReport> result = dailyReportService.getDailyReportAuth(LocalDate.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void getOrCreateDailyReport_ForUser_WhenReportExists_ShouldReturnExisting() {
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(testDailyReport));

        DailyReport result = dailyReportService.getOrCreateDailyReport(testUser, LocalDate.now());

        assertNotNull(result);
        assertEquals(testDailyReport, result);
        verify(dailyReportRepository, never()).save(any(DailyReport.class));
    }

    @Test
    void getOrCreateDailyReport_ForUser_WhenReportNotExists_ShouldCreateNew() {
        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(dailyReportRepository.save(any(DailyReport.class))).thenReturn(testDailyReport);

        DailyReport result = dailyReportService.getOrCreateDailyReport(testUser, LocalDate.now());

        assertNotNull(result);
        verify(dailyReportRepository).save(any(DailyReport.class));
    }

    @Test
    void recalculateDailyReportTotals_ShouldUpdateTotals() {
        NutritionSumResponse nutritionSum = new NutritionSumResponse(1800.0, 75.0, 50.0, 220.0);

        when(mealEntryService.getNutritionSumByDailyReportId(1L))
                .thenReturn(nutritionSum);
        when(dailyReportRepository.save(testDailyReport)).thenReturn(testDailyReport);

        dailyReportService.recalculateDailyReportTotals(testDailyReport);

        assertEquals(1800.0, testDailyReport.getTotalCaloriesConsumed());
        assertEquals(75.0, testDailyReport.getTotalProteinsConsumed());
        assertEquals(50.0, testDailyReport.getTotalFatsConsumed());
        assertEquals(220.0, testDailyReport.getTotalCarbsConsumed());
        verify(dailyReportRepository).save(testDailyReport);
    }

    @Test
    void recalculateDailyReportTotals_WhenEmptyNutrition_ShouldSetZeros() {
        NutritionSumResponse emptyNutrition = new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);

        when(mealEntryService.getNutritionSumByDailyReportId(1L))
                .thenReturn(emptyNutrition);
        when(dailyReportRepository.save(testDailyReport)).thenReturn(testDailyReport);

        dailyReportService.recalculateDailyReportTotals(testDailyReport);

        assertEquals(0.0, testDailyReport.getTotalCaloriesConsumed());
        assertEquals(0.0, testDailyReport.getTotalProteinsConsumed());
        assertEquals(0.0, testDailyReport.getTotalFatsConsumed());
        assertEquals(0.0, testDailyReport.getTotalCarbsConsumed());
    }

    @Test
    void getOrCreateDailyReportById_WhenExists_ShouldReturnReport() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findById(1L))
                .thenReturn(Optional.of(testDailyReport));

        DailyReport result = dailyReportService.getOrCreateDailyReportById(1L);

        assertNotNull(result);
        assertEquals(testDailyReport, result);
        verify(dailyReportRepository, never()).save(any(DailyReport.class));
    }

    @Test
    void getOrCreateDailyReportById_WhenNotExists_ShouldCreateNew() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findById(1L))
                .thenReturn(Optional.empty());
        when(dailyReportRepository.save(any(DailyReport.class))).thenReturn(testDailyReport);

        DailyReport result = dailyReportService.getOrCreateDailyReportById(1L);

        assertNotNull(result);
        verify(dailyReportRepository).save(any(DailyReport.class));
    }

    @Test
    void getCalendarDataForMonth_ShouldReturnCalendarData() {
        LocalDate targetDate = LocalDate.of(2024, 12, 1);
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        DailyReport report1 = new DailyReport(testUser, LocalDate.of(2024, 12, 5));
        report1.setGoalAchieved(true);
        DailyReport report2 = new DailyReport(testUser, LocalDate.of(2024, 12, 10));
        report2.setGoalAchieved(false);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndDateRange(1L, startDate, endDate))
                .thenReturn(Arrays.asList(report1, report2));

        List<CalendarDayResponse> calendarData = dailyReportService.getCalendarDataForMonth(targetDate);

        assertNotNull(calendarData);
        assertEquals(31, calendarData.size()); // 31 день в декабре

        // Проверяем, что 5 декабря цель достигнута
        Optional<CalendarDayResponse> dec5 = calendarData.stream()
                .filter(c -> c.getDate().equals("2024-12-05"))
                .findFirst();
        assertTrue(dec5.isPresent());
        assertTrue(dec5.get().getIsGoalAchieved());

        // Проверяем, что 10 декабря цель не достигнута
        Optional<CalendarDayResponse> dec10 = calendarData.stream()
                .filter(c -> c.getDate().equals("2024-12-10"))
                .findFirst();
        assertTrue(dec10.isPresent());
        assertFalse(dec10.get().getIsGoalAchieved());
    }

    @Test
    void getCalendarDataForMonth_WhenUserNotAuthenticated_ShouldThrowException() {
        when(userService.getAuthUser()).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            dailyReportService.getCalendarDataForMonth(LocalDate.now());
        }, "Пользователь не авторизован");
    }

    @Test
    void getDailyReportsForMonth_ShouldReturnReports() {
        int year = 2024;
        int month = 12;
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, 31);

        DailyReport report1 = new DailyReport(testUser, LocalDate.of(2024, 12, 5));
        report1.setId(1L);
        report1.setTotalCaloriesConsumed(2000.0);
        report1.setGoalAchieved(true);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(report1));

        List<DailyReportResponse> reports = dailyReportService.getDailyReportsForMonth(year, month);

        assertNotNull(reports);
        assertEquals(1, reports.size());

        DailyReportResponse response = reports.get(0);
        assertEquals(1L, response.getId());
        assertEquals(2000.0, response.getTotalCaloriesConsumed());
        assertTrue(response.getGoalAchieved());
        assertEquals("2024-12-05", response.getReportDate());
    }

    @Test
    void getDailyReportsForMonth_WhenNoReports_ShouldReturnEmptyList() {
        int year = 2024;
        int month = 12;
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, 31);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDateBetween(1L, startDate, endDate))
                .thenReturn(Collections.emptyList());

        List<DailyReportResponse> reports = dailyReportService.getDailyReportsForMonth(year, month);

        assertNotNull(reports);
        assertTrue(reports.isEmpty());
    }

    @Test
    void getDailyReportsForMonth_WhenUserNotAuthenticated_ShouldThrowException() {
        when(userService.getAuthUser()).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            dailyReportService.getDailyReportsForMonth(2024, 12);
        }, "Пользователь не авторизован");
    }

    @Test
    void getOrCreateDailyReportAuth_WithDifferentDates_ShouldWorkCorrectly() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        DailyReport yesterdayReport = new DailyReport(testUser, yesterday);
        yesterdayReport.setId(2L);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, yesterday))
                .thenReturn(Optional.of(yesterdayReport));
        when(dailyReportRepository.findByUserIdAndReportDate(1L, tomorrow))
                .thenReturn(Optional.empty());
        when(dailyReportRepository.save(any(DailyReport.class))).thenReturn(testDailyReport);

        DailyReport result1 = dailyReportService.getOrCreateDailyReportAuth(yesterday);
        DailyReport result2 = dailyReportService.getOrCreateDailyReportAuth(tomorrow);

        assertEquals(yesterdayReport, result1);
        assertNotNull(result2);
        verify(dailyReportRepository, times(1)).save(any(DailyReport.class));
    }
}