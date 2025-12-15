package ru.daniil.NauJava.service.nutritionGoal;

import ru.daniil.NauJava.entity.UserProfile;

public interface NutritionGoalService {
    /**
     * Пересчитывает целевые показатели для пользователя, создавая их при необходимости
     * @param userProfile профиль пользователя
     */
    void calculateAndUpdateNutritionGoal(UserProfile userProfile);
}
