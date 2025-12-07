package ru.daniil.NauJava.response;

public class UsersStatisticsResponse {
    private Long totalUsers;
    private Long activeToday;
    private Long averageStreak;

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveToday() {
        return activeToday;
    }

    public void setActiveToday(Long activeToday) {
        this.activeToday = activeToday;
    }

    public Long getAverageStreak() {
        return averageStreak;
    }

    public void setAverageStreak(Long averageStreak) {
        this.averageStreak = averageStreak;
    }
}
