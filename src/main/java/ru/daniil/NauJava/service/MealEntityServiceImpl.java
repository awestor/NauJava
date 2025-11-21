package ru.daniil.NauJava.service;

import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.request.NutritionSumResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MealEntityServiceImpl implements MealEntityService {
    private final MealEntryRepository mealEntryRepository;
    private final ProductService productService;

    public MealEntityServiceImpl(MealEntryRepository mealEntryRepository, ProductService productService){
        this.mealEntryRepository = mealEntryRepository;
        this.productService = productService;
    }

    @Override
    public List<MealEntry> createMealEntries(Meal meal, List<String> productNames, List<Integer> quantities) {
        if (productNames.size() != quantities.size()) {
            throw new IllegalArgumentException("Количество продуктов и весов не совпадает");
        }

        List<MealEntry> mealEntries = new ArrayList<>();

        for (int i = 0; i < productNames.size(); i++) {
            String productName = productNames.get(i);
            Integer quantity = quantities.get(i);

            Product product = productService.findProductByName(productName);
            if (product == null) {
                throw new IllegalArgumentException("Продукт из списка потреблённых не найден: " + productName);
            }

            MealEntry mealEntry = createMealEntry(meal, product, quantity);
            mealEntries.add(mealEntry);
        }

        return mealEntries;
    }

    @Override
    public MealEntry createMealEntry(Meal meal, Product product, Integer quantity) {
        MealEntry mealEntry = new MealEntry(meal, product, quantity);

        return mealEntryRepository.save(mealEntry);
    }

    @Override
    public NutritionSumResponse getNutritionSumByMealId(Long mealId) {
        List<Double> result = mealEntryRepository.findNutritionSumByMealId(mealId);

        if (result.isEmpty() || result.get(0) == 0.0) {
            return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
        }

        return new NutritionSumResponse(result.get(0), result.get(1),
                result.get(2), result.get(3));
    }

    @Override
    public NutritionSumResponse getNutritionSumByDailyReportId(Long dailyReportId) {
        List<Double> result = mealEntryRepository.findNutritionSumByDailyReportId(dailyReportId);

        if (result.isEmpty() || result.get(0) == 0.0) {
            return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
        }

        return new NutritionSumResponse(result.get(0), result.get(1),
                result.get(2), result.get(3));
    }
}
