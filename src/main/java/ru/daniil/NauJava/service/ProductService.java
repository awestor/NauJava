package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.Product;

public interface ProductService {
    /**
     * Метод вызова действий по созданию новой
     * записи продукта в списке.
     * @param id идентификатор сущности в БД
     * @param name название продукта
     * @param description описание продукта
     * @param calories количество калорий продукта
     */
    void createProduct(Long id, String name, String description, double calories);

    /**
     * Метод вызова действий по поиску записи
     * в списке по его id.
     * @param id идентификатор сущности в БД
     * @return найденный продукт
     */
    Product findById(Long id);

    /**
     * Метод вызова действий по удалению записи
     * продукта из списка по его id.
     * @param id идентификатор сущности в БД
     */
    void deleteById(Long id);

    /**
     * Метод вызова действий по изменению описания продукта
     * @param id идентификатор сущности в БД
     * @param newDescription новое описание продукта
     */
    void updateDescription(Long id, String newDescription);

    /**
     * Метод вызова действий по изменению калорийности продукта
     * @param id идентификатор сущности в БД
     * @param newCalories новое значение калорий у продукта
     */
    void updateCalories(Long id, double newCalories);
}
