package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.request.create.CreateProductRequest;
import ru.daniil.NauJava.request.update.UpdateProductRequest;
import ru.daniil.NauJava.service.ProductServiceImpl;
import ru.daniil.NauJava.service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MealEntryRepository mealEntryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "testuser", "encodedPassword");
        testUser.setId(1L);

        testProduct = new Product(
                "Яблоко", "Свежие яблоки",
                52.0, 0.3,
                0.2, 14.0);
        testProduct.setId(1L);
        testProduct.setCreatedByUser(testUser);
    }

    @Test
    void getAll_WhenUserExists_ShouldReturnProducts() {
        when(productRepository.findByCreatedByUserIsNullOrCreatedByUserId(1L))
                .thenReturn(Collections.singletonList(testProduct));

        List<Product> products = productService.getAll(1L);

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Яблоко", products.get(0).getName());
        verify(productRepository).findByCreatedByUserIsNullOrCreatedByUserId(1L);
    }

    @Test
    void getAll_WhenUserNotAuthenticated_ShouldThrowException() {
        List<Product> result = productService.getAll(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    void findProductByName_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findByNameContainingIgnoreCase("яблоко"))
                .thenReturn(Collections.singletonList(testProduct));

        Product result = productService.findProductByName("яблоко");

        assertNotNull(result);
        assertEquals("Яблоко", result.getName());
        verify(productRepository).findByNameContainingIgnoreCase("яблоко");
    }

    @Test
    void findProductByName_WhenProductNotExists_ShouldReturnNull() {
        when(productRepository.findByNameContainingIgnoreCase("несуществующий"))
                .thenReturn(Collections.emptyList());

        Product result = productService.findProductByName("несуществующий");

        assertNull(result);
    }

    @Test
    void saveProduct_WhenValidRequestAndUniqueName_ShouldSaveProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Банан");
        request.setCaloriesPer100g(89.0);
        request.setProteinsPer100g(1.1);
        request.setFatsPer100g(0.3);
        request.setCarbsPer100g(22.8);

        Product newProduct = new Product(
                "Банан", "description",
                89.0, 1.1,
                0.3, 22.8);
        newProduct.setCreatedByUser(testUser);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(productRepository.existsByNameIgnoreCase("Банан")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product result = productService.saveProduct(request);

        assertNotNull(result);
        assertEquals("Банан", result.getName());
        assertEquals(89, result.getCaloriesPer100g());
        verify(productRepository).existsByNameIgnoreCase("Банан");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void saveProduct_WhenDuplicateName_ShouldReturnNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Яблоко");

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(productRepository.existsByNameIgnoreCase("Яблоко")).thenReturn(true);

        Product result = productService.saveProduct(request);

        assertNull(result);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProduct_WhenNullRequest_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            productService.saveProduct(null);
        });
    }

    @Test
    void findProductByUserId_WhenUserHasProducts_ShouldReturnList() {
        when(productRepository.findByCreatedByUserId(1L))
                .thenReturn(Collections.singletonList(testProduct));

        List<Product> products = productService.findProductByUserId(1L);

        assertEquals(1, products.size());
        assertEquals(testUser, products.get(0).getCreatedByUser());
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        when(productRepository.findByNameContainingIgnoreCase("ябл"))
                .thenReturn(Collections.singletonList(testProduct));

        List<Product> products = productService.findByNameContainingIgnoreCase("ябл");

        assertEquals(1, products.size());
        assertTrue(products.get(0).getName().toLowerCase().contains("ябл"));
    }

    @Test
    void findById_WhenExists_ShouldReturnOptional() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmptyOptional() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void productExists_WhenProductExists_ShouldReturnTrue() {
        when(productRepository.findByNameContainingIgnoreCase("Яблоко"))
                .thenReturn(Collections.singletonList(testProduct));

        boolean exists = productService.productExists("Яблоко");

        assertTrue(exists);
    }

    @Test
    void productExists_WhenProductNotExists_ShouldReturnFalse() {
        when(productRepository.findByNameContainingIgnoreCase("Несуществующий"))
                .thenReturn(Collections.emptyList());

        boolean exists = productService.productExists("Несуществующий");

        assertFalse(exists);
    }

    @Test
    void findProductsByNames_WhenNamesListProvided_ShouldReturnProducts() {
        List<String> names = Arrays.asList("Яблоко", "Банан");
        Product banana = new Product(
                "Банан", "Свежий банан",
                89.0, 1.1,
                0.3, 22.8);

        when(productRepository.findByNameContainingIgnoreCase("Яблоко"))
                .thenReturn(Collections.singletonList(testProduct));
        when(productRepository.findByNameContainingIgnoreCase("Банан"))
                .thenReturn(List.of(banana));

        List<Product> products = productService.findProductsByNames(names);

        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("Яблоко")));
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("Банан")));
    }

    @Test
    void findProductsByNames_WhenNullList_ShouldReturnEmptyList() {
        List<Product> products = productService.findProductsByNames(null);

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    void findProductsByNames_WhenEmptyList_ShouldReturnEmptyList() {
        List<Product> products = productService.findProductsByNames(new ArrayList<>());

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    void updateProduct_WhenValidRequestAndOwner_ShouldUpdateProduct() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setId(1L);
        request.setName("Яблоко обновленное");
        request.setCaloriesPer100g(55.0);
        request.setProteinsPer100g(0.4);
        request.setFatsPer100g(0.3);
        request.setCarbsPer100g(15.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateProduct(request);

        verify(productRepository).save(any(Product.class));
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldThrowException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setId(999L);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(request);
        }, "Продукт не найден");
    }

    @Test
    void updateProduct_WhenNotOwner_ShouldThrowException() {
        User otherUser = new User("other@example.com", "other_user", "password");
        otherUser.setId(2L);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userService.getAuthUser()).thenReturn(Optional.of(otherUser));

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(request);
        }, "У пользователя нет прав на редактирование этого продукта");
    }

    @Test
    void deleteProduct_WhenValidAndOwner_ShouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        doNothing().when(mealEntryRepository).disconnectFromProduct(1L);
        doNothing().when(productRepository).delete(testProduct);

        productService.deleteProduct(1L);

        verify(mealEntryRepository).disconnectFromProduct(1L);
        verify(productRepository).delete(testProduct);
    }

    @Test
    void deleteProduct_WhenNotOwner_ShouldThrowException() {
        User otherUser = new User("other@example.com", "other_user", "password");
        otherUser.setId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userService.getAuthUser()).thenReturn(Optional.of(otherUser));

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(1L);
        }, "У пользователя нет прав на удаление этого продукта");
    }
}
