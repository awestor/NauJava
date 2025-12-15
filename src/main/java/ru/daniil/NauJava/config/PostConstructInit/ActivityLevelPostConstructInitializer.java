package ru.daniil.NauJava.config.PostConstructInit;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.repository.ActivityLevelRepository;

@Component
public class ActivityLevelPostConstructInitializer {

    private final ActivityLevelRepository activityLevelRepository;

    public ActivityLevelPostConstructInitializer(ActivityLevelRepository activityLevelRepository) {
        this.activityLevelRepository = activityLevelRepository;
    }

    @PostConstruct
    public void initializeActivityLevels() {
        createActivityLevelIfNotExists("Сидячий образ жизни", "Сижу дома и почти никуда не выхожу", 1.2);
        createActivityLevelIfNotExists("Легкая активность", "Иногда бегаю по утрам (1-3 раза в неделю)", 1.375);
        createActivityLevelIfNotExists("Умеренная активность", "Периодически выполняю комплекс упражнений (3-4 раз в неделю)", 1.55);
        createActivityLevelIfNotExists("Высокая активность ", "Занимаюсь спортом почти каждый день (5-6 раз в неделю)", 1.725);
        createActivityLevelIfNotExists("Ежедневная активность", "Очень высокая активность", 1.9);
    }

    private void createActivityLevelIfNotExists(String name, String description, Double multiplier) {
        if (activityLevelRepository.findByLevelName(name).isEmpty()) {
            ActivityLevel level = new ActivityLevel(name, description, multiplier);
            activityLevelRepository.save(level);
            System.out.println("Создан тип приёма пищи: " + name);
        }
    }
}
