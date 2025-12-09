package ru.daniil.NauJava.controller.product;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.response.ProductInfoResponse;
import ru.daniil.NauJava.request.update.UpdateProductRequest;
import ru.daniil.NauJava.service.ProductService;
import ru.daniil.NauJava.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ProductApiController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    @Autowired
    ProductApiController(ProductService productService,
                         UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Возвращает все продукты, что сохранены в базе данных
     * @return все продукты в БД, что принадлежат конкретному пользователю
     */
    @GetMapping("/all")
    public Iterable<Product> getAllProducts() {
        appLogger.info("GET /all | Получение продуктов пользователя");

        User user = userService.getAuthUser().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Пользователь не найден или не авторизован"));
        return productService.getAll(user.getId());
    }

    /**
     * Возвращает все продукты, что сохранены в базе данных
     *
     * @return id и названия всех продукты в БД, что принадлежат конкретному пользователю
     */
    @GetMapping("/all/baseInfo")
    public List<ProductInfoResponse> getAllBaseInfoProducts() {
        appLogger.info("GET /all/baseInfo | Получение id и имён продуктов пользователя");

        User user = userService.getAuthUser().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Пользователь не найден или не авторизован"));
        return productService.getAll(user.getId()).stream()
                .map(product -> new ProductInfoResponse(product.getId(), product.getName()))
                .collect(Collectors.toList());
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
     * Обновляет данные о продукте с id равным указанному в запросе
     * @param id id продукта
     * @param request новые данные к обновлению продукта
     * @return код ответа 200 или 400
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id,
                                              @Valid @RequestBody UpdateProductRequest request) {
        try {
            if (!id.equals(request.getId())) {
                return ResponseEntity.badRequest().build();
            }
            appLogger.info("PUT /{id} | Обновление данных о продукте с id{}", id);

            productService.updateProduct(request);

            appLogger.debug("Обновление данных о продукте с id {} прошло успешно", id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("Ошибка при обновление данных о продукте с id {} и ошибкой:{}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удаляет продукт по его id
     * @param id id продукта
     * @return код ответа 200 или 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            appLogger.info("DELETE /{id} | удаление продукта с id{}", id);

            productService.deleteProduct(id);

            appLogger.debug("Удаление продукта с id{} прошло успешно", id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("При удалении продукта с id{} возникла проблема", id);
            return ResponseEntity.notFound().build();
        }
    }
}
