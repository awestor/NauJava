package ru.daniil.NauJava.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ru.daniil.NauJava.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyReportResponse {
    private Long id;

    private String reportDate;

    private Double totalCaloriesConsumed = 0.0;

    private Double totalProteinsConsumed = 0.0;

    private Double totalFatsConsumed = 0.0;

    private Double totalCarbsConsumed = 0.0;

    private Boolean isGoalAchieved = true;

    public DailyReportResponse() {
    }

    public DailyReportResponse(Long id,
                               Double totalCaloriesConsumed, Double totalProteinsConsumed,
                               Double totalFatsConsumed, Double totalCarbsConsumed,
                               Boolean isGoalAchieved, String reportDate) {
        this.id = id;
        this.totalCaloriesConsumed = totalCaloriesConsumed;
        this.totalProteinsConsumed = totalProteinsConsumed;
        this.totalFatsConsumed = totalFatsConsumed;
        this.totalCarbsConsumed = totalCarbsConsumed;
        this.isGoalAchieved = isGoalAchieved;
        this.reportDate = reportDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public Double getTotalCaloriesConsumed() {
        return totalCaloriesConsumed;
    }

    public void setTotalCaloriesConsumed(Double totalCaloriesConsumed) {
        this.totalCaloriesConsumed = totalCaloriesConsumed;
    }

    public Double getTotalProteinsConsumed() {
        return totalProteinsConsumed;
    }

    public void setTotalProteinsConsumed(Double totalProteinsConsumed) {
        this.totalProteinsConsumed = totalProteinsConsumed;
    }

    public Double getTotalFatsConsumed() {
        return totalFatsConsumed;
    }

    public void setTotalFatsConsumed(Double totalFatsConsumed) {
        this.totalFatsConsumed = totalFatsConsumed;
    }

    public Double getTotalCarbsConsumed() {
        return totalCarbsConsumed;
    }

    public void setTotalCarbsConsumed(Double totalCarbsConsumed) {
        this.totalCarbsConsumed = totalCarbsConsumed;
    }

    public Boolean getGoalAchieved() {
        return isGoalAchieved;
    }

    public void setGoalAchieved(Boolean goalAchieved) {
        isGoalAchieved = goalAchieved;
    }
}
