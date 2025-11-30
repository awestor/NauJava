package ru.daniil.NauJava.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.daniil.NauJava.entity.MealType;
import ru.daniil.NauJava.repository.MealTypeRepository;

@Component
public class MealTypePostConstructInitializer {

    private final MealTypeRepository mealTypeRepository;

    public MealTypePostConstructInitializer(MealTypeRepository mealTypeRepository) {
        this.mealTypeRepository = mealTypeRepository;
    }

    @PostConstruct
    public void initializeMealTypes() {
        createMealTypeIfNotExists("Перекус", "Приём пищи в любое время");
        createMealTypeIfNotExists("Завтрак", "Приём пищи до 10:30");
        createMealTypeIfNotExists("Полдник", "Приём пищи с 10:30 до 13:00");
        createMealTypeIfNotExists("Обед", "Приём пищи с 13:00 до 16:00");
        createMealTypeIfNotExists("Второй обед", "Приём пищи с 16:00 до 18:00");
        createMealTypeIfNotExists("Ужин", "Приём пищи с 18:00 до 22:00");
        createMealTypeIfNotExists("Поздний ужин", "Приём пищи после 22:00");
    }

    private void createMealTypeIfNotExists(String name, String description) {
        if (mealTypeRepository.findByName(name).isEmpty()) {
            MealType mealType = new MealType(name, description);
            mealTypeRepository.save(mealType);
            System.out.println("Создан тип приёма пищи: " + name);
        }
    }
}
