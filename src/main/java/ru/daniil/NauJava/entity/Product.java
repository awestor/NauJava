package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "tbl_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "calories_per_100g", nullable = false)
    private Double caloriesPer100g;

    @Column(name = "proteins_per_100g", nullable = false)
    private Double proteinsPer100g;

    @Column(name = "fats_per_100g", nullable = false)
    private Double fatsPer100g;

    @Column(name = "carbs_per_100g", nullable = false)
    private Double carbsPer100g;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию. Используется для
     * заполнения полей значениями по умолчанию.
     */
    public Product() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Конструктор для полноценной инициализации продуктов в БД.
     * Заполняет все основные поля информации продукта используя параметры.
     * @param name название продукта
     * @param description описание продукта
     * @param caloriesPer100g количество калорий продукта на 100 грамм
     * @param proteinsPer100g количество белков в продукте на 100 грамм
     * @param fatsPer100g количество жиров в продукте на 100 грамм
     * @param carbsPer100g количество углеводов в продукте на 100 грамм
     */
    public Product(String name, String description, Double caloriesPer100g, Double proteinsPer100g,
                   Double fatsPer100g, Double carbsPer100g) {
        this();
        this.name = name;
        this.description = description;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinsPer100g = proteinsPer100g;
        this.fatsPer100g = fatsPer100g;
        this.carbsPer100g = carbsPer100g;
    }

    /**
     * Временно оставленный класс генерации продукта.
     * После исключения отработки консольной реализации из task 2 будет удалён.
     * @param id идентификатор создаваемого продукта
     * @param name название продукта
     * @param description описание продукта
     * @param caloriesPer100g калорийность на 100 грамм
     */
    @Deprecated
    public Product(Long id, String name, String description, Double caloriesPer100g) {
        this();
        this.name = name;
        this.description = description;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinsPer100g = 0.1;
        this.fatsPer100g = 0.1;
        this.carbsPer100g = 0.1;
    }


    //Сгенерированные гетеры
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", caloriesPer100g=" + caloriesPer100g +
                ", proteinsPer100g=" + proteinsPer100g +
                ", fatsPer100g=" + fatsPer100g +
                ", carbsPer100g=" + carbsPer100g +
                '}';
    }
}

