package ru.daniil.NauJava.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void createProduct(Long id, String name, String description, double calories) {
        Product product = new Product(id, name, description, calories);
        productRepository.create(product);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.read(id);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.delete(id);
    }

    @Override
    public void updateDescription(Long id, String newDescription) {
        Product product = productRepository.read(id);
        if (product != null) {
            Product updated = new Product(id, product.getName(), newDescription, product.getCalories());
            productRepository.update(updated);
        }
    }

    @Override
    public void updateCalories(Long id, double newCalories) {
        Product product = productRepository.read(id);
        if (product != null) {
            Product updated = new Product(id, product.getName(), product.getDescription(), newCalories);
            productRepository.update(updated);
        }
    }
}
