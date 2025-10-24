package ru.daniil.NauJava.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/products")
public class ProductViewController {
    private ProductRepository productRepository;

    @Autowired
    ProductViewController(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    /**
     * Возвращает домашнюю страницу
     * @return index.html
     */
    @GetMapping(value = "/")
    public String getIndex() {
        return "index";
    }

    /**
     * Возвращает все доступные продукты в базе данных
     * в виде страницы с таблицей
     * @return products.html
     */
    @GetMapping("/list")
    public ModelAndView productListView() {
        Map<String, Object> model = new HashMap<>();
        List<Product> products = (List<Product>) productRepository.findAll();
        model.put("products", products);

        long systemCount = products.stream().filter(p -> p.getCreatedByUser() == null).count();
        long userCount = products.size() - systemCount;
        model.put("systemCount", systemCount);
        model.put("userCount", userCount);

        return new ModelAndView("products", model);
    }
}
