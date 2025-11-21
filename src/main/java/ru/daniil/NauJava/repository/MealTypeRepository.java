package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Meal;
import ru.daniil.NauJava.entity.MealType;

import java.util.Optional;

@Repository
public interface MealTypeRepository extends CrudRepository<MealType, Long> {
    Optional<MealType> findByName(String name);
}
