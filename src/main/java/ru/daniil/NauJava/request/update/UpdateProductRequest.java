package ru.daniil.NauJava.request.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateProductRequest {
    @NotNull
    private Long id;

    @NotBlank(message = "Название продукта обязательно")
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private Double caloriesPer100g;

    @NotNull
    @DecimalMin("0.0")
    private Double proteinsPer100g;

    @NotNull
    @DecimalMin("0.0")
    private Double fatsPer100g;

    @NotNull
    @DecimalMin("0.0")
    private Double carbsPer100g;

    public UpdateProductRequest(){
    }

    public UpdateProductRequest(Long id, String name, Double caloriesPer100g, Double proteinsPer100g, Double fatsPer100g, Double carbsPer100g) {
        this.id = id;
        this.name = name;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinsPer100g = proteinsPer100g;
        this.fatsPer100g = fatsPer100g;
        this.carbsPer100g = carbsPer100g;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCaloriesPer100g() {
        return caloriesPer100g;
    }

    public void setCaloriesPer100g(Double caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
    }

    public Double getProteinsPer100g() {
        return proteinsPer100g;
    }

    public void setProteinsPer100g(Double proteinsPer100g) {
        this.proteinsPer100g = proteinsPer100g;
    }

    public Double getFatsPer100g() {
        return fatsPer100g;
    }

    public void setFatsPer100g(Double fatsPer100g) {
        this.fatsPer100g = fatsPer100g;
    }

    public Double getCarbsPer100g() {
        return carbsPer100g;
    }

    public void setCarbsPer100g(Double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }
}