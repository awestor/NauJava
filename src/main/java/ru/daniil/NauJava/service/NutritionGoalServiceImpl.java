package ru.daniil.NauJava.service;

import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.entity.NutritionGoal;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.repository.NutritionGoalRepository;

import java.time.LocalDate;
import java.time.Period;

@Service
public class NutritionGoalServiceImpl implements NutritionGoalService {
    private final NutritionGoalRepository nutritionGoalRepository;
    public NutritionGoalServiceImpl(NutritionGoalRepository nutritionGoalRepository){
        this.nutritionGoalRepository = nutritionGoalRepository;
    }
    @Override
    public void calculateAndUpdateNutritionGoal(UserProfile userProfile) {
        if (!hasRequiredDataForCalculation(userProfile)) {
            throw new RuntimeException("Недостаточно данных для расчета целевых показателей");
        }

        double bmr = calculateBMR(userProfile);

        double valueTDEE = calculateTDEE(bmr, userProfile.getActivityLevel());

        double adjustedCalories = adjustCaloriesForWeightGoal(valueTDEE, userProfile.getWeight(),
                userProfile.getTargetWeight());

        MacronutrientDistribution macros = calculateMacronutrients(adjustedCalories);

        NutritionGoal nutritionGoal = nutritionGoalRepository.findByUserProfile(userProfile)
                .orElse(new NutritionGoal(userProfile, (int) Math.round(adjustedCalories)));

        nutritionGoal.setDailyCalorieGoal((int) Math.round(adjustedCalories));
        nutritionGoal.setDailyProteinGoal(macros.protein);
        nutritionGoal.setDailyFatGoal(macros.fat);
        nutritionGoal.setDailyCarbsGoal(macros.carbs);

        nutritionGoalRepository.save(nutritionGoal);
        System.out.println("Обновление значений завершено успешно");
    }

    private boolean hasRequiredDataForCalculation(UserProfile userProfile) {
        return userProfile.getGender() != null &&
                userProfile.getDateOfBirth() != null &&
                userProfile.getHeight() != null &&
                userProfile.getWeight() != null &&
                userProfile.getActivityLevel() != null;
    }

    private double calculateBMR(UserProfile userProfile) {
        int age = Period.between(userProfile.getDateOfBirth(), LocalDate.now()).getYears();
        double height = userProfile.getHeight();
        double weight = userProfile.getWeight();

        // Формула Миффлина-Сан Жеора
        if ("M".equals(userProfile.getGender())) {
            // Для мужчин: BMR = 10 * вес(кг) + 6.25 * рост(см) - 5 * возраст(лет) + 5
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            // Для женщин: BMR = 10 * вес(кг) + 6.25 * рост(см) - 5 * возраст(лет) - 161
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
    }

    private double calculateTDEE(double bmr, ActivityLevel activityLevel) {
        // TDEE = BMR * множитель активности
        return bmr * activityLevel.getMultiplier();
    }

    private double adjustCaloriesForWeightGoal(double tdee, Double currentWeight, Double targetWeight) {
        if (targetWeight == null || targetWeight.equals(currentWeight)) {
            return tdee;
        }

        double weightDifference = targetWeight - currentWeight;

        if (weightDifference < 0) {
            // Похудение: дефицит 500 ккал в день для потери ~0.5 кг в неделю
            return Math.max(tdee - 500, tdee * 0.8);
        } else {
            // Набор массы: профицит 500 ккал в день для набора ~0.5 кг в неделю
            return tdee + 500;
        }
    }

    private MacronutrientDistribution calculateMacronutrients(double calories) {
        // Стандартное распределение макронутриентов:
        // Белки: 25% от калорий (1 г белка = 4 ккал)
        // Жиры: 25% от калорий (1 г жира = 9 ккал)
        // Углеводы: 50% от калорий (1 г углеводов = 4 ккал)

        double proteinCalories = calories * 0.25;
        double fatCalories = calories * 0.25;
        double carbCalories = calories * 0.5;

        double proteinGrams = proteinCalories / 4;
        double fatGrams = fatCalories / 9;
        double carbGrams = carbCalories / 4;

        return new MacronutrientDistribution(
                Math.round(proteinGrams * 10.0) / 10.0,
                Math.round(fatGrams * 10.0) / 10.0,
                Math.round(carbGrams * 10.0) / 10.0
        );
    }

    /**
     * Вспомогательный класс для хранения распределения макронутриентов
     * @param protein протеины
     * @param fat жиры
     * @param carbs углеводы
     */
    private record MacronutrientDistribution(double protein, double fat, double carbs) {
    }
}
