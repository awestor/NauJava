package ru.daniil.NauJava.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tbl_product",
    uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"name", "created_by_user_id"},
                name = "unique_product_name_user"
        )
    }
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

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
    @JsonIgnore
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию. Используется для
     * заполнения поля createdAt.
     */
    public Product() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Конструктор для полноценной инициализации продуктов в БД.
     * Заполняет все основные поля информации продукта используя параметры.
     * @param name название продукта
     * @param caloriesPer100g количество калорий продукта на 100 грамм
     * @param proteinsPer100g количество белков в продукте на 100 грамм
     * @param fatsPer100g количество жиров в продукте на 100 грамм
     * @param carbsPer100g количество углеводов в продукте на 100 грамм
     */
    public Product(String name, Double caloriesPer100g, Double proteinsPer100g,
                   Double fatsPer100g, Double carbsPer100g) {
        this();
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
                ", caloriesPer100g=" + caloriesPer100g +
                ", proteinsPer100g=" + proteinsPer100g +
                ", fatsPer100g=" + fatsPer100g +
                ", carbsPer100g=" + carbsPer100g +
                '}';
    }
}

