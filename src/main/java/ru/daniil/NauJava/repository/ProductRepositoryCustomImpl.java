package ru.daniil.NauJava.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.Product;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findProductsWithMinCalories(Double minCalories) {
        CriteriaBuilder critBuild = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = critBuild.createQuery(Product.class);
        Root<Product> table = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (minCalories != null) {
            predicates.add(critBuild.greaterThanOrEqualTo(table.get("caloriesPer100g"), minCalories));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        CriteriaBuilder critBuild = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = critBuild.createQuery(Product.class);
        Root<Product> table = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            predicates.add(critBuild.like(critBuild.lower(table.get("name")), "%" + name.toLowerCase() + "%"));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}