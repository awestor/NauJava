package ru.daniil.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.daniil.NauJava.entity.Product;

import java.util.ArrayList;
import java.util.List;

//@Configuration
public class ProductConfig {
    //@Bean
    public List<Product> productContainer() {
        return new ArrayList<>();
    }
}

