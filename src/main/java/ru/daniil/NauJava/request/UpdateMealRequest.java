package ru.daniil.NauJava.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateMealRequest {
    @NotNull(message = "ID приёма пищи обязателен")
    private Long id;

    @NotNull(message = "Тип приёма пищи обязателен")
    @NotEmpty(message = "Тип приёма пищи не может быть пустым")
    private String mealTypeName;

    @NotEmpty(message = "Должен быть хотя бы один продукт")
    @Size(max = 8, message = "Максимум 8 продуктов за раз")
    private List<@NotEmpty String> productNames;

    @NotEmpty(message = "Должны быть указаны количества")
    @Size(max = 8, message = "Максимум 8 продуктов за раз")
    private List<@NotNull Integer> quantities;

    public UpdateMealRequest() {
    }

    public UpdateMealRequest(Long id, String mealTypeName, List<String> productNames, List<Integer> quantities) {
        this.id = id;
        this.mealTypeName = mealTypeName;
        this.productNames = productNames;
        this.quantities = quantities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMealTypeName() {
        return mealTypeName;
    }

    public void setMealTypeName(String mealTypeName) {
        this.mealTypeName = mealTypeName;
    }

    public List<String> getProductNames() {
        return productNames;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }
}
