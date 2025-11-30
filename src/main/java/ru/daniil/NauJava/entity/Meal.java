package ru.daniil.NauJava.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_meals")
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id")
    private DailyReport dailyReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_type_id")
    private MealType mealType;

    @Column(name = "eaten_at")
    private LocalDateTime eatenAt;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MealEntry> mealEntries = new ArrayList<>();

    /**
     * Конструктор по умолчанию. Используется для
     * заполнения полей значениями по умолчанию.
     */
    public Meal() {
        this.eatenAt = LocalDateTime.now();
    }

    /**
     * Конструктор, что дополнительно указывает на отчёт,
     * а также тип приёма пищи.
     * @param dailyReport отчёт к которому принадлежит
     * @param mealType тип приёма пищи
     */
    public Meal(DailyReport dailyReport, MealType mealType) {
        this();
        this.dailyReport = dailyReport;
        this.mealType = mealType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DailyReport getDailyReport() {
        return dailyReport;
    }

    public void setDailyReport(DailyReport dailyReport) {
        this.dailyReport = dailyReport;
    }

    public void addMealEntry(MealEntry mealEntry) {
        mealEntries.add(mealEntry);
        mealEntry.setMeal(this);
    }

    public List<MealEntry> getMealEntries() {
        return mealEntries;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
    public String getMealType() {
        return mealType.getName();
    }

    public Integer getTotalCalories() {
        return mealEntries.stream()
                .mapToInt(MealEntry::getCalculatedCalories)
                .sum();
    }

    public Double getTotalProteins() {
        return mealEntries.stream()
                .mapToDouble(MealEntry::getCalculatedProteins)
                .sum();
    }

    public Double getTotalFats() {
        return mealEntries.stream()
                .mapToDouble(MealEntry::getCalculatedFats)
                .sum();
    }

    public Double getTotalCarbs() {
        return mealEntries.stream()
                .mapToDouble(MealEntry::getCalculatedCarbs)
                .sum();
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id=" + id +
                ", mealType='" + mealType + '\'' +
                ", eatenAt=" + eatenAt +
                ", entriesCount=" + mealEntries.size() +
                '}';
    }
}