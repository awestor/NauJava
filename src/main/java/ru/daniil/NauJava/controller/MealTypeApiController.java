package ru.daniil.NauJava.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.request.MealTypeResponse;
import ru.daniil.NauJava.service.MealTypeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meal-types")
public class MealTypeApiController {

    private final MealTypeService mealTypeService;

    public MealTypeApiController(MealTypeService mealTypeService) {
        this.mealTypeService = mealTypeService;
    }

    @GetMapping
    public List<MealTypeResponse> getMealTypes() {
        return mealTypeService.getMealTypes().stream()
                .map(mealType -> new MealTypeResponse(mealType.getName()))
                .collect(Collectors.toList());
    }
}