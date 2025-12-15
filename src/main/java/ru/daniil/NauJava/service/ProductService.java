package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.create.CreateProductRequest;
import ru.daniil.NauJava.request.update.UpdateProductRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    /**
     * Находит все продукты пользователя и системы
     * @return список продуктов с этим названием
     */
    List<Product> getAll(Long userId);

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
     * Добавляет в БД продукт по его информации
     * @param productInfo информация о продукте
     * @return сохранённый продукт
     */
    Product saveProduct(CreateProductRequest productInfo);

    /**
     * Метод, что проверяет существование продукта по его имени
     * @param userId id пользователя
     * @return список продуктов, созданных пользователем
     */
    List<Product> findProductByUserId(Long userId);

    /**
     * Возвращает все продукты, в названии которых есть указанная часть
     * @param name часть названия продукта
     * @return список продуктов
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Находит продукт по его id
     * @param identifier id продукта
     * @return продукт или null
     */
    Optional<Product> findById(Long identifier);

    /**
     * Метод, что проверяет существование продукта по его имени
     * @param productName имя продукта
     * @return true если продукт найден, иначе - false
     */
    boolean productExists(String productName);

    /**
     * Считает количество продуктов зарегистрированных в системе в диапазоне дат
     * @param start начало диапазона дат
     * @param end конец диапазона дат
     * @return количество продуктов
     */
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Обновляет данные о продукте
     * @param request UpdateProductRequest, что содержит данные для обновления
     */
    void updateProduct(UpdateProductRequest request);

    /**
     * Удаляет продукт из БД
     * @param id id продукта для удаления
     */
    void deleteProduct(Long id);
}
