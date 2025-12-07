package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.response.NutritionSumResponse;
import ru.daniil.NauJava.service.MealEntryServiceImpl;
import ru.daniil.NauJava.service.ProductService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealEntryServiceTest {

    @Mock
    private MealEntryRepository mealEntryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private MealEntryServiceImpl mealEntryService;

    private Meal testMeal;
    private Product testProduct1;
    private Product testProduct2;
    private MealEntry testMealEntry1;
    private MealEntry testMealEntry2;

    @BeforeEach
    void setUp() {
        testMeal = new Meal();
        testMeal.setId(1L);

        testProduct1 = new Product("Яблоко", "Свежие яблоки", 52.0, 0.3, 0.2, 14.0);
        testProduct1.setId(1L);

        testProduct2 = new Product("Банан", "Свежий банан", 89.0, 1.1, 0.3, 22.8);
        testProduct2.setId(2L);

        testMealEntry1 = new MealEntry(testMeal, testProduct1, 100);
        testMealEntry1.setId(1L);

        testMealEntry2 = new MealEntry(testMeal, testProduct2, 150);
        testMealEntry2.setId(2L);
    }

    @Test
    void createMealEntries_WhenValidData_ShouldCreateEntries() {
        List<String> productNames = Arrays.asList("Яблоко", "Банан");
        List<Integer> quantities = Arrays.asList(100, 150);

        when(productService.findProductByName("Яблоко")).thenReturn(testProduct1);
        when(productService.findProductByName("Банан")).thenReturn(testProduct2);
        when(mealEntryRepository.save(any(MealEntry.class)))
                .thenReturn(testMealEntry1)
                .thenReturn(testMealEntry2);

        List<MealEntry> mealEntries = mealEntryService.createMealEntries(testMeal, productNames, quantities);

        assertNotNull(mealEntries);
        assertEquals(2, mealEntries.size());
        verify(productService, times(2)).findProductByName(anyString());
        verify(mealEntryRepository, times(2)).save(any(MealEntry.class));
    }

    @Test
    void createMealEntries_WhenDifferentSizes_ShouldThrowException() {
        List<String> productNames = Arrays.asList("Яблоко", "Банан");
        List<Integer> quantities = List.of(100);

        assertThrows(IllegalArgumentException.class, () -> {
            mealEntryService.createMealEntries(testMeal, productNames, quantities);
        }, "Количество продуктов и весов не совпадает");
    }

    @Test
    void createMealEntries_WhenProductNotFound_ShouldThrowException() {
        List<String> productNames = List.of("Несуществующий");
        List<Integer> quantities = List.of(100);

        when(productService.findProductByName("Несуществующий")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            mealEntryService.createMealEntries(testMeal, productNames, quantities);
        }, "Продукт из списка потреблённых не найден: Несуществующий");
    }

    @Test
    void createMealEntry_ShouldSaveAndReturnEntry() {
        when(mealEntryRepository.save(any(MealEntry.class))).thenReturn(testMealEntry1);

        MealEntry result = mealEntryService.createMealEntry(testMeal, testProduct1, 100);

        assertNotNull(result);
        assertEquals(testMeal, result.getMeal());
        assertEquals(testProduct1, result.getProduct());
        assertEquals(100, result.getQuantityGrams());
        verify(mealEntryRepository).save(any(MealEntry.class));
    }

    @Test
    void getNutritionSumByMealId_WhenValidData_ShouldReturnNutritionSum() {
        List<Object[]> rowData = Collections.singletonList(new Object[]{500.0, 25.0, 15.0, 60.0});
        NutritionSumResponse resultList = new NutritionSumResponse
                (500.0, 25.0, 15.0, 60.0);

        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(rowData);

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(resultList.getTotalCalories(), response.getTotalCalories());
        assertEquals(resultList.getTotalProteins(), response.getTotalProteins());
        assertEquals(resultList.getTotalFats(), response.getTotalFats());
        assertEquals(resultList.getTotalCarbs(), response.getTotalCarbs());
    }

    @Test
    void getNutritionSumByMealId_WhenEmptyResult_ShouldReturnZeroSum() {
        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(Collections.emptyList());

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories());
        assertEquals(0.0, response.getTotalProteins());
        assertEquals(0.0, response.getTotalFats());
        assertEquals(0.0, response.getTotalCarbs());
    }

    @Test
    void getNutritionSumByMealId_WhenNullResult_ShouldReturnZeroSum() {
        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(null);

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories());
        assertEquals(0.0, response.getTotalProteins());
        assertEquals(0.0, response.getTotalFats());
        assertEquals(0.0, response.getTotalCarbs());
    }

    @Test
    void getNutritionSumByMealId_WhenNullRow_ShouldReturnZeroSum() {
        List<Object[]> resultList = Collections.singletonList(new Object[]{null});

        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(resultList);

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories());
    }

    @Test
    void getNutritionSumByMealId_WhenInsufficientColumns_ShouldReturnZeroSum() {
        List<Object[]> resultList = Collections.singletonList(new Object[]{500.0, 25.0});

        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(resultList);

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories());
    }

    @Test
    void getNutritionSumByDailyReportId_WhenValidData_ShouldReturnNutritionSum() {
        List<Object[]> resultList = Collections.singletonList(new Object[]{2000.0, 80.0, 60.0, 250.0});

        when(mealEntryRepository.findNutritionSumByDailyReportId(1L))
                .thenReturn(resultList);

        NutritionSumResponse response = mealEntryService.getNutritionSumByDailyReportId(1L);

        assertNotNull(response);
        assertEquals(2000.0, response.getTotalCalories());
        assertEquals(80.0, response.getTotalProteins());
        assertEquals(60.0, response.getTotalFats());
        assertEquals(250.0, response.getTotalCarbs());
    }

    @Test
    void getNutritionSumByDailyReportId_WhenEmptyResult_ShouldReturnZeroSum() {
        when(mealEntryRepository.findNutritionSumByDailyReportId(1L))
                .thenReturn(Collections.emptyList());

        NutritionSumResponse response = mealEntryService.getNutritionSumByDailyReportId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories());
    }

    @Test
    void getAllByMealId_ShouldReturnMealEntries() {
        when(mealEntryRepository.findByMealId(1L))
                .thenReturn(Arrays.asList(testMealEntry1, testMealEntry2));

        List<MealEntry> entries = mealEntryService.getAllByMealId(1L);

        assertEquals(2, entries.size());
        assertTrue(entries.contains(testMealEntry1));
        assertTrue(entries.contains(testMealEntry2));
    }

    @Test
    void deleteByMealId_ShouldCallRepository() {
        doNothing().when(mealEntryRepository).deleteByMealId(1L);

        mealEntryService.deleteByMealId(1L);

        verify(mealEntryRepository).deleteByMealId(1L);
    }

    @Test
    void getNutritionSumByMealId_WhenNullValuesInRow_ShouldHandleGracefully() {
        Object[] rowData = new Object[]{null, 25.0, null, 60.0};
        List<Object[]> resultList = Collections.singletonList(new Object[]{null, 25.0, null, 60.0});

        when(mealEntryRepository.findNutritionSumByMealId(1L))
                .thenReturn(resultList);

        NutritionSumResponse response = mealEntryService.getNutritionSumByMealId(1L);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCalories()); // null преобразуется в 0.0
        assertEquals(25.0, response.getTotalProteins());
        assertEquals(0.0, response.getTotalFats()); // null преобразуется в 0.0
        assertEquals(60.0, response.getTotalCarbs());
    }

    @Test
    void createMealEntries_WithDuplicateProductNames_ShouldCreateSeparateEntries() {
        List<String> productNames = Arrays.asList("Яблоко", "Яблоко");
        List<Integer> quantities = Arrays.asList(100, 50);

        when(productService.findProductByName("Яблоко"))
                .thenReturn(testProduct1)
                .thenReturn(testProduct1);
        when(mealEntryRepository.save(any(MealEntry.class)))
                .thenReturn(testMealEntry1)
                .thenReturn(testMealEntry2);

        List<MealEntry> mealEntries = mealEntryService.createMealEntries(testMeal, productNames, quantities);

        assertEquals(2, mealEntries.size());
        verify(productService, times(2)).findProductByName("Яблоко");
        verify(mealEntryRepository, times(2)).save(any(MealEntry.class));
    }
}
