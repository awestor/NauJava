package ru.daniil.NauJava.request;

public class NutritionSumResponse {
    private final Double totalCalories;
    private final Double totalProteins;
    private final Double totalFats;
    private final Double totalCarbs;

    public NutritionSumResponse(Double totalCalories, Double totalProteins,
                           Double totalFats, Double totalCarbs) {
        this.totalCalories = totalCalories != null ? totalCalories : 0.0;
        this.totalProteins = totalProteins != null ? totalProteins : 0.0;
        this.totalFats = totalFats != null ? totalFats : 0.0;
        this.totalCarbs = totalCarbs != null ? totalCarbs : 0.0;
    }

    public Double getTotalCalories() { return totalCalories; }
    public Double getTotalProteins() { return totalProteins; }
    public Double getTotalFats() { return totalFats; }
    public Double getTotalCarbs() { return totalCarbs; }
}
