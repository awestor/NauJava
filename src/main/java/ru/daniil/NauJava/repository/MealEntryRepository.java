package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.MealEntry;

import java.util.List;

@Repository
@RepositoryRestResource(path = "mealEntries")
public interface MealEntryRepository extends CrudRepository<MealEntry, Long> {
    List<MealEntry> findByMealId(Long mealId);

    List<MealEntry> findByProductId(Long productId);

    long countByMealId(Long mealId);
}
