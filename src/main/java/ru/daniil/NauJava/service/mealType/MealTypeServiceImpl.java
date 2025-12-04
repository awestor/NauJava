package ru.daniil.NauJava.service.mealType;

import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.MealType;
import ru.daniil.NauJava.repository.MealTypeRepository;

import java.util.List;

@Service
public class MealTypeServiceImpl implements MealTypeService {
    private final MealTypeRepository mealTypeRepository;

    public MealTypeServiceImpl(MealTypeRepository mealTypeRepository){
        this.mealTypeRepository = mealTypeRepository;
    }

    @Override
    public List<MealType> getMealTypes() {
        return (List<MealType>)mealTypeRepository.findAll();
    }
}
