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
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.request.CreateProductRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/products")
public class ProductViewController {
    private final ProductRepository productRepository;

    @Autowired
    ProductViewController(
            ProductRepository productRepository
        )
    {
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

    /**
     * Отображает форму для создания нового продукта
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
     * @param bindingResult Результаты валидации
     * @param redirectAttributes Атрибуты для redirect
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
            /*
                // Потом здесь будет проверка на то, авторизован ли пользователь, но пока - нет
                // и всё что создаётся - будет системным
                User currentUser = userService.findUserByEmail(principal.getName());

                if (currentUser == null) {
                    throw new ValidationException("User not found");

                }
                System.out.println(currentUser);
            */


            // Создание продукта
            Product product = new Product(
                    createProductRequest.getName(),
                    "description",
                    createProductRequest.getCaloriesPer100g(),
                    createProductRequest.getProteinsPer100g(),
                    createProductRequest.getFatsPer100g(),
                    createProductRequest.getCarbsPer100g()
            );
            product.setCreatedByUser(null);

            System.out.println(product);

            Product savedProduct = productRepository.save(product);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product '" + savedProduct.getName() + "' successfully created!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating product: " + e.getMessage());
        }

        return "redirect:/view/products/list";
    }
}
