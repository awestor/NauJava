package ru.daniil.NauJava.controller.userData;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.exception.ApiError;
import ru.daniil.NauJava.request.update.UpdateAccountRequest;
import ru.daniil.NauJava.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountApiController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AccountApiController.class);

    public AccountApiController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAccount(@Valid @RequestBody UpdateAccountRequest request) {
        try {
            logger.info("PUT /api/account/update | Обновление учётной записи пользователя");

            userService.updateUserAccount(request);

            logger.debug("Обновление учётной записи пользователя c логином {} завершено успешно.",
                    request.getLogin());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Данные аккаунта успешно обновлены");
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Ошибка при обновлении аккаунта: {}", e.getMessage());
            System.out.println("Ошибка при обновлении аккаунта: " + e.getMessage());
            ApiError apiError = new ApiError(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    "/api/account/update"
            );
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при обновлении аккаунта: {}", e.getMessage());
            System.out.println("Неожиданная ошибка при обновлении аккаунта: " + e.getMessage());
            ApiError apiError = new ApiError(
                    "Ошибка при обновлении данных аккаунта",
                    HttpStatus.BAD_REQUEST.value(),
                    "Update Error",
                    "/api/account/update"
            );
            return ResponseEntity.badRequest().body(apiError);
        }
    }
}
