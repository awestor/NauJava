package ru.daniil.NauJava.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_daily_report")
public class DailyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_calories_consumed")
    private Double totalCaloriesConsumed = 0.0;

    @Column(name = "total_proteins_consumed")
    private Double totalProteinsConsumed = 0.0;

    @Column(name = "total_fats_consumed")
    private Double totalFatsConsumed = 0.0;

    @Column(name = "total_carbs_consumed")
    private Double totalCarbsConsumed = 0.0;

    @Column(name = "is_goal_achieved")
    private Boolean isGoalAchieved = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Связь 1:М с приемами пищи
    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Meal> meals = new ArrayList<>();

    /**
     * Конструктор по умолчанию. Используется для
     * заполнения полей значениями по умолчанию.
     */
    public DailyReport() {
        this.createdAt = LocalDateTime.now();
        this.totalCaloriesConsumed = 0.0;
        this.totalProteinsConsumed = 0.0;
        this.totalFatsConsumed = 0.0;
        this.totalCarbsConsumed = 0.0;
        this.isGoalAchieved = true;
    }

    /**
     * Конструктор, что дополнительно указывает пользователя
     * и дату за которую формируется отчёт.
     * @param user объект с данными по пользователю
     * @param reportDate дата(год, месяц, день) за которую формируется отчёт
     */
    public DailyReport(User user, LocalDate reportDate) {
        this();
        this.user = user;
        this.reportDate = reportDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTotalCarbsConsumed() {
        return totalCarbsConsumed;
    }

    public void setTotalCarbsConsumed(Double totalCarbsConsumed) {
        this.totalCarbsConsumed = totalCarbsConsumed;
    }

    public Double getTotalFatsConsumed() {
        return totalFatsConsumed;
    }

    public void setTotalFatsConsumed(Double totalFatsConsumed) {
        this.totalFatsConsumed = totalFatsConsumed;
    }

    public Double getTotalProteinsConsumed() {
        return totalProteinsConsumed;
    }

    public void setTotalProteinsConsumed(Double totalProteinsConsumed) {
        this.totalProteinsConsumed = totalProteinsConsumed;
    }

    public Double getTotalCaloriesConsumed() {
        return totalCaloriesConsumed;
    }

    public void setTotalCaloriesConsumed(Double totalCaloriesConsumed) {
        this.totalCaloriesConsumed = totalCaloriesConsumed;
    }

    public Boolean getGoalAchieved() {
        return isGoalAchieved;
    }

    public void setGoalAchieved(Boolean goalAchieved) {
        isGoalAchieved = goalAchieved;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (totalCaloriesConsumed == null) {
            totalCaloriesConsumed = 0.0;
        }
        if (isGoalAchieved == null) {
            isGoalAchieved = true;
        }
    }

    @Override
    public String toString() {
        return "DailyReport{" +
                "id=" + id +
                ", reportDate=" + reportDate +
                ", totalCalories=" + totalCaloriesConsumed +
                ", isGoalAchieved=" + isGoalAchieved +
                '}';
    }
}
