package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tbl_meal_type")
public class MealType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "mealType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals = new ArrayList<>();

    public MealType(){
    }

    public MealType(String description, String name){
        this.description = description;
        this.name = name;
    }

    public void addMeal(Meal meal) {
        meals.add(meal);
        meal.setMealType(this);
    }

    public void removeMeal(Meal meal) {
        meals.remove(meal);
        meal.setMealType(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealType mealType = (MealType) o;
        return Objects.equals(id, mealType.id) &&
                Objects.equals(name, mealType.name);
    }
}
