package ru.daniil.NauJava.request;

public class MealTypeResponse {
    private String name;

    public MealTypeResponse() {}

    public MealTypeResponse(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
