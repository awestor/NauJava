package ru.daniil.NauJava.controller.product;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.request.ProductInfoResponse;
import ru.daniil.NauJava.request.update.UpdateProductRequest;
import ru.daniil.NauJava.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;

    @Autowired
    ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Возвращает все продукты, что сохранены в базе данных
     * @return все продукты в БД
     */
    @GetMapping("/all")
    public Iterable<Product> getAllProducts() {
        return productService.getAll();
    }

    /**
     * Возвращает все продукты, что сохранены в базе данных
     *
     * @return все продукты в БД
     */
    @GetMapping("/all/baseInfo")
    public List<ProductInfoResponse> getAllBaseInfoProducts() {
        return productService.getAll().stream()
                .map(product -> new ProductInfoResponse(product.getId(), product.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает все продукты, у которых поле CreatedByUser == identifier,
     * указанному в URL из тех, что сохранены в базе данных
     *
     * @param userId идентификатор пользователя
     * @return продукты конкретного пользователя
     */
    @GetMapping("/getByCreatorId")
    public List<Product> getByCreatorId(@RequestParam Long userId) {
        return userId != 0 ?
                productService.findProductByUserId(userId) :
                productService.findByCreatedByUserIsNull();
    }

    /**
     * Возвращает все продукты, в названии которых есть 'name'
     *
     * @param name часть названия продукта
     * @return список найденных продуктов
     */
    @GetMapping("/getProductsByName")
    public List<Product> getProductsByName(@RequestParam String name) {
        return productService.findByNameContainingIgnoreCase(name);
    }


    /**
     * Возвращает продукт, у которого id равен указанному
     *
     * @param identifier идентификатор продукта
     * @return продукт из БД или null
     */
    @GetMapping("/{identifier}")
    public Optional<Product> getProductById(@PathVariable Long identifier) {
        return productService.findById(identifier);
    }

    /**
     * Возвращает true если продукт(ы) с таким названием существуют в БД и
     * false если таковых не найдено
     *
     * @param productName название продукта
     * @return true или false
     */
    @GetMapping("/existsProductsByName")
    public boolean existsProductsByName(@RequestParam String productName) {
        return productService.productExists(productName);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id,
                                              @Valid @RequestBody UpdateProductRequest request) {
        try {
            if (!id.equals(request.getId())) {
                return ResponseEntity.badRequest().build();
            }

            productService.updateProduct(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
