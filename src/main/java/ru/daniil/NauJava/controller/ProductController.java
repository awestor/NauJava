package ru.daniil.NauJava.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;

    @Autowired
    ProductController(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    /**
     * Возвращает все продукты, что сохранены в базе данных
     * @return все продукты в БД
     */
    @GetMapping("/all")
    public Iterable<Product> getAllProducts()
    {
        return productRepository.findAll();
    }

    /**
     * Возвращает все продукты, у которых поле CreatedByUser == null,
     * что сохранены в базе данных
     * @return системные продукты в БД
     */
    @GetMapping("/system")
    public List<Product> getAllSystemProducts() {
        return productRepository.findByCreatedByUserIsNull();
    }

    /**
     * Возвращает все продукты, у которых поле CreatedByUser == identifier,
     * указанному в URL из тех, что сохранены в базе данных
     * @param userId идентификатор пользователя
     * @return продукты конкретного пользователя
     */
    @GetMapping("/getByCreatorId")
    public List<Product> getByCreatorId(@RequestParam Long userId)
    {
        return userId != 0 ?
                productRepository.findByCreatedByUserId(userId) :
                productRepository.findByCreatedByUserIsNull();
    }

    /**
     * Возвращает все продукты, в названии которых есть 'name'
     * @param name часть названия продукта
     * @return список найденных продуктов
     */
    @GetMapping("/getProductsByName")
    public List<Product> getProductsByName(@RequestParam String name)
    {
        return productRepository.findByNameContainingIgnoreCase(name);
    }


    /**
     * Возвращает продукт, у которого id равен указанному
     * @param identifier идентификатор продукта
     * @return продукт из БД или null
     */
    @GetMapping("/{identifier}")
    public Optional<Product> getProductById(@PathVariable Long identifier)
    {
        return productRepository.findById(identifier);
    }

    /**
     * Возвращает все продукты системы с калорийностью выше указанной
     * @param calories минимум калорий
     * @return список продуктов системы
     */
    @GetMapping("/getProductsWithMinCalories/system")
    public List<Product> getSystemProductsWithMinCalories(@RequestParam Double calories) {
        return productRepository.findProductsWithMinCaloriesAndUser(calories, null);
    }

    /**
     * Возвращает все продукты пользователя с калорийностью выше указанной
     * @param calories минимум калорий
     * @param userId идентификатор пользователя
     * @return список продуктов пользователя
     */
    @GetMapping("/getProductsWithMinCalories/{userId}")
    public List<Product> getUserProductsWithMinCalories(@PathVariable Double calories, @RequestParam Long userId)
    {
        return productRepository.findProductsWithMinCaloriesAndUser(calories, userId);
    }

    /**
     * Возвращает true если продукт(ы) с таким названием существуют в БД и
     * false если таковых не найдено
     * @param productName название продукта
     * @return true или false
     */
    @GetMapping("/existsProductsByName")
    public boolean existsProductsByName(@RequestParam String productName)
    {
        return productRepository.existsByNameIgnoreCase(productName);
    }
}
