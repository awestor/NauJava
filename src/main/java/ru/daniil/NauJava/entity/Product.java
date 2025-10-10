package ru.daniil.NauJava.entity;

public class Product {
    private final Long id;
    private String name;
    private String description;
    private Double calories;

    /**
     * Конструктор для неполного заполнения полей,
     * а только первичного создания.
     * @param id
     */
    public Product(Long id){
        this.id = id;
    }

    /**
     * Конструктор для полного заполнения полей класса
     * не используя сетеры.
     * @param id
     * @param name
     * @param description
     * @param calories
     */
    public Product(Long id, String name, String description, double calories){
        this.id = id;
        this.name = name;
        this.description = description;
        this.calories = calories;
    }

    //Сгенерированные гетеры
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getCalories() {
        return calories;
    }

    //Сгенерированные сетеры
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }
}

