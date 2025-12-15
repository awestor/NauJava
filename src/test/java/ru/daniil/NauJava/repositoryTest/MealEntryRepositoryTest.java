package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MealEntryRepositoryTest {

    @Autowired
    private MealEntryRepository mealEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private MealTypeRepository mealTypeRepository;

    @Autowired
    private MealRepository mealRepository;

    private Product testProduct;
    private DailyReport dailyReport;
    private MealType mealType;
    private Meal testMeal;
    private MealEntry mealEntry;

    @BeforeEach
    void setUp() {
        mealEntryRepository.deleteAll();
        mealRepository.deleteAll();
        dailyReportRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        mealTypeRepository.deleteAll();

        User testUser = new User("test@example.com", "testUser", "password123");
        userRepository.save(testUser);

        testProduct = new Product("Apple", 52.0,
                0.3, 0.2, 14.0);
        testProduct.setCreatedByUser(null);
        productRepository.save(testProduct);

        mealType = new MealType("Breakfast", "Утренний прием пищи");
        mealTypeRepository.save(mealType);

        dailyReport = new DailyReport(testUser, java.time.LocalDate.now());
        dailyReportRepository.save(dailyReport);

        testMeal = new Meal(dailyReport, mealType);
        mealRepository.save(testMeal);

        mealEntry = new MealEntry(testMeal, testProduct, 200);
        mealEntryRepository.save(mealEntry);
    }

    @Test
    void findByMealId_WhenMealEntriesExist_ShouldReturnList() {
        List<MealEntry> entries = mealEntryRepository.findByMealId(testMeal.getId());

        assertThat(entries).isNotEmpty();
        assertThat(entries).extracting(MealEntry::getMeal)
                .extracting(Meal::getId)
                .containsOnly(testMeal.getId());
    }

    @Test
    void findByMealId_WhenNoMealEntries_ShouldReturnEmpty() {
        List<MealEntry> entries = mealEntryRepository.findByMealId(999L);

        assertThat(entries).isEmpty();
    }

    @Test
    void findNutritionSumByMealId_ShouldCalculateCorrectly() {
        List<Object[]> results = mealEntryRepository.findNutritionSumByMealId(testMeal.getId());

        assertThat(results).isNotEmpty();
        Object[] nutrition = results.get(0);

        assertThat((Number) nutrition[0]).isEqualTo(104.0);
        assertThat((Number) nutrition[1]).isEqualTo(0.6);
        assertThat((Number) nutrition[2]).isEqualTo(0.4);
        assertThat((Number) nutrition[3]).isEqualTo(28.0);
    }

    @Test
    void findNutritionSumByMealId_WhenNoEntries_ShouldReturnZeros() {
        Meal emptyMeal = new Meal(dailyReport, mealType);
        mealRepository.save(emptyMeal);

        List<Object[]> results = mealEntryRepository.findNutritionSumByMealId(emptyMeal.getId());

        assertThat(results).isEmpty();
    }

    @Test
    void findNutritionSumByDailyReportId_ShouldCalculateCorrectly() {
        Product banana = new Product("Banana", 89.0, 1.1,
                0.3, 23.0);
        productRepository.save(banana);

        MealEntry secondEntry = new MealEntry(testMeal, banana, 150);
        mealEntryRepository.save(secondEntry);

        List<Object[]> results = mealEntryRepository.findNutritionSumByDailyReportId(dailyReport.getId());

        assertThat(results).isNotEmpty();
        Object[] nutrition = results.get(0);

        assertThat((Number) nutrition[0]).isEqualTo(237.5);
    }

    @Test
    void deleteByMealId_ShouldRemoveEntries() {
        mealEntryRepository.deleteByMealId(testMeal.getId());

        List<MealEntry> entries = mealEntryRepository.findByMealId(testMeal.getId());
        assertThat(entries).isEmpty();
    }

    @Test
    void saveMealEntry_WithNegativeQuantity_ShouldSucceed() {
        MealEntry entry = new MealEntry(testMeal, testProduct, -100);

        assertThatCode(() -> mealEntryRepository.save(entry))
                .doesNotThrowAnyException();
    }

    @Test
    void updateMealEntry_ShouldPersistChanges() {
        mealEntry.updateQuantity(300);
        mealEntryRepository.save(mealEntry);

        MealEntry updatedEntry = mealEntryRepository.findById(mealEntry.getId()).orElseThrow();
        assertThat(updatedEntry.getQuantityGrams()).isEqualTo(300);
        assertThat(updatedEntry.getCalculatedCalories()).isEqualTo(156);
    }

    @Test
    void deleteMealEntry_ShouldRemoveEntry() {
        Long entryId = mealEntry.getId();
        mealEntryRepository.delete(mealEntry);

        boolean exists = mealEntryRepository.existsById(entryId);
        assertThat(exists).isFalse();
    }

    @Test
    void createMultipleEntriesForSameMeal_ShouldSucceed() {
        Product banana = new Product("Banana", 89.0,
                1.1, 0.3, 23.0);
        productRepository.save(banana);

        MealEntry secondEntry = new MealEntry(testMeal, banana, 150);
        mealEntryRepository.save(secondEntry);

        List<MealEntry> entries = mealEntryRepository.findByMealId(testMeal.getId());
        assertThat(entries).hasSize(2);
    }
}
