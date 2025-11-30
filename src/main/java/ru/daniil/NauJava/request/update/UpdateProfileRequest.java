package ru.daniil.NauJava.request.update;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UpdateProfileRequest {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String surname;

    @Size(max = 50, message = "Отчество должно содержать не более 50 символов")
    private String patronymic;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[MF]$", message = "Пол должен быть M или F")
    private String gender;

    @Min(value = 100, message = "Рост должен быть не менее 100 см")
    @Max(value = 250, message = "Рост должен быть не более 250 см")
    private Integer height;

    @Min(value = 30, message = "Вес должен быть не менее 30 кг")
    @Max(value = 300, message = "Вес должен быть не более 300 кг")
    private Double weight;

    @Min(value = 30, message = "Целевой вес должен быть не менее 30 кг")
    @Max(value = 300, message = "Целевой вес должен быть не более 300 кг")
    private Double targetWeight;

    private Long activityLevelId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }
    public Long getActivityLevelId() { return activityLevelId; }
    public void setActivityLevelId(Long activityLevelId) { this.activityLevelId = activityLevelId; }
}
