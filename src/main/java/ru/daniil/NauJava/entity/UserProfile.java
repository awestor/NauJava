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

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 1)
    private String gender;

    private Integer height;

    private Double weight;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "activity_level")
    private String activityLevel;

    @Column(name = "daily_calorie_goal", nullable = false)
    private Integer dailyCalorieGoal;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию для пользователей.
     * Модернизирован для инициализации значений по умолчанию.
     */
    public UserProfile(){
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
