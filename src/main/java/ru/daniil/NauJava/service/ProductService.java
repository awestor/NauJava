package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    /**
     * Находит все продукты пользователя и системы
     * @return список продуктов с этим названием
     */
    List<Product> getAll();

    /**
     * Находит несколько продуктов по именам
     * @param productNames название продукта
     * @return список продуктов с этим названием
     */
    List<Product> findProductsByNames(List<String> productNames);

    /**
     * Находит и возвращает только 1-й продукт с указанным именем (без учета регистра)
     * @param productName название продукта
     * @return найденный по названию продукт
     */
    Product findProductByName(String productName);

    /**
     * Метод, что проверяет существование продукта по его имени
     * @param productName имя продукта
     * @return true если продукт найден, иначе - false
     */
    boolean productExists(String productName);
    /**
     * Метод, что проверяет существование продукта по его имени
     * @param userId id пользователя
     * @return список продуктов, созданных пользователем
     */
    List<Product> findProductByUserId(Long userId);

    List<Product> findByCreatedByUserIsNull();

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findById(Long identifier);

    List<Product> findProductsWithMinCaloriesAndUser(Double calories, Long userId);

    boolean existsByNameIgnoreCase(String productName);
}
