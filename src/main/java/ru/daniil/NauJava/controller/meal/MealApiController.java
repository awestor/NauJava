package ru.daniil.NauJava.controller.meal;

import jakarta.validation.Valid;
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

    public MealApiController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createMeal(@Valid @RequestBody CreateMealRequest request) {
        try {
            Meal meal = mealService.createMealWithProducts(request.getMealTypeName(),
                    request.getProductNames(), request.getQuantities());
            mealService.updateNutritionSum(meal.getDailyReport().getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<Void> updateMeal(@PathVariable Long id,
                                           @Valid @RequestBody UpdateMealRequest request) {
        try {
            if (!id.equals(request.getId())) {
                return ResponseEntity.badRequest().build();
            }

            Meal meal = mealService.updateMealWithProducts(request.getId(), request.getMealTypeName(),
                    request.getProductNames(), request.getQuantities());
            mealService.updateNutritionSum(meal.getDailyReport().getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        try {
            mealService.deleteCurrentMeal(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getMeal(@PathVariable Long id) {
        try {
            Meal meal = mealService.getMealById(id).orElseThrow();
            MealResponse response = convertToResponse(meal);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
