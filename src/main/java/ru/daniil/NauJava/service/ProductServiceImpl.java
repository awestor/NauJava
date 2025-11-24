package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.request.UpdateProductRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, UserService userService) {
        this.userService = userService;
        this.productRepository = productRepository;
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
    public Product saveProduct(Product productInfo) {
        return productRepository.save(productInfo);
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

    @Override
    public List<Product> findProductsWithMinCaloriesAndUser(Double calories, Long userId) {
        return productRepository.findProductsWithMinCaloriesAndUser(calories, userId);
    }

    @Override
    public boolean existsByNameIgnoreCase(String productName) {
        return productRepository.existsByNameIgnoreCase(productName);
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

        productRepository.delete(product);
    }
}
