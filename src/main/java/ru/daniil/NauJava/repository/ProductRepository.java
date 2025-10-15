package ru.daniil.NauJava.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Product;

import java.util.List;

@Repository
public class ProductRepository implements CrudRepository<Product, Long> {

    private final List<Product> productContainer;

    @Autowired
    private ProductRepository(List<Product> productContainer)
    {
        this.productContainer = productContainer;
    }

    @Override
    public void create(Product product) {
        productContainer.add(product);
    }

    @Override
    public Product read(Long id) {
        return productContainer.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Product updatedProduct) {
        Long tempId = updatedProduct.getId();
        for (int i = 0; i < productContainer.size(); i++) {
            if (productContainer.get(i).getId().equals(tempId)) {
                productContainer.set(i, updatedProduct);
                return;
            }
        }
    }

    @Override
    public void delete(Long id) {
        productContainer.removeIf(p -> p.getId().equals(id));
    }
}
