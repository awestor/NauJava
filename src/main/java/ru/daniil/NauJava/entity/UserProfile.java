package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String patronymic;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 1)
    private String gender;

    private Integer height;

    private Double weight;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_level_id")
    private ActivityLevel activityLevel;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NutritionGoal nutritionGoal;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию для пользователей.
     * Модернизирован для инициализации значений по умолчанию.
     */
    public UserProfile() {
        this.name = null;
        this.surname = null;
        this.patronymic = null;
        this.updatedAt = LocalDateTime.now();
        this.gender = "M";
        this.height = null;
        this.weight = null;
        this.targetWeight = null;
        this.currentStreak = 0;
        this.activityLevel = null;
    }

    public UserProfile(String name, String surname, String patronymic) {
        this();
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setNutritionGoal(NutritionGoal nutritionGoal) {
        if (nutritionGoal == null) {
            if (this.nutritionGoal != null) {
                this.nutritionGoal.setUserProfile(null);
            }
        } else {
            nutritionGoal.setUserProfile(this);
        }
        this.nutritionGoal = nutritionGoal;
    }

    public NutritionGoal getNutritionGoal() {
        return nutritionGoal;
    }

    public Integer getDailyCalorieGoal() {
        return nutritionGoal != null ? nutritionGoal.getDailyCalorieGoal() : null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}