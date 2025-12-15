package ru.daniil.NauJava.response;

import java.util.List;

public class MealResponse {
    private Long id;
    private String mealType;
    private List<MealEntryResponse> mealEntries;

    public MealResponse(Long id, String mealType, List<MealEntryResponse> mealEntries) {
        this.id = id;
        this.mealType = mealType;
        this.mealEntries = mealEntries;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public List<MealEntryResponse> getMealEntries() {
        return mealEntries;
    }

    public void setMealEntries(List<MealEntryResponse> mealEntries) {
        this.mealEntries = mealEntries;
    }
}
