package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String patronymic;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Связь 1:1 с профилем
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    // Связь 1:М с отчетами
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyReport> dailyReports = new ArrayList<>();

    // Связь 1:М с продуктами (созданными пользователем)
    @OneToMany(mappedBy = "createdByUser", fetch = FetchType.LAZY)
    private List<Product> userProducts = new ArrayList<>();

    /**
     * Конструктор по умолчанию для пользователей.
     * Используется в полноценном конструкторе для
     * инициализации значений по умолчанию.
     */
    public User(){
        this.createdAt = LocalDateTime.now();
        this.currentStreak = 0;
    }

    /**
     * Конструктор для полноценной инициализации пользователя в БД.
     * Заполняет все основные поля информации у пользователя.
     * @param email указанная электронная почта
     * @param password значение для пароля, что будет занесено в БД
     * @param name имя пользователя
     * @param surname фамилия пользователя
     */
    public User(String email, String password,
                String name, String surname, String patronymic) {
        this();
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", currentStreak=" + currentStreak +
                ", createdAt=" + createdAt +
                '}';
    }
}
