package ru.daniil.NauJava.request;

public class NutritionSumResponse {
    private Double totalCalories;
    private Double totalProteins;
    private Double totalFats;
    private Double totalCarbs;

    public NutritionSumResponse(Double totalCalories, Double totalProteins,
                           Double totalFats, Double totalCarbs) {
        this.totalCalories = totalCalories != null ? totalCalories : 0.0;
        this.totalProteins = totalProteins != null ? totalProteins : 0.0;
        this.totalFats = totalFats != null ? totalFats : 0.0;
        this.totalCarbs = totalCarbs != null ? totalCarbs : 0.0;
    }

    public Double getTotalCalories() {
        return totalCalories;
    }
    public Double getTotalProteins() {
        return totalProteins;
    }
    public Double getTotalFats() {
        return totalFats;
    }
    public Double getTotalCarbs() {
        return totalCarbs;
    }
    public void setTotalCalories(Double totalCalories) {
        this.totalCalories = totalCalories;
    }
    public void setTotalProteins(Double totalProteins) {
        this.totalProteins = totalProteins;
    }
    public void setTotalFats(Double totalFats) {
        this.totalFats = totalFats;
    }
    public void setTotalCarbs(Double totalCarbs) {
        this.totalCarbs = totalCarbs;
    }
}
