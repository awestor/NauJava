package ru.daniil.NauJava.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealEntry;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.repository.MealEntryRepository;
import ru.daniil.NauJava.response.NutritionSumResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class MealEntryServiceImpl implements MealEntryService {
    private final MealEntryRepository mealEntryRepository;
    private final ProductService productService;

    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD-LOGGER");

    public MealEntryServiceImpl(MealEntryRepository mealEntryRepository, ProductService productService){
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

            methodLogger.info("{MealEntryServiceImpl.createMealEntries} |" +
                    " Происходит вызов метода productService.findProductByName с productName:{}", productName);
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
        try {
            List<Object[]> result = mealEntryRepository.findNutritionSumByMealId(mealId);

            if (result == null || result.isEmpty() || result.get(0) == null) {
                return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
            }

            Object[] row = result.get(0);

            if (row.length < 4) {
                return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
            }

            Double calories = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
            Double proteins = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Double fats = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double carbs = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            return new NutritionSumResponse(calories, proteins, fats, carbs);
        } catch (Exception e) {
            return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public NutritionSumResponse getNutritionSumByDailyReportId(Long dailyReportId) {
        try {
            List<Object[]> result = mealEntryRepository.findNutritionSumByDailyReportId(dailyReportId);

            if (result == null || result.isEmpty() || result.get(0) == null) {
                return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
            }

            Object[] row = result.get(0);

            if (row.length < 4) {
                return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
            }

            Double calories = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
            Double proteins = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Double fats = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double carbs = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            return new NutritionSumResponse(calories, proteins, fats, carbs);
        } catch (Exception e) {
            return new NutritionSumResponse(0.0, 0.0, 0.0, 0.0);
        }
    }

    public List<MealEntry> getAllByMealId(Long mealId){
        return mealEntryRepository.findByMealId(mealId);
    }

    public void deleteByMealId(Long mealId){
        mealEntryRepository.deleteByMealId(mealId);
    }
}
