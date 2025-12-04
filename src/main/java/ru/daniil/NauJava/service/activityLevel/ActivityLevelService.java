package ru.daniil.NauJava.service.activityLevel;

import ru.daniil.NauJava.entity.ActivityLevel;

import java.util.List;
import java.util.Optional;

public interface ActivityLevelService {
    /**
     * Возвращает уровень активности по его id
     * @param activityLevelId id уровня активности
     * @return уровень активности или null
     */
    Optional<ActivityLevel> getById(Long activityLevelId);

    /**
     * Возвращает все уровни активности, зарегистрированные в системе
     * @return список уровней активности
     */
    List<ActivityLevel> getAllActivityLevels();
}
