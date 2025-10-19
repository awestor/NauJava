package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_meal_entries")
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_grams")
    private Integer quantityGrams;

    @Column(name = "calculated_calories")
    private Integer calculatedCalories;

    @Column(name = "calculated_proteins")
    private Double calculatedProteins;

    @Column(name = "calculated_fats")
    private Double calculatedFats;

    @Column(name = "calculated_carbs")
    private Double calculatedCarbs;

    /**
     * Конструктор по умолчанию. Используется для
     * заполнения полей значениями по умолчанию.
     */
    public MealEntry() {}

    /**
     * Конструктор, что дополнительно указывает на приём пищи,
     * съеденный продукт, а также употреблённый вес.
     * @param meal приём пищи
     * @param product съеденный продукт
     * @param quantityGrams употреблённый вес
     */
    public MealEntry(Meal meal, Product product, Integer quantityGrams) {
        this.meal = meal;
        this.product = product;
        this.quantityGrams = quantityGrams;
        calculateNutrition();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Integer getCalculatedCalories() {
        return calculatedCalories;
    }

    public Double getCalculatedProteins() {
        return calculatedProteins;
    }

    public Double getCalculatedFats() {
        return calculatedFats;
    }

    public Double getCalculatedCarbs() {
        return calculatedCarbs;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantityGrams() {
        return quantityGrams;
    }

    public void calculateNutrition() {
        if (product != null && quantityGrams != null) {
            double ratio = quantityGrams / 100.0;
            this.calculatedCalories = (int) Math.round(product.getCaloriesPer100g() * ratio);
            this.calculatedProteins = product.getProteinsPer100g() * ratio;
            this.calculatedFats = product.getFatsPer100g() * ratio;
            this.calculatedCarbs = product.getCarbsPer100g() * ratio;
        }
    }

    public void updateQuantity(Integer newQuantityGrams) {
        this.quantityGrams = newQuantityGrams;
        calculateNutrition();
    }

    public Double getQuantityInKilos() {
        return quantityGrams / 1000.0;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (calculatedCalories == null) {
            calculateNutrition();
        }
    }

    @Override
    public String toString() {
        return "MealEntry{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantityGrams +
                ", calories=" + calculatedCalories +
                '}';
    }
}
