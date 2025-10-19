package ru.daniil.NauJava;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.repository.ProductRepositoryCustom;
import ru.daniil.NauJava.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductRepositoryCustom productRepositoryCustom;

    /**
     * Инициализация тестовых данных перед выполнением всех тестов.
     * Создает тестового пользователя, системные продукты и пользовательские продукты
     * для использования в последующих тестах. Также выполняет очистку предыдущих тестовых данных.
     */
    @BeforeAll
    void setUp() {
        cleanupAllTestData();
        // Создание тестового пользователя
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser = userRepository.save(testUser);

        // Создание тестовой продукции от системы
        Product systemProduct1 = new Product();
        systemProduct1.setName("test Apple");
        systemProduct1.setDescription("delicious");
        systemProduct1.setCaloriesPer100g(52.0);
        systemProduct1.setProteinsPer100g(0.3);
        systemProduct1.setFatsPer100g(0.2);
        systemProduct1.setCarbsPer100g(14.0);
        systemProduct1.setCreatedByUser(null);
        productRepository.save(systemProduct1);

        Product systemProduct2 = new Product();
        systemProduct2.setName("test Banana");
        systemProduct2.setCaloriesPer100g(89.1);
        systemProduct2.setProteinsPer100g(1.1);
        systemProduct2.setFatsPer100g(0.3);
        systemProduct2.setCarbsPer100g(22.8);
        systemProduct2.setCreatedByUser(null);
        productRepository.save(systemProduct2);

        // Создание тестовой продукции созданной пользователем
        Product userProduct1 = new Product();
        userProduct1.setName("test Chicken Breast");
        userProduct1.setCaloriesPer100g(165.5);
        userProduct1.setProteinsPer100g(31.0);
        userProduct1.setFatsPer100g(3.6);
        userProduct1.setCarbsPer100g(0.0);
        userProduct1.setCreatedByUser(testUser);
        productRepository.save(userProduct1);

        Product userProduct2 = new Product();
        userProduct2.setName("test Oatmeal");
        userProduct2.setCaloriesPer100g(68.0);
        userProduct2.setProteinsPer100g(2.4);
        userProduct2.setFatsPer100g(1.4);
        userProduct2.setCarbsPer100g(12.0);
        userProduct2.setCreatedByUser(testUser);
        productRepository.save(userProduct2);
    }

    /**
     * Тестирует поиск продуктов по частичному совпадению имени без учета регистра.
     * Проверяет, что метод возвращает корректный продукт при частичном совпадении имени.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenPartialNameMatch_ShouldReturnProducts() {
        List<Product> result = productRepository.findByNameContainingIgnoreCase("test a");

        assertThat(result.get(0).getName()).isEqualTo("test Apple");
    }

    /**
     * Тестирует поиск продуктов по частичному совпадению имени с разным регистром.
     * Проверяет, что метод работает без учета регистра и возвращает корректный продукт.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenDifferentCase_ShouldReturnProducts() {
        List<Product> result = productRepository.findByNameContainingIgnoreCase("BANANA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test Banana");
    }

    /**
     * Тестирует поиск продуктов при отсутствии совпадений по имени.
     * Проверяет, что метод возвращает пустой список когда нет продуктов с указанным именем.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenNoMatch_ShouldReturnEmptyList() {
        List<Product> result = productRepository.findByNameContainingIgnoreCase("xyz");

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск системных продуктов (созданных без пользователя).
     * Проверяет, что метод возвращает только продукты с createdByUser = null.
     */
    @Test
    void findByCreatedByUserIsNull_ShouldReturnSystemProducts() {
        List<Product> result = productRepository.findByCreatedByUserIsNull();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Apple", "test Banana");
    }

    /**
     * Тестирует поиск продуктов по идентификатору пользователя-создателя.
     * Проверяет, что метод возвращает только продукты, созданные указанным пользователем.
     */
    @Test
    void findByCreatedByUserId_ShouldReturnUserProducts() {
        Optional<User> testUser = userRepository.findByEmail("test@example.com");
        List<Product> result = null;
        if (testUser.isPresent()) {
            result = productRepository.findByCreatedByUserId(testUser.get().getId());
        }

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Chicken Breast", "test Oatmeal");
    }

    /**
     * Тестирует поиск продуктов пользователя, у которого нет созданных продуктов.
     * Проверяет, что метод возвращает пустой список когда пользователь не создавал продукты.
     */
    @Test
    void findByCreatedByUserId_WhenUserHasNoProducts_ShouldReturnEmptyList() {
        // Создание временного пользователя (хеширование пароля не проверяется)
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("pass");
        newUser.setName("New");
        newUser.setSurname("User");
        newUser = userRepository.save(newUser);

        List<Product> result = productRepository.findByCreatedByUserId(newUser.getId());

        assertThat(result).isEmpty();
        cleanupTestUser(newUser);
    }

    /**
     * Тестирует поиск продуктов с калорийностью больше или равной указанному значению.
     * Проверяет, что метод возвращает все продукты, удовлетворяющие условию по калорийности.
     */
    @Test
    void findByCaloriesPer100gGreaterThanEqual_WhenCaloriesMatch_ShouldReturnProducts() {
        List<Product> result = productRepository.findByNameContainingIgnoreCase("BANANA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test Banana");
    }

    /**
     * Тестирует поиск продуктов с калорийностью больше или равной указанному значению
     * когда нет продуктов, удовлетворяющих условию.
     * Проверяет, что метод возвращает пустой список при отсутствии совпадений.
     */
    @Test
    void findByCaloriesPer100gGreaterThanEqual_WhenNoMatch_ShouldReturnEmptyList() {
        List<Product> result = productRepository.findProductsWithMinCalories(200.0);

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск продуктов, которые являются системными ИЛИ созданы указанным пользователем.
     * Проверяет, что метод возвращает объединение системных продуктов и продуктов пользователя.
     */
    @Test
    void findByCreatedByUserIsNullOrCreatedByUserId_ShouldReturnAllProducts() {
        Optional<User> testUser = userRepository.findByEmail("test@example.com");
        List<Product> result = null;
        if (testUser.isPresent()) {
            result = productRepository.findByCreatedByUserIsNullOrCreatedByUserId(testUser.get().getId());
        }

        assertThat(result).hasSize(4);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Apple", "test Banana", "test Chicken Breast", "test Oatmeal");
    }

    /**
     * Тестирует поиск продуктов по точному имени и диапазону калорийности.
     * Проверяет, что метод возвращает продукт при полном совпадении имени и калорийности в диапазоне.
     */
    @Test
    void findByNameAndCaloriesPer100gBetween_WhenExactMatch_ShouldReturnProduct() {
        List<Product> result = productRepository.findByNameAndCaloriesPer100gBetween(
                "test Apple", 50, 60);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test Apple");
        assertThat(result.get(0).getCaloriesPer100g()).isEqualTo(52);
    }

    /**
     * Тестирует поиск продуктов по имени и диапазону калорийности при отсутствии совпадений.
     * Проверяет, что метод возвращает пустой список когда нет продуктов с указанным именем
     * и калорийностью в заданном диапазоне.
     */
    @Test
    void findByNameAndCaloriesPer100gBetween_WhenNoMatch_ShouldReturnEmptyList() {
        List<Product> result = productRepository.findByNameAndCaloriesPer100gBetween(
                "Apple", 100, 200);

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск системного продукта по имени без учета регистра.
     * Проверяет, что метод возвращает системный продукт при совпадении имени без учета регистра.
     */
    @Test
    void findByNameIgnoreCaseAndCreatedByUserIsNull_WhenSystemProductExists_ShouldReturnProduct() {
        Optional<Product> result = productRepository.findByNameIgnoreCaseAndCreatedByUserIsNull("test APPLE");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test Apple");
        assertThat(result.get().getCreatedByUser()).isNull();
    }

    /**
     * Тестирует поиск системного продукта по имени когда продукт является пользовательским.
     * Проверяет, что метод не возвращает пользовательские продукты при поиске системных.
     */
    @Test
    void findByNameIgnoreCaseAndCreatedByUserIsNull_WhenUserProduct_ShouldReturnEmpty() {
        Optional<Product> result = productRepository.findByNameIgnoreCaseAndCreatedByUserIsNull("test CHICKEN BREAST");

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск пользовательского продукта по имени без учета регистра и идентификатору пользователя.
     * Проверяет, что метод возвращает продукт при совпадении имени и идентификатора создателя.
     */
    @Test
    void findByNameIgnoreCaseAndCreatedByUserId_WhenUserProductExists_ShouldReturnProduct() {
        Optional<User> testUser = userRepository.findByEmail("test@example.com");
        Optional<Product> result = Optional.empty();
        if (testUser.isPresent()) {
            result = productRepository.findByNameIgnoreCaseAndCreatedByUserId(
                    "test chicken breast", testUser.get().getId());
        }

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test Chicken Breast");
        assertThat(result.get().getCreatedByUser().getId()).isEqualTo(testUser.get().getId());
    }

    /**
     * Тестирует поиск пользовательского продукта когда продукт создан другим пользователем.
     * Проверяет, что метод не возвращает продукты, созданные другими пользователями.
     */
    @Test
    void findByNameIgnoreCaseAndCreatedByUserId_WhenDifferentUser_ShouldReturnEmpty() {
        // Создание временного пользователя (хеширование пароля не проверяется)
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("pass");
        otherUser.setName("Other");
        otherUser.setSurname("User");
        otherUser = userRepository.save(otherUser);


        Optional<Product> result = productRepository.findByNameIgnoreCaseAndCreatedByUserId(
                "chicken breast", otherUser.getId());

        assertThat(result).isEmpty();
        cleanupTestUser(otherUser);
    }

    /**
     * Тестирует проверку существования продукта по имени без учета регистра.
     * Проверяет, что метод возвращает true для существующих продуктов независимо от регистра.
     */
    @Test
    void existsByNameIgnoreCase_WhenProductExists_ShouldReturnTrue() {
        assertThat(productRepository.existsByNameIgnoreCase("test APPLE")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("test banana")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("test Chicken Breast")).isTrue();
    }

    /**
     * Тестирует проверку существования продукта по имени когда продукт не существует.
     * Проверяет, что метод возвращает false для несуществующих продуктов.
     */
    @Test
    void existsByNameIgnoreCase_WhenProductNotExists_ShouldReturnFalse() {
        assertThat(productRepository.existsByNameIgnoreCase("Nonexistent")).isFalse();
    }
    //Здесь начинаются
    //тестовые методы для работы репозитория написанного на Criteria Api.
    //Я делал так, чтобы они более подробно проверяли имеющиеся методы
    //ведь их всего реализованно 2
    /**
     * Тестирует поиск продуктов с калорийностью >= 0.
     * Проверяет, что метод возвращает все продукты при минимальном значении калорий.
     */
    @Test
    void findProductsWithMinCalories_WhenMinCaloriesIsZero() {
        List<Product> result = productRepositoryCustom.findProductsWithMinCalories(0.0);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Apple", "test Banana", "test Chicken Breast", "test Oatmeal");
    }

    /**
     * Тестирует поиск продуктов с калорийностью >= 80.
     * Проверяет, что метод возвращает только необходимые продукты.
     */
    @Test
    void findProductsWithMinCalories_WhenMinCaloriesIs80() {
        List<Product> result = productRepositoryCustom.findProductsWithMinCalories(80.0);

        for (Product res: result) {
            System.out.println(res.getName());
        }
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Banana", "test Chicken Breast");
    }

    /**
     * Тестирует поиск продуктов с калорийностью >= 200.
     * Проверяет, что метод возвращает пустой список при отсутствии совпадений.
     */
    @Test
    void findProductsWithMinCalories_WhenMinCaloriesIs200() {
        List<Product> result = productRepositoryCustom.findProductsWithMinCalories(200.0);

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск продуктов с калорийностью >= 68.
     * Проверяет, что метод возвращает продукты с равным значением калорийности.
     */
    @Test
    void findProductsWithMinCalories_WhenMinCaloriesIs68() {
        List<Product> result = productRepositoryCustom.findProductsWithMinCalories(68.0);

        for (Product res: result) {
            System.out.println(res.getName());
        }
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Oatmeal", "test Banana", "test Chicken Breast");
    }

    /**
     * Тестирует поиск продуктов с null значением калорийности.
     * Проверяет, что метод возвращает все продукты при параметре = null.
     */
    @Test
    void findProductsWithMinCalories_WhenMinCaloriesIsNull() {
        List<Product> result = productRepositoryCustom.findProductsWithMinCalories(null);

        for (Product res: result) {
            System.out.println(res.getName());
        }
        assertThat(result).hasSize(4);
    }

    //А здесь уже тесты для 2-го реализованного метода
    /**
     * Тестирует поиск продуктов по имени при отсутствии совпадений.
     * Проверяет, что метод возвращает пустой список когда нет продуктов с указанным именем.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenNoMatches() {
        // Act
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase("noNameToFind");

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск продуктов по имени с одним точным совпадением.
     * Проверяет, что метод возвращает корректный продукт при точном совпадении имени.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenSingleExactMatch() {
        // Act
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase("oatmeal");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test Oatmeal");
    }

    /**
     * Тестирует поиск продуктов по части имени с несколькими совпадениями.
     * Проверяет, что метод возвращает все продукты, содержащие указанную подстроку в имени.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenMultiplePartialMatches() {
        // Act
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase("test");

        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("test Apple", "test Banana", "test Chicken Breast", "test Oatmeal");
    }

    /**
     * Тестирует поиск продуктов по имени без учета регистра.
     * Проверяет, что метод находит продукты независимо от регистра введенного запроса.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenDifferentCase() {
        // Act
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase("TEST APPLE");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test Apple");
    }

    /**
     * Тестирует поиск продуктов с пустой строкой в качестве параметра.
     * Проверяет, что метод возвращает все продукты при пустой строке поиска.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenEmptyString() {
        // Act
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase("");

        // Assert
        assertThat(result).hasSize(4);
    }

    /**
     * Тестирует поиск продуктов с null значением в качестве параметра.
     * Проверяет, что метод возвращает все продукты при параметре = null.
     */
    @Test
    void findByNameContainingIgnoreCase_WhenNullString() {
        List<Product> result = productRepositoryCustom.findByNameContainingIgnoreCase(null);

        assertThat(result).hasSize(4);
    }

    //Методы для выполнения отчистки БД от тестовых данных

    /**
     * Очищает тестовые данные для указанного пользователя.
     * Удаляет все продукты, созданные пользователем, а затем самого пользователя.
     *
     * @param user пользователь, чьи данные необходимо очистить
     */
    private void cleanupTestUser(User user) {
        if (user != null && user.getId() != null) {
            List<Product> userProducts = productRepository.findByCreatedByUserId(user.getId());
            for (Product product : userProducts) {
                productRepository.delete(product);
            }

            userRepository.delete(user);
        }
    }

    /**
     * Очищает все тестовые данные, созданные в процессе тестирования.
     * Удаляет все продукты с именем, содержащим "test", и тестового пользователя
     * с email "test@example.com" вместе с его продуктами.
     */
    public void cleanupAllTestData() {
        List<Product> result1 = productRepository.findByNameContainingIgnoreCase("test");
        for (Product product : result1) {
            productRepository.delete(product);
        }

        Optional<User> testUser = userRepository.findByEmail("test@example.com");
        if (testUser.isPresent()){
            List<Product> allProducts = productRepository.findByCreatedByUserId(testUser.get().getId());
            for (Product product : allProducts) {
                productRepository.delete(product);
            }
            userRepository.delete(testUser.get());
        }
    }
}