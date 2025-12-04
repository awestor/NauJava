package ru.daniil.NauJava.service.mealType;

import ru.daniil.NauJava.entity.MealType;

import java.util.List;

public interface MealTypeService {
    /**
     * Находит и возвращает типы приёмов пищи, зарегистрированные в системе
     * @return типы приёмов пищи
     */
    List<MealType> getMealTypes();
}
