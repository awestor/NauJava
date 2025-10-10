package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Product;

public interface ProductService {
    /**
     * Метод вызова действий по созданию новой
     * записи продукта в списке.
     * @param id
     * @param name
     * @param description
     * @param calories
     */
    void createProduct(Long id, String name, String description, double calories);

    /**
     * Метод вызова действий по поиску записи
     * в списке по его id.
     * @param id
     * @return
     */
    Product findById(Long id);

    /**
     * Метод вызова действий по удалению записи
     * продукта из списка по его id.
     * @param id
     */
    void deleteById(Long id);

    /**
     * Метод вызова действий по изменению описания продукта
     * @param id
     * @param newDescription
     */
    void updateDescription(Long id, String newDescription);

    /**
     * Метод вызова действий по изменению калорийности продукта
     * @param id
     * @param newCalories
     */
    void updateCalories(Long id, double newCalories);
}
