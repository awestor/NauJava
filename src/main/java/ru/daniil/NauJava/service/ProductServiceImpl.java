package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.request.create.CreateProductRequest;
import ru.daniil.NauJava.request.update.UpdateProductRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserService userService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, UserService userService,
                              MealEntryRepository mealEntryRepository) {
        this.userService = userService;
        this.productRepository = productRepository;
        this.mealEntryRepository = mealEntryRepository;
    }

    @Transactional
    @Override
    public List<Product> getAll() {
        User user = userService.getAuthUser().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Пользователь не найден или не авторизован"));
        return productRepository.findByCreatedByUserIsNullOrCreatedByUserId(user.getId());
    }

    @Transactional
    @Override
    public Product findProductByName(String productName) {
        return productRepository.findByNameContainingIgnoreCase(productName)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Product saveProduct(CreateProductRequest productInfo) {
        Product newProduct = Optional.of(productInfo)
                .map(request -> new Product(
                        request.getName(),
                        "description",
                        request.getCaloriesPer100g(),
                        request.getProteinsPer100g(),
                        request.getFatsPer100g(),
                        request.getCarbsPer100g()
                ))
                .orElseThrow(() -> new IllegalArgumentException("CreateProductRequest cannot be null"));
        newProduct.setCreatedByUser(userService.getAuthUser().orElse(null));

        Product product = productRepository.findById(newProduct.getId()).orElse(null);

        if (product != null){
            return null;
        }

        return productRepository.save(newProduct);
    }

    @Transactional
    @Override
    public List<Product> findProductByUserId(Long userId) {
        return productRepository.findByCreatedByUserId(userId);
    }

    @Override
    public List<Product> findByCreatedByUserIsNull() {
        return productRepository.findByCreatedByUserIsNull();
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Optional<Product> findById(Long identifier) {
        return productRepository.findById(identifier);
    }

    @Transactional
    @Override
    public boolean productExists(String productName) {
        return !productRepository.findByNameContainingIgnoreCase(productName).isEmpty();
    }

    @Transactional
    @Override
    public List<Product> findProductsByNames(List<String> productNames) {
        if(productNames == null){
            return new ArrayList<Product>();
        }
        return productNames.stream()
                .map(this::findProductByName)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public void updateProduct(UpdateProductRequest request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        User currentUser = userService.getAuthUser().orElseThrow();
        if (product.getCreatedByUser() != null &&
                !product.getCreatedByUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("У пользователя нет прав на редактирование этого продукта");
        }

        product.setName(request.getName());
        product.setCaloriesPer100g(request.getCaloriesPer100g());
        product.setProteinsPer100g(request.getProteinsPer100g());
        product.setFatsPer100g(request.getFatsPer100g());
        product.setCarbsPer100g(request.getCarbsPer100g());

        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        User currentUser = userService.getAuthUser().orElseThrow();

        if (product.getCreatedByUser() != null &&
                !product.getCreatedByUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("У пользователя нет прав на удаление этого продукта");
        }

        mealEntryRepository.disconnectFromProduct(id);
        productRepository.delete(product);
    }
}
