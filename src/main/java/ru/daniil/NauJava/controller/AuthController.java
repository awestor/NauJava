package ru.daniil.NauJava.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserProfileService;
import ru.daniil.NauJava.service.UserService;


@Controller
@RequestMapping()
public class AuthController {
    private final UserService userService;
    private final UserProfileService userProfileService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    public AuthController(UserService userService,
                          UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    /**
     * Возвращает страницу входа
     * @return страница входа
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    /**
     * Возвращает форму для регистрации
     * @param model запрашиваемая модель
     * @return страница регистрации
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationRequest());
        return "register";
    }

    /**
     * Регистрация нового пользователя
     * @param registrationRequest заполненная форма регистрации
     * @param bindingResult результат валидации с фронта
     * @param redirectAttributes атрибут для перенаправления
     * @return путь для перенаправления
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationRequest registrationRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            appLogger.info("POST /register | Создание создание нового пользователя");

            User user = userService.registerUser(registrationRequest);
            userProfileService.createUserProfileForUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация прошла успешно! Теперь вы можете войти в систему.");

            appLogger.debug("Пользователь зарегистрирован: ID={}, Name={}",
                    user.getId(), user.getLogin());

            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка регистрации: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
