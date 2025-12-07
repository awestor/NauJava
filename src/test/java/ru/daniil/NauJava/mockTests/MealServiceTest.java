package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.NotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.MealRepository;
import ru.daniil.NauJava.repository.MealTypeRepository;
import ru.daniil.NauJava.response.NutritionSumResponse;
import ru.daniil.NauJava.service.DailyReportService;
import ru.daniil.NauJava.service.MealEntryService;
import ru.daniil.NauJava.service.MealServiceImpl;
import ru.daniil.NauJava.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private MealEntryService mealEntryService;

    @Mock
    private MealTypeRepository mealTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private DailyReportService dailyReportService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    @InjectMocks
    private MealServiceImpl mealService;

    private User testUser;
    private DailyReport testDailyReport;
    private MealType testMealType;
    private Meal testMeal;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "testuser", "password");
        testUser.setId(1L);

        testDailyReport = new DailyReport(testUser, LocalDate.now());
        testDailyReport.setId(1L);

        testMealType = new MealType("Завтрак", "Первый прием пищи");
        testMealType.setId(1L);

        testMeal = new Meal(testDailyReport, testMealType);
        testMeal.setId(1L);
        testMeal.setEatenAt(LocalDateTime.now());
    }

    @Test
    void createMealWithProducts_WhenValidData_ShouldCreateMeal() {
        List<String> productNames = Arrays.asList("Яблоко", "Банан");
        List<Integer> quantities = Arrays.asList(100, 150);

        MealEntry mealEntry1 = new MealEntry(testMeal, new Product
                ("Яблоко", "Описание", 10.0,
                        10.0, 10.0, 10.0), 100);
        MealEntry mealEntry2 = new MealEntry(testMeal, new Product
                ("Банан", "Описание", 10.0,
                        10.0, 10.0, 10.0), 150);
        List<MealEntry> mealEntries = Arrays.asList(mealEntry1, mealEntry2);

        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
        when(dailyReportService.getOrCreateDailyReportAuth(any(LocalDate.class)))
                .thenReturn(testDailyReport);
        when(mealTypeRepository.findByName("Завтрак"))
                .thenReturn(Optional.of(testMealType));
        when(mealRepository.save(any(Meal.class))).thenReturn(testMeal);
        when(mealEntryService.createMealEntries(any(Meal.class), anyList(), anyList()))
                .thenReturn(mealEntries);

        Meal result = mealService.createMealWithProducts("Завтрак", productNames, quantities);

        assertNotNull(result);
        assertEquals(testDailyReport, result.getDailyReport());
        assertEquals(testMealType.getName(), result.getMealType());
        verify(transactionManager).commit(transactionStatus);
        verify(mealEntryService).createMealEntries(any(Meal.class), eq(productNames), eq(quantities));
    }

    @Test
    void createMealWithProducts_WhenMealTypeNotFound_ShouldThrowException() {
        List<String> productNames = List.of("Яблоко");
        List<Integer> quantities = List.of(100);

        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
        when(dailyReportService.getOrCreateDailyReportAuth(any(LocalDate.class)))
                .thenReturn(testDailyReport);
        when(mealTypeRepository.findByName("Несуществующий"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            mealService.createMealWithProducts("Несуществующий", productNames, quantities);
        });

        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void createMealWithProducts_WhenExceptionOccurs_ShouldRollback() {
        List<String> productNames = List.of("Яблоко");
        List<Integer> quantities = List.of(100);

        when(transactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
        when(dailyReportService.getOrCreateDailyReportAuth(any(LocalDate.class)))
                .thenReturn(testDailyReport);
        when(mealTypeRepository.findByName("Завтрак"))
                .thenReturn(Optional.of(testMealType));
        when(mealRepository.save(any(Meal.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> {
            mealService.createMealWithProducts("Завтрак", productNames, quantities);
        });

        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void getTodayMeals_WhenUserAuthenticated_ShouldReturnMeals() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(mealRepository.findByDailyReportUserIdAndDailyReportReportDate(1L, LocalDate.now()))
                .thenReturn(Collections.singletonList(testMeal));

        List<Meal> meals = mealService.getTodayMeals("test@example.com");

        assertEquals(1, meals.size());
        assertEquals(testMeal, meals.get(0));
    }

    @Test
    void getTodayMeals_WhenUserNotAuthenticated_ShouldReturnEmptyList() {
        when(userService.getAuthUser()).thenReturn(Optional.empty());

        List<Meal> meals = mealService.getTodayMeals("test@example.com");

        assertNotNull(meals);
        assertTrue(meals.isEmpty());
    }

    @Test
    void getByDailyReportId_ShouldReturnMeals() {
        when(mealRepository.findByDailyReportId(1L))
                .thenReturn(Collections.singletonList(testMeal));

        List<Meal> meals = mealService.getByDailyReportId(1L);

        assertEquals(1, meals.size());
        assertEquals(1L, meals.get(0).getDailyReport().getId());
    }

    @Test
    void deleteCurrentMeal_WhenExists_ShouldDeleteAndUpdateNutrition() {
        when(mealRepository.findById(1L)).thenReturn(Optional.of(testMeal));
        when(dailyReportService.getOrCreateDailyReportById(testDailyReport.getId())).thenReturn(testDailyReport);
        doNothing().when(mealRepository).deleteById(1L);

        mealService.deleteCurrentMeal(1L);


        verify(mealRepository).deleteById(1L);
        verify(dailyReportService).recalculateDailyReportTotals(testDailyReport);
    }

    @Test
    void getNutritionSum_ShouldReturnNutritionSum() {
        NutritionSumResponse expectedResponse = new NutritionSumResponse(500.0, 25.0, 15.0, 60.0);

        when(mealEntryService.getNutritionSumByMealId(1L))
                .thenReturn(expectedResponse);

        NutritionSumResponse result = mealService.getNutritionSum(1L);

        assertNotNull(result);
        assertEquals(500.0, result.getTotalCalories());
        assertEquals(25.0, result.getTotalProteins());
    }

    @Test
    void updateNutritionSum_ShouldRecalculateTotals() {
        when(dailyReportService.getOrCreateDailyReportById(1L))
                .thenReturn(testDailyReport);
        doNothing().when(dailyReportService).recalculateDailyReportTotals(testDailyReport);

        mealService.updateNutritionSum(1L);

        verify(dailyReportService).recalculateDailyReportTotals(testDailyReport);
    }

    @Test
    void updateMealWithProducts_WhenValidData_ShouldUpdateMeal() {
        List<String> productNames = Arrays.asList("Яблоко", "Банан");
        List<Integer> quantities = Arrays.asList(100, 150);

        when(mealRepository.findById(1L)).thenReturn(Optional.of(testMeal));
        when(mealTypeRepository.findByName("Обед"))
                .thenReturn(Optional.of(new MealType("Обед", "Второй прием пищи")));
        doNothing().when(mealEntryService).deleteByMealId(1L);
        when(mealEntryService.createMealEntries(any(Meal.class), anyList(), anyList()))
                .thenReturn(new ArrayList<>());
        when(mealRepository.save(any(Meal.class))).thenReturn(testMeal);

        Meal result = mealService.updateMealWithProducts(1L, "Обед", productNames, quantities);

        assertNotNull(result);
        verify(mealEntryService).deleteByMealId(1L);
        verify(mealEntryService).createMealEntries(any(Meal.class), eq(productNames), eq(quantities));
    }

    @Test
    void updateMealWithProducts_WhenMealNotFound_ShouldThrowException() {
        when(mealRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            mealService.updateMealWithProducts(999L, "Обед", new ArrayList<>(), new ArrayList<>());
        }, "Meal not found");
    }

    @Test
    void getMealById_WhenExists_ShouldReturnOptional() {
        when(mealRepository.findById(1L)).thenReturn(Optional.of(testMeal));

        Optional<Meal> result = mealService.getMealById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getMealById_WhenNotExists_ShouldReturnEmpty() {
        when(mealRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Meal> result = mealService.getMealById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLastMealActivityByUserId_ShouldReturnDateTime() {
        LocalDateTime expectedDateTime = LocalDateTime.now();
        when(mealRepository.findLastMealActivityByUserId(1L))
                .thenReturn(expectedDateTime);

        LocalDateTime result = mealService.getLastMealActivityByUserId(1L);

        assertEquals(expectedDateTime, result);
    }

    @Test
    void countUsersWithActivityAfter_ShouldReturnCount() {
        when(mealRepository.countUsersWithActivityAfter(any(LocalDateTime.class)))
                .thenReturn(10L);

        Long count = mealService.countUsersWithActivityAfter(LocalDateTime.now().minusDays(7));

        assertEquals(10L, count);
    }

    @Test
    void getMealsForDate_WhenReportExists_ShouldReturnMeals() {
        LocalDate date = LocalDate.now();

        when(dailyReportService.getDailyReportAuth(date))
                .thenReturn(Optional.of(testDailyReport));
        when(mealRepository.findByDailyReportId(1L))
                .thenReturn(Collections.singletonList(testMeal));

        List<Meal> meals = mealService.getMealsForDate(date);

        assertEquals(1, meals.size());
        assertEquals(testMeal, meals.get(0));
    }

    @Test
    void getMealsForDate_WhenReportNotExists_ShouldReturnEmptyList() {
        LocalDate date = LocalDate.now();

        when(dailyReportService.getDailyReportAuth(date))
                .thenReturn(Optional.empty());

        List<Meal> meals = mealService.getMealsForDate(date);

        assertNotNull(meals);
        assertTrue(meals.isEmpty());
    }

    @Test
    void getMealsForDate_WhenDateIsNull_ShouldUseCurrentDate() {
        when(dailyReportService.getDailyReportAuth(LocalDate.now()))
                .thenReturn(Optional.of(testDailyReport));
        when(mealRepository.findByDailyReportId(1L))
                .thenReturn(Collections.singletonList(testMeal));

        List<Meal> meals = mealService.getMealsForDate(null);

        assertEquals(1, meals.size());
    }
}