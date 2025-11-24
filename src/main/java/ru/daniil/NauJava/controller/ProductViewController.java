package ru.daniil.NauJava.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.request.CreateProductRequest;
import ru.daniil.NauJava.service.ProductService;
import ru.daniil.NauJava.service.UserService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/products")
public class ProductViewController {
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    ProductViewController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Возвращает домашнюю страницу
     *
     * @return index.html
     */
    @GetMapping(value = "/")
    public String getIndex() {
        return "index";
    }

    /**
     * Возвращает все доступные продукты в базе данных
     * в виде страницы с таблицей
     *
     * @return products.html
     */
    @GetMapping("/list")
    public ModelAndView productListView() {
        Map<String, Object> model = new HashMap<>();
        List<Product> products = productService.getAll();

        products.sort(Comparator.comparing(Product::getId));

        model.put("products", products);

        long totalCount = products.size();
        model.put("totalCount", totalCount);

        return new ModelAndView("products", model);
    }

    /**
     * Отображает форму для создания нового продукта
     *
     * @return имя HTML шаблона формы
     */
    @GetMapping("/createForm")
    public String showCreateProductForm() {
        return "product-form";
    }

    /**
     * Обрабатывает полученную форму создания продукта,
     * а после создаёт запись о продукте в БД
     *
     * @param createProductRequest DTO с данными продукта
     * @param bindingResult        Результаты валидации
     * @param redirectAttributes   Атрибуты для redirect
     * @return redirect URL после обработки формы
     */
    @PostMapping("/create")
    public String createProductFromForm(@Valid @ModelAttribute("product") CreateProductRequest createProductRequest,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "product-form";
        }

        try {
            Product product = new Product(
                    createProductRequest.getName(),
                    "description",
                    createProductRequest.getCaloriesPer100g(),
                    createProductRequest.getProteinsPer100g(),
                    createProductRequest.getFatsPer100g(),
                    createProductRequest.getCarbsPer100g()
            );
            product.setCreatedByUser(userService.getAuthUser().orElse(null));

            Product savedProduct = productService.saveProduct(product);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product '" + savedProduct.getName() + "' successfully created!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating product: " + e.getMessage());
        }

        return "redirect:/view/products/list";
    }

    @GetMapping("/calendarActivity")
    public String showCalendarActivity() {
        return "calendarActivity";
    }
}
