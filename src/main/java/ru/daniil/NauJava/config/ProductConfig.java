package ru.daniil.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.BeanDefinition;
import ru.daniil.NauJava.entity.Product;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ProductConfig {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<Product> productContainer() {
        return new ArrayList<>();
    }
}

