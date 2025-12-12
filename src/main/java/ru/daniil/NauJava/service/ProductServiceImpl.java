package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.request.create.CreateProductRequest;
import ru.daniil.NauJava.request.update.UpdateProductRequest;

import java.time.LocalDateTime;
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


    @Cacheable(value = "user-products",
            key = "'products:' + #userId")
    @Override
    public List<Product> getAll(Long userId) {
        if (userId == null){
            return new ArrayList<>();
        }
        return productRepository.findByCreatedByUserIsNullOrCreatedByUserId(userId);
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
        if (productInfo == null){
            throw new NullPointerException();
        }
        Product newProduct = Optional.of(productInfo)
                .map(request -> new Product(
                        request.getName(),
                        request.getCaloriesPer100g(),
                        request.getProteinsPer100g(),
                        request.getFatsPer100g(),
                        request.getCarbsPer100g()
                ))
                .orElseThrow(() -> new IllegalArgumentException("CreateProductRequest cannot be null"));
        newProduct.setCreatedByUser(userService.getAuthUser().orElse(null));
        if (productRepository.existsByNameIgnoreCase(productInfo.getName())){
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

    @Override
    public Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return productRepository.countByCreatedAtBetween(start, end);
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
    @Override
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
    @Override
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
