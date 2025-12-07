package ru.daniil.NauJava.response;

public class CalendarDayResponse {
    private String date;
    private Boolean isGoalAchieved;

    public CalendarDayResponse(String date, Boolean isGoalAchieved) {
        this.date = date;
        this.isGoalAchieved = isGoalAchieved;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getIsGoalAchieved() {
        return isGoalAchieved;
    }

    public void setIsGoalAchieved(Boolean isGoalAchieved) {
        this.isGoalAchieved = isGoalAchieved;
    }
}
