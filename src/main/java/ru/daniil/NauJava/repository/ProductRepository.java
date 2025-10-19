package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCreatedByUserIsNull();

    List<Product> findByCreatedByUserId(Long userId);

    @Query("SELECT p FROM Product p WHERE p.caloriesPer100g >= :minCalories")
    List<Product> findProductsWithMinCalories(@Param("minCalories") Double minCalories);

    List<Product> findByCreatedByUserIsNullOrCreatedByUserId(Long userId);

    List<Product> findByNameAndCaloriesPer100gBetween(String name, Integer minCalories, Integer maxCalories);

    Optional<Product> findByNameIgnoreCaseAndCreatedByUserIsNull(String name);

    Optional<Product> findByNameIgnoreCaseAndCreatedByUserId(String name, Long userId);

    boolean existsByNameIgnoreCase(String name);
}
