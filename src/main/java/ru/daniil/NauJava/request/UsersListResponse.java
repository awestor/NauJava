package ru.daniil.NauJava.request;

public class UsersListResponse {
    private String login;
    private String email;
    private String fio;
    private Integer streak;
    private String lastActivity;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFio() { return fio; }
    public void setFio(String fio) { this.fio = fio; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }
}