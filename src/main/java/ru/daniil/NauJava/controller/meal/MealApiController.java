package ru.daniil.NauJava.controller.meal;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.request.create.CreateMealRequest;
import ru.daniil.NauJava.response.MealEntryResponse;
import ru.daniil.NauJava.response.MealResponse;
import ru.daniil.NauJava.request.update.UpdateMealRequest;
import ru.daniil.NauJava.service.MealService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meals")
public class MealApiController {

    private final MealService mealService;

    private static final Logger logger = LoggerFactory.getLogger(MealApiController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    public MealApiController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createMeal(@Valid @RequestBody CreateMealRequest request) {
        try {
            appLogger.info("POST /api/meals/create | Создание нового приёма пищи");

            Meal meal = mealService.createMealWithProducts(request.getMealTypeName(),
                    request.getProductNames(), request.getQuantities());
            mealService.updateNutritionSum(meal.getDailyReport().getId());

            appLogger.debug("Создание нового приёма пищи прошло успешно, его id:{}", meal.getId());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("Создание нового приёма пищи провалилось с ошибкой: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<Void> updateMeal(@PathVariable Long id,
                                           @Valid @RequestBody UpdateMealRequest request) {
        try {
            logger.info("PUT /api/meals/update/{id} | Обновление приёма пищи");

            if (!id.equals(request.getId())) {
                return ResponseEntity.badRequest().build();
            }

            Meal meal = mealService.updateMealWithProducts(request.getId(), request.getMealTypeName(),
                    request.getProductNames(), request.getQuantities());
            mealService.updateNutritionSum(meal.getDailyReport().getId());

            logger.debug("Обновление приёма пищи с id:{} прошло успешно", id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("Обновление приёма пищи провалилось с ошибкой: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        try {
            logger.info("DELETE /api/meals/{id} | Удаление приёма пищи с id:{}", id);

            mealService.deleteCurrentMeal(id);

            logger.debug("Удаление приёма пищи с id:{} прошло успешно", id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("Удаление приёма пищи провалилось с ошибкой: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getMeal(@PathVariable Long id) {
        try {
            logger.info("GET /api/meals/{id} | Получение приёма пищи пользователя");

            Meal meal = mealService.getMealById(id).orElseThrow();
            MealResponse response = convertToResponse(meal);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Получение приёма пищи провалилось с ошибкой: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private MealResponse convertToResponse(Meal meal) {
        List<MealEntryResponse> entries = meal.getMealEntries().stream()
                .map(entry -> new MealEntryResponse(
                        entry.getProduct().getName(),
                        entry.getQuantityGrams()
                ))
                .collect(Collectors.toList());

        return new MealResponse(
                meal.getId(),
                meal.getMealType(),
                entries
        );
    }

}
