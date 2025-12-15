package ru.daniil.NauJava.response;

public class UserDetailsResponse {
    private String login;
    private String email;
    private String name;
    private String surname;
    private String patronymic;
    private Integer currentStreak;
    private String activityLevel;
    private Integer dailyCalorieGoal;
    private String lastActivity;
    private String createdAt;

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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

    public Integer getCurrentStreak() {
        return currentStreak;
    }
    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public String getActivityLevel() {
        return activityLevel;
    }
    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Integer getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public String getLastActivity() {
        return lastActivity;
    }
    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}