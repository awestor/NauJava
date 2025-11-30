package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_nutrition_goal")
public class NutritionGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", unique = true, nullable = false)
    private UserProfile userProfile;

    @Column(name = "daily_calorie_goal")
    private Integer dailyCalorieGoal;

    @Column(name = "daily_protein_goal")
    private Double dailyProteinGoal;

    @Column(name = "daily_fat_goal")
    private Double dailyFatGoal;

    @Column(name = "daily_carbs_goal")
    private Double dailyCarbsGoal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public NutritionGoal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public NutritionGoal(UserProfile userProfile, Integer dailyCalorieGoal) {
        this();
        this.userProfile = userProfile;
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public Integer getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(Integer dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public Double getDailyProteinGoal() {
        return dailyProteinGoal;
    }

    public void setDailyProteinGoal(Double dailyProteinGoal) {
        this.dailyProteinGoal = dailyProteinGoal;
    }

    public Double getDailyFatGoal() {
        return dailyFatGoal;
    }

    public void setDailyFatGoal(Double dailyFatGoal) {
        this.dailyFatGoal = dailyFatGoal;
    }

    public Double getDailyCarbsGoal() {
        return dailyCarbsGoal;
    }

    public void setDailyCarbsGoal(Double dailyCarbsGoal) {
        this.dailyCarbsGoal = dailyCarbsGoal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
