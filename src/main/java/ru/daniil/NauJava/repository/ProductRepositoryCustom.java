package ru.daniil.NauJava.repository;

import ru.daniil.NauJava.entity.Product;
import java.util.List;

public interface ProductRepositoryCustom {

    /**
     * Находит все продукты с калорийностью больше или равной указанной
     * @param minCalories минимальное значение калорийности
     */
    List<Product> findProductsWithMinCalories(Double minCalories);

    /**
     * Находит все продукты, название которых содержит указанную строку (без учета регистра)
     * @param name часть названия продукта для поиска
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}
