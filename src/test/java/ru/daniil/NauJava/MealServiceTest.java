package ru.daniil.NauJava;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.*;
import ru.daniil.NauJava.service.MealServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MealServiceTest {
    @Autowired
    private MealServiceImpl mealService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private MealEntryRepository mealEntryRepository;

    private User testUser;

    /**
     * Инициализация тестовых данных перед выполнением каждого теста.
     * Создает тестового пользователя и продукты для использования в тестах.
     */
    @BeforeEach
    void setUp() {
        cleanupTestData();

        // Создание тестового пользователя
        testUser = new User();
        testUser.setEmail("test.user@example.com");
        testUser.setPassword("password");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser = userRepository.save(testUser);

        // Создание тестовых продуктов
        Product testProduct1 = new Product();
        testProduct1.setName("Test Chicken Breast");
        testProduct1.setCaloriesPer100g(165.0);
        testProduct1.setProteinsPer100g(31.0);
        testProduct1.setFatsPer100g(3.6);
        testProduct1.setCarbsPer100g(0.0);
        testProduct1.setCreatedByUser(null);
        productRepository.save(testProduct1);

        Product testProduct2 = new Product();
        testProduct2.setName("Test Oatmeal");
        testProduct2.setCaloriesPer100g(68.0);
        testProduct2.setProteinsPer100g(2.4);
        testProduct2.setFatsPer100g(1.4);
        testProduct2.setCarbsPer100g(12.0);
        testProduct2.setCreatedByUser(null);
        productRepository.save(testProduct2);
    }

    /**
     * Удаляет записи после каждого теста
     */
    @AfterEach
    void dellData(){
        cleanupTestData();
    }

    /**
     * Тестирует успешное создание приема пищи с одним продуктом.
     * Проверяет, что метод корректно создает объект Meal и связанные с ним MealEntry.
     */
    @Test
    void createMealWithProducts_WhenSingleProduct_ShouldCreateMealAndMealEntry() {
        String userEmail = "test.user@example.com";
        String mealType = "lunch";
        List<String> productNames = List.of("Test Chicken Breast");
        List<Integer> quantities = List.of(150);

        Meal result = mealService.createMealWithProducts(userEmail, mealType, productNames, quantities);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getMealType()).isEqualTo(mealType);
        assertThat(result.getDailyReport().getUser().getEmail()).isEqualTo(userEmail);

        MealEntry mealEntry = result.getMealEntries().get(0);
        assertThat(mealEntry.getProduct().getName()).isEqualTo("Test Chicken Breast");
        assertThat(mealEntry.getQuantityGrams()).isEqualTo(150);
        assertThat(mealEntry.getCalculatedCalories()).isEqualTo(248);
    }

    /**
     * Тестирует создание приема пищи с несколькими продуктами.
     * Проверяет, что метод корректно обрабатывает несколько продуктов в одном приеме пищи.
     */
    @Test
    void createMealWithProducts_WhenMultipleProducts_ShouldCreateMealWithAllEntries() {
        String userEmail = "test.user@example.com";
        String mealType = "dinner";
        List<String> productNames = List.of("Test Chicken Breast", "Test Oatmeal");
        List<Integer> quantities = List.of(200, 100);

        Meal result = mealService.createMealWithProducts(userEmail, mealType, productNames, quantities);

        assertThat(result).isNotNull();
        Assertions.assertThat(result.getMealEntries()).hasSize(2);
        Assertions.assertThat(result.getMealEntries())
                .extracting(mealEntry -> mealEntry.getProduct().getName())
                .containsExactlyInAnyOrder("Test Chicken Breast", "Test Oatmeal");
    }

    /**
     * Тестирует создание приема пищи когда DailyReport не существует.
     * Проверяет, что метод автоматически создает DailyReport для пользователя
     * в условиях его отсутствия.
     */
    @Test
    void createMealWithProducts_WhenDailyReportNotExists_ShouldCreateDailyReport() {
        String userEmail = "test.user@example.com";
        String mealType = "breakfast";
        List<String> productNames = List.of("Test Oatmeal");
        List<Integer> quantities = List.of(100);

        Optional<DailyReport> existingReport = dailyReportRepository.findByUserIdAndReportDate(
                testUser.getId(), LocalDate.now());
        assertThat(existingReport).isEmpty();

        Meal result = mealService.createMealWithProducts(userEmail, mealType, productNames, quantities);

        assertThat(result).isNotNull();
        assertThat(result.getDailyReport()).isNotNull();

        Optional<DailyReport> createdReport = dailyReportRepository.findByUserIdAndReportDate(
                testUser.getId(), LocalDate.now());
        assertThat(createdReport).isPresent();
        assertThat(createdReport.get().getUser().getEmail()).isEqualTo(userEmail);
    }

    /**
     * Тестирует создание приема пищи когда пользователь не существует.
     * Проверяет, что метод кидает исключение и обрабатывает его,
     * а транзакция закрывается с откатом.
     */
    @Test
    void createMealWithProducts_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        String mealType = "lunch";
        List<String> productNames = List.of("Test Chicken Breast");
        List<Integer> quantities = List.of(150);

        // Сохраняем начальное состояние базы данных
        long initialMealCount = mealRepository.count();
        long initialMealEntryCount = mealEntryRepository.count();
        long initialDailyReportCount = dailyReportRepository.count();

        // Act & Assert
        assertThatThrownBy(() ->
                mealService.createMealWithProducts(nonExistentEmail, mealType, productNames, quantities)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден: " + nonExistentEmail);

        // Проверяем, что транзакция откатилась и данные не сохранились
        assertThat(mealRepository.count()).isEqualTo(initialMealCount);
        assertThat(mealEntryRepository.count()).isEqualTo(initialMealEntryCount);
        assertThat(dailyReportRepository.count()).isEqualTo(initialDailyReportCount);

        // Дополнительная проверка - убеждаемся, что не создалось никаких новых сущностей
        List<Meal> allMeals = (List<Meal>) mealRepository.findAll();
        List<DailyReport> allReports = (List<DailyReport>) dailyReportRepository.findAll();

        // Проверяем, что не появилось новых приемов пищи с тестовыми данными
        Assertions.assertThat(allMeals).noneMatch(meal ->
                meal.getMealType().equals(mealType) &&
                        meal.getDailyReport() != null &&
                        meal.getDailyReport().getUser().getEmail().equals(nonExistentEmail)
        );
    }

    /**
     * Тестирует создание приема пищи когда продукт не существует.
     * Проверяет, что метод кидает исключение и обрабатывает его,
     * а транзакция закрывается с откатом.
     */
    @Test
    void createMealWithProducts_WhenProductNotExists_ShouldThrowException() {
        String userEmail = "test.user@example.com";
        String mealType = "lunch";
        List<String> productNames = List.of("Nonexistent Product");
        List<Integer> quantities = List.of(150);

        assertThatThrownBy(() ->
                mealService.createMealWithProducts(userEmail, mealType, productNames, quantities)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Продукт не найден");
    }

    /**
     * Тестирует создание приема пищи когда количество продуктов и весов не совпадает.
     * Проверяет, что метод кидает исключение при несоответствии размеров списков
     * и обрабатывает его, а транзакция закрывается с откатом.
     */
    @Test
    void createMealWithProducts_WhenProductAndQuantityMismatch_ShouldThrowException() {
        String userEmail = "test.user@example.com";
        String mealType = "lunch";
        List<String> productNames = List.of("Test Chicken Breast", "Test Oatmeal");
        List<Integer> quantities = List.of(150);

        assertThatThrownBy(() ->
                mealService.createMealWithProducts(userEmail, mealType, productNames, quantities)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Количество продуктов и количеств не совпадает");
    }

    /**
     * Тестирует получение сегодняшних приемов пищи для пользователя.
     * Проверяет корректность работы метода getTodayMeals.
     */
    @Test
    void getTodayMeals_WhenMealsExist_ShouldReturnMeals() {
        String userEmail = "test.user@example.com";
        Optional<DailyReport> existingReport = dailyReportRepository.findByUserIdAndReportDate(
                testUser.getId(), LocalDate.now());
        assertThat(existingReport).isEmpty();

        List<String> productNames = List.of("Test Chicken Breast");
        List<Integer> quantities = List.of(150);
        mealService.createMealWithProducts(userEmail, "breakfast", productNames, quantities);

        productNames = List.of("Test Chicken Breast");
        quantities = List.of(150);
        mealService.createMealWithProducts(userEmail, "lunch", productNames, quantities);

        List<Meal> todayMeals = mealService.getTodayMeals(userEmail);

        Assertions.assertThat(todayMeals).hasSize(2);
        Assertions.assertThat(todayMeals)
                .extracting(Meal::getMealType)
                .containsExactlyInAnyOrder("breakfast", "lunch");
    }

    /**
     * Тестирует получение сегодняшних приемов пищи когда пользователь не существует.
     * Проверяет, что метод возвращает пустой список.
     */
    @Test
    void getTodayMeals_WhenUserNotExists_ShouldReturnEmptyList() {
        String nonExistentEmail = "nonexistent@example.com";

        List<Meal> result = mealService.getTodayMeals(nonExistentEmail);

        Assertions.assertThat(result).isEmpty();
    }

    /**
     * Очищает тестовые данные после каждого теста.
     */
    private void cleanupTestData() {
        mealEntryRepository.deleteAll();
        mealRepository.deleteAll();
        dailyReportRepository.deleteAll();

        List<Product> testProducts = productRepository.findByNameContainingIgnoreCase("Test");
        for (Product product : testProducts) {
            productRepository.delete(product);
        }

        Optional<User> testUser = userRepository.findByEmail("test.user@example.com");
        testUser.ifPresent(userRepository::delete);
    }
}
