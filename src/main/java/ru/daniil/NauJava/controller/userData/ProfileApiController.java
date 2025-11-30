package ru.daniil.NauJava.controller.userData;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.exception.ApiError;
import ru.daniil.NauJava.request.update.UpdateProfileRequest;
import ru.daniil.NauJava.service.UserProfileService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileApiController {

    private final UserProfileService userProfileService;

    public ProfileApiController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            userProfileService.updateUserProfile(request);
            System.out.println("Обновление профиля завершено успешно");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Данные профиля успешно обновлены");
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("Ошибка при обновлении профиля: " + e.getMessage());
            ApiError apiError = new ApiError(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    "/api/profile/update"
            );
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка при обновлении профиля: " + e.getMessage());
            ApiError apiError = new ApiError(
                    "Ошибка при обновлении данных профиля",
                    HttpStatus.BAD_REQUEST.value(),
                    "Update Error",
                    "/api/profile/update"
            );
            return ResponseEntity.badRequest().body(apiError);
        }
    }
}