package ru.daniil.NauJava.response;

public class MealEntryResponse {
    private String productName;
    private Integer quantityGrams;

    public MealEntryResponse(String productName, Integer quantityGrams) {
        this.productName = productName;
        this.quantityGrams = quantityGrams;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public Integer getQuantityGrams() {
        return quantityGrams;
    }
    public void setQuantityGrams(Integer quantityGrams) {
        this.quantityGrams = quantityGrams;
    }
}
