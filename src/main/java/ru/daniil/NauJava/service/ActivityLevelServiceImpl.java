package ru.daniil.NauJava.service;

import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.repository.ActivityLevelRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityLevelServiceImpl implements ActivityLevelService{
    private final ActivityLevelRepository activityLevelRepository;
    public ActivityLevelServiceImpl(ActivityLevelRepository activityLevelRepository){
        this.activityLevelRepository = activityLevelRepository;
    }

    @Override
    public Optional<ActivityLevel> getById(Long id){
        return activityLevelRepository.findById(id);
    }

    @Override
    public List<ActivityLevel> getAllActivityLevels() {
        return (List<ActivityLevel>) activityLevelRepository.findAll();
    }
}
