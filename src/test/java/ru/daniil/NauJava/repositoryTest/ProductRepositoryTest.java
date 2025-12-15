package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product userProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("test@example.com", "testuser", "password123");
        userRepository.save(testUser);

        Product globalProduct = new Product(
                "Global Apple",
                52.0,
                0.3,
                0.2,
                14.0
        );
        globalProduct.setCreatedByUser(null);
        productRepository.save(globalProduct);

        userProduct = new Product(
                "User Banana",
                89.0,
                1.1,
                0.3,
                23.0
        );
        userProduct.setCreatedByUser(testUser);
        productRepository.save(userProduct);
    }

    @Test
    void findByNameContainingIgnoreCase_WhenMatches_ShouldReturnProducts() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("apple");

        assertThat(products).isNotEmpty();
        assertThat(products).extracting(Product::getName)
                .contains("Global Apple");
    }

    @Test
    void findByNameContainingIgnoreCase_WhenNoMatches_ShouldReturnEmpty() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("orange");

        assertThat(products).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_WhenCaseInsensitive_ShouldReturnProducts() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("APPLE");

        assertThat(products).isNotEmpty();
    }

    @Test
    void findByCreatedByUserIsNull_ShouldReturnGlobalProducts() {
        List<Product> products = productRepository.findByCreatedByUserIsNull();

        assertThat(products).isNotEmpty();
        assertThat(products).extracting(Product::getCreatedByUser)
                .containsOnlyNulls();
    }

    @Test
    void findByCreatedByUserId_ShouldReturnUserProducts() {
        List<Product> products = productRepository.findByCreatedByUserId(testUser.getId());

        assertThat(products).isNotEmpty();
        assertThat(products).extracting(p -> p.getCreatedByUser().getId())
                .containsOnly(testUser.getId());
    }

    @Test
    void findProductsWithMinCalories_ShouldReturnFilteredProducts() {
        List<Product> products = productRepository.findProductsWithMinCalories(80.0);

        assertThat(products).isNotEmpty();
        assertThat(products).extracting(Product::getCaloriesPer100g)
                .allMatch(calories -> calories >= 80.0);
    }

    @Test
    void findProductsWithMinCaloriesAndUser_ShouldReturnFilteredProducts() {
        List<Product> products = productRepository.findProductsWithMinCaloriesAndUser(
                50.0,
                testUser.getId()
        );

        assertThat(products).isNotEmpty();
        products.forEach(p -> {
            assertThat(p.getCaloriesPer100g()).isGreaterThanOrEqualTo(50.0);
            if (p.getCreatedByUser() != null) {
                assertThat(p.getCreatedByUser().getId()).isEqualTo(testUser.getId());
            }
        });
    }

    @Test
    void findByCreatedByUserIsNullOrCreatedByUserId_ShouldReturnBothTypes() {
        List<Product> products = productRepository.findByCreatedByUserIsNullOrCreatedByUserId(testUser.getId());

        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(p -> p.getCreatedByUser() != null ?
                        p.getCreatedByUser().getId() : null)
                .containsExactlyInAnyOrder(null, testUser.getId());
    }

    @Test
    void findByNameAndCaloriesPer100gBetween_ShouldReturnFilteredProducts() {
        Product testProduct = new Product("Test Product", 100.0, 10.0,
                5.0, 15.0);
        testProduct.setCreatedByUser(null);
        productRepository.save(testProduct);

        List<Product> products = productRepository.findByNameAndCaloriesPer100gBetween(
                "Test Product",
                80,
                120
        );

        assertThat(products).isNotEmpty();
        assertThat(products).extracting(Product::getName)
                .contains("Test Product");
    }

    @Test
    void findByNameIgnoreCaseAndCreatedByUserIsNull_WhenExists_ShouldReturnProduct() {
        Optional<Product> product = productRepository.findByNameIgnoreCaseAndCreatedByUserIsNull("GLOBAL APPLE");

        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Global Apple");
    }

    @Test
    void findByNameIgnoreCaseAndCreatedByUserIsNull_WhenNotExists_ShouldReturnEmpty() {
        Optional<Product> product = productRepository.findByNameIgnoreCaseAndCreatedByUserIsNull("NONEXISTENT");

        assertThat(product).isEmpty();
    }

    @Test
    void findByNameIgnoreCaseAndCreatedByUserId_WhenExists_ShouldReturnProduct() {
        Optional<Product> product = productRepository.findByNameIgnoreCaseAndCreatedByUserId(
                "USER BANANA", testUser.getId());

        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("User Banana");
    }

    @Test
    void findByNameIgnoreCaseAndCreatedByUserId_WhenWrongUser_ShouldReturnEmpty() {
        Optional<Product> product = productRepository.findByNameIgnoreCaseAndCreatedByUserId(
                "USER BANANA", 999L);

        assertThat(product).isEmpty();
    }

    @Test
    void countByCreatedAtBetween_ShouldCountCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);

        Long count = productRepository.countByCreatedAtBetween(start, end);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByCreatedAtBetween_WhenNoProducts_ShouldReturnZero() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(20);

        Long count = productRepository.countByCreatedAtBetween(start, end);

        assertThat(count).isZero();
    }

    @Test
    void existsByNameIgnoreCase_WhenExists_ShouldReturnTrue() {
        boolean exists = productRepository.existsByNameIgnoreCase("GLOBAL APPLE");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_WhenNotExists_ShouldReturnFalse() {
        boolean exists = productRepository.existsByNameIgnoreCase("NONEXISTENT PRODUCT");

        assertThat(exists).isFalse();
    }

    @Test
    void saveProduct_WithDuplicateNameForSameUser_ShouldThrowException() {
        Product duplicateProduct = new Product(
                "User Banana",
                100.0,
                10.0,
                5.0,
                15.0
        );
        duplicateProduct.setCreatedByUser(testUser);

        assertThatThrownBy(() -> productRepository.save(duplicateProduct))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveProduct_WithSameNameForDifferentUsers_ShouldSucceed() {
        User anotherUser = new User("another@example.com", "anotheruser", "password");
        userRepository.save(anotherUser);

        Product sameNameProduct = new Product(
                "User Banana",
                100.0,
                10.0,
                5.0,
                15.0
        );
        sameNameProduct.setCreatedByUser(anotherUser);

        assertThatCode(() -> productRepository.save(sameNameProduct))
                .doesNotThrowAnyException();
    }

    @Test
    void saveProduct_WithNullName_ShouldThrowException() {
        Product nullNameProduct = new Product(
                null,
                100.0,
                10.0,
                5.0,
                15.0
        );

        assertThatThrownBy(() -> productRepository.save(nullNameProduct))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveProduct_WithNullCalories_ShouldThrowException() {
        Product nullCaloriesProduct = new Product(
                "Test Product",
                null,
                10.0,
                5.0,
                15.0
        );

        assertThatThrownBy(() -> productRepository.save(nullCaloriesProduct))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateProduct_ShouldPersistChanges() {
        userProduct.setName("Updated Banana");
        userProduct.setCaloriesPer100g(95.0);
        productRepository.save(userProduct);

        Optional<Product> updatedProduct = productRepository.findById(userProduct.getId());
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getName()).isEqualTo("Updated Banana");
        assertThat(updatedProduct.get().getCaloriesPer100g()).isEqualTo(95.0);
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() {
        Long productId = userProduct.getId();
        productRepository.delete(userProduct);

        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }
}
