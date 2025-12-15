package ru.daniil.NauJava.controller.product;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.create.CreateProductRequest;
import ru.daniil.NauJava.service.ProductService;
import ru.daniil.NauJava.service.UserService;

import java.util.*;

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
     * Возвращает все доступные продукты в базе данных
     * в виде страницы с таблицей
     *
     * @return products.html
     */
    @GetMapping("/list")
    public ModelAndView productListView() {
        Map<String, Object> model = new HashMap<>();
        User user = userService.getAuthUser().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Пользователь не найден или не авторизован"));
        List<Product> products = productService.getAll(user.getId());

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
            Product savedProduct = productService.saveProduct(createProductRequest);
            if (savedProduct == null){
                System.err.println("Ошибка при создании продукта");
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка при создании продукта: Продукт уже сохранён в БД");
            }
            else {
                System.out.println("Продукт успешно создан");
                redirectAttributes.addFlashAttribute("successMessage",
                        "Продукт '" + savedProduct.getName() + "' успешно сохранён!");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании продукта " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "ошибка при создании продукта: " + e.getMessage());
        }

        return "redirect:/view/products/list";
    }
}
