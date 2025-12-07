package ru.daniil.NauJava.controller.meal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.response.NutritionSumResponse;
import ru.daniil.NauJava.service.DailyReportService;
import ru.daniil.NauJava.service.MealEntryService;
import ru.daniil.NauJava.service.MealService;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/view/meals")
public class MealViewController {

    private final DailyReportService dailyReportService;
    private final MealService mealService;
    private final MealEntryService mealEntryService;

    public MealViewController(DailyReportService dailyReportService,
                              MealService mealService,
                              MealEntryService mealEntryService) {
        this.dailyReportService = dailyReportService;
        this.mealService = mealService;
        this.mealEntryService = mealEntryService;
    }

    @GetMapping("/list")
    public String getMealsForDate(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  Model model) {
        List<Meal> meals = mealService.getMealsForDate(date);

        Map<Long, List<MealEntry>> mealEntries = new HashMap<>();
        Map<Long, NutritionSumResponse> nutritionSums = new HashMap<>();

        for (Meal meal : meals) {
            Long mealId = meal.getId();

            List<MealEntry> entries = mealEntryService.getAllByMealId(mealId);
            mealEntries.put(mealId, entries != null ? entries : new ArrayList<>());

            NutritionSumResponse sum = mealEntryService.getNutritionSumByMealId(mealId);
            nutritionSums.put(mealId, sum != null ? sum : new NutritionSumResponse(0.0, 0.0, 0.0, 0.0));
        }
        Date utilDate = java.sql.Date.valueOf(date);

        model.addAttribute("currentDate", utilDate);
        model.addAttribute("meals", meals);
        model.addAttribute("mealEntries", mealEntries);
        model.addAttribute("nutritionSums", nutritionSums);
        model.addAttribute("isToday", date.equals(LocalDate.now()));

        return "meals";
    }
}
