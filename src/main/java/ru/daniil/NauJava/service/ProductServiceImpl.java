package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.ProductRepository;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public Product findProductByName(String productName) {
        return productRepository.findByNameContainingIgnoreCase(productName)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean productExists(String productName) {
        return !productRepository.findByNameContainingIgnoreCase(productName).isEmpty();
    }

    @Transactional
    @Override
    public List<Product> findProductsByNames(List<String> productNames) {
        return productNames.stream()
                .map(this::findProductByName)
                .filter(product -> product != null)
                .toList();
    }

    /*@Override
    public void createProduct(Long id, String name, String description, double calories) {
        Product product = new Product(id, name, description, calories);
        //productRepository.create(product);
    }

    //@Override
    public Product findById(Long id) {
        return null;//productRepository.read(id);
    }

    //@Override
    public void deleteById(Long id) {
        //productRepository.delete(id);
    }

    //@Override
    public void updateDescription(Long id, String newDescription) {
        Product product = null;// productRepository.read(id);
        if (product != null) {
            Product updated = new Product(id, product.getName(), newDescription, product.getCaloriesPer100g());
            //productRepository.update(updated);
        }
    }

    //@Override
    public void updateCalories(Long id, double newCalories) {
        Product product = null;//productRepository.read(id);
        if (product != null) {
            Product updated = new Product(id, product.getName(), product.getDescription(), newCalories);
            //productRepository.update(updated);
        }
    }*/
}
