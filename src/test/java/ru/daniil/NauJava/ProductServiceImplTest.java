package ru.daniil.NauJava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.service.ProductServiceImpl;
import ru.daniil.NauJava.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testUser;
    private Product systemProduct;
    private Product userProduct;

    /**
     * Инициализация тестовых данных перед выполнением каждого теста.
     * Создает тестового пользователя, системный и пользовательский продукты
     * для использования в последующих тестах.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setLogin("testuser");
        testUser.setPassword("password");

        systemProduct = new Product();
        systemProduct.setId(1L);
        systemProduct.setName("Apple");
        systemProduct.setDescription("Fresh apple");
        systemProduct.setCaloriesPer100g(52.0);
        systemProduct.setProteinsPer100g(0.3);
        systemProduct.setFatsPer100g(0.2);
        systemProduct.setCarbsPer100g(14.0);
        systemProduct.setCreatedByUser(null);

        userProduct = new Product();
        userProduct.setId(2L);
        userProduct.setName("Chicken Breast");
        userProduct.setCaloriesPer100g(165.0);
        userProduct.setProteinsPer100g(31.0);
        userProduct.setFatsPer100g(3.6);
        userProduct.setCarbsPer100g(0.0);
        userProduct.setCreatedByUser(testUser);
    }

    /**
     * Тестирует получение всех продуктов системы и пользователя
     * в том случае если это авторизованный пользователь.
     */
    @Test
    void getAll_WhenUserAuthenticated_ShouldReturnProducts() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        List<Product> expectedProducts = List.of(systemProduct, userProduct);
        when(productRepository.findByCreatedByUserIsNullOrCreatedByUserId(testUser.getId()))
                .thenReturn(expectedProducts);

        List<Product> result = productService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(systemProduct));
        assertTrue(result.contains(userProduct));

        verify(userService, times(1)).getAuthUser();
        verify(productRepository, times(1))
                .findByCreatedByUserIsNullOrCreatedByUserId(testUser.getId());
    }

    /**
     * Тестирует получение всех продуктов системы и пользователя
     * в том случае запрос поступил от пользователя, что не авторизован
     * или произошла ошибка и его данные авторизации небыли найдены.
     */
    @Test
    void getAll_WhenUserNotAuthenticated_ShouldThrowException() {
        when(userService.getAuthUser()).thenReturn(Optional.empty());

        AuthenticationCredentialsNotFoundException exception =
                assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> productService.getAll());

        assertEquals("User is not find or authenticated", exception.getMessage());

        verify(userService, times(1)).getAuthUser();
        verify(productRepository, never()).findByCreatedByUserIsNullOrCreatedByUserId(anyLong());
    }

    /**
     * Тестирует получение продукта по имени, когда
     * он существует в сохранённых записях.
     */
    @Test
    void findProductByName_WhenProductExists_ShouldReturnProduct() {
        String productName = "Apple";
        List<Product> products = List.of(systemProduct);
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenReturn(products);

        Product result = productService.findProductByName(productName);

        assertNotNull(result);
        assertEquals(systemProduct.getName(), result.getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует получение продукта по имени, когда его нет в сохранённых записях.
     */
    @Test
    void findProductByName_WhenProductNotExists_ShouldReturnNull() {
        String productName = "NonExistentProduct";
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenReturn(Collections.emptyList());

        Product result = productService.findProductByName(productName);

        assertNull(result);
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует получение всех продуктов системы и пользователя
     * для тех случаев, когда необходимо найти все продукты, у которых в названии есть
     * указанная часть.
     */
    @Test
    void findProductByName_WhenMultipleProductsFound_ShouldReturnFirst() {
        String productName = "test";
        Product secondProduct = new Product();
        secondProduct.setId(3L);
        secondProduct.setName("Test Product");

        List<Product> products = List.of(systemProduct, secondProduct);
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenReturn(products);

        Product result = productService.findProductByName(productName);

        assertNotNull(result);
        assertEquals(systemProduct.getId(), result.getId());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует нахождение продукта среди сохранённых по названию.
     * Возвращает положительный результат.
     */
    @Test
    void productExists_WhenProductExists_ShouldReturnTrue() {
        String productName = "Apple";
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenReturn(List.of(systemProduct));

        boolean result = productService.productExists(productName);

        assertTrue(result);
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует нахождение продукта среди сохранённых по названию.
     * Возвращает отрицательный результат.
     */
    @Test
    void productExists_WhenProductNotExists_ShouldReturnFalse() {
        String productName = "NonExistentProduct";
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenReturn(Collections.emptyList());

        boolean result = productService.productExists(productName);

        assertFalse(result);
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует получение продукта среди сохранённых по названию,
     * если на вход подали массив с данными, что будут полностью соответствовать
     * тем, что сохранены.
     */
    @Test
    void findProductsByNames_WhenAllProductsExist_ShouldReturnAllProducts() {
        List<String> productNames = List.of("Apple", "Chicken Breast");

        when(productRepository.findByNameContainingIgnoreCase("Apple"))
                .thenReturn(List.of(systemProduct));
        when(productRepository.findByNameContainingIgnoreCase("Chicken Breast"))
                .thenReturn(List.of(userProduct));

        List<Product> result = productService.findProductsByNames(productNames);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(systemProduct));
        assertTrue(result.contains(userProduct));

        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Apple");
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Chicken Breast");
    }

    /**
     * Тестирует получение продукта среди сохранённых по названию,
     * если на вход подали массив с названиями,
     * среди которых будет найдена лишь часть продуктов.
     */
    @Test
    void findProductsByNames_WhenSomeProductsNotExist_ShouldReturnOnlyExistingProducts() {
        List<String> productNames = List.of("Apple", "NonExistentProduct", "Chicken Breast");

        when(productRepository.findByNameContainingIgnoreCase("Apple"))
                .thenReturn(List.of(systemProduct));
        when(productRepository.findByNameContainingIgnoreCase("NonExistentProduct"))
                .thenReturn(Collections.emptyList());
        when(productRepository.findByNameContainingIgnoreCase("Chicken Breast"))
                .thenReturn(List.of(userProduct));

        List<Product> result = productService.findProductsByNames(productNames);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(systemProduct));
        assertTrue(result.contains(userProduct));

        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Apple");
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("NonExistentProduct");
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Chicken Breast");
    }

    /**
     * Тестирует случай, когда на вход методу поиска попадает пустой список названий.
     */
    @Test
    void findProductsByNames_WhenEmptyList_ShouldReturnEmptyList() {
        List<String> productNames = Collections.emptyList();

        List<Product> result = productService.findProductsByNames(productNames);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    /**
     * Тестирует случай, когда на вход методу поиска попадает null значение.
     */
    @Test
    void findProductsByNames_WhenNullList_ShouldHandleGracefully() {
        List<String> productNames = null;

        List<Product> result = productService.findProductsByNames(productNames);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Тестирует получение всех продуктов, когда среди сохранённых нет никаких продуктов.
     */
    @Test
    void getAll_WhenRepositoryReturnsEmptyList_ShouldReturnEmptyList() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(productRepository.findByCreatedByUserIsNullOrCreatedByUserId(testUser.getId()))
                .thenReturn(Collections.emptyList());

        List<Product> result = productService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userService, times(1)).getAuthUser();
        verify(productRepository, times(1))
                .findByCreatedByUserIsNullOrCreatedByUserId(testUser.getId());
    }

    /**
     * Тестирует работу поиска в случае возникновения ошибки при работе с БД.
     */
    @Test
    void findProductByName_WhenRepositoryThrowsException_ShouldPropagateException() {
        String productName = "Apple";
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> productService.findProductByName(productName));

        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }

    /**
     * Тестирует работу проверки нахождения продукта среди сохранённых,
     * в случае возникновения ошибки при работе с БД.
     */
    @Test
    void productExists_WhenRepositoryThrowsException_ShouldPropagateException() {
        String productName = "Apple";
        when(productRepository.findByNameContainingIgnoreCase(productName))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> productService.productExists(productName));

        verify(productRepository, times(1)).findByNameContainingIgnoreCase(productName);
    }
}