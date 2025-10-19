package ru.daniil.NauJava;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.ProductRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class NauJavaApplicationTests {
    @Autowired
    ProductRepository productRepository;

    /**
     * Тестовый метод проверки корректной загрузки контекста в проекте
     */
    @Test
    public void contextLoads() {}

    /**
     * Тестирует сохранение новой записи продукта в БД.
     * Проверяет работу алгоритма сохранения в БД,
     * а также работу метода findByNameIgnoreCaseAndCreatedByUserId()
     */
    @Test
    void addProduct(){
        Product product = new Product(
                "Apple", "delicious",
                52.0, 0.26,
                0.17,13.81);
        Long userId = product.getCreatedByUser() == null ? null : product.getCreatedByUser().getId();

        Optional<Product> savedProduct = productRepository.findByNameIgnoreCaseAndCreatedByUserId(product.getName(), userId);

        if(savedProduct.isEmpty()) {
            savedProduct = Optional.of(productRepository.save(product));
        }
        else{
            System.out.println("Продукт уже существует в БД.");
        }

        // Проверка, что продукт сохранился
        assertThat(savedProduct.get().getId()).isNotNull();
        assertThat(savedProduct.get().getName()).isEqualTo("apple");
    }
}


