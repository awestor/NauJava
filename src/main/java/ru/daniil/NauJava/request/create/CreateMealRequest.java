package ru.daniil.NauJava.request.create;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateMealRequest {
    @NotNull(message = "Тип приёма пищи обязателен")
    @NotEmpty(message = "Тип приёма пищи не может быть пустым")
    private String mealTypeName;

    @Size(max = 8, message = "Максимум 8 продуктов за раз")
    private List<String> productNames;

    @Size(max = 8, message = "Максимум 8 продуктов за раз")
    private List<Integer> quantities;

    public CreateMealRequest(){
    }

    public CreateMealRequest(String mealTypeName, List<String> productNames, List<Integer> quantities){
        this.mealTypeName = mealTypeName;
        this.productNames = productNames;
        this.quantities = quantities;
    }

    public String getMealTypeName() {
        return mealTypeName;
    }

    public List<String> getProductNames() {
        return productNames;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setMealTypeName(String mealTypeName) {
        this.mealTypeName = mealTypeName;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }
}
