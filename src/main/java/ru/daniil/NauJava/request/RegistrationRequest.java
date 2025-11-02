package ru.daniil.NauJava.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Логин обязателен")
    @Size(min = 6, message = "Логин должен содержать минимум 6 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и подчеркивания")
    @Schema(description = "Логин пользователя", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!$%^()_+\\-=\\[\\]{};:'\",.<>])[A-Za-z\\d!$%^()_+\\-=\\[\\]{};:'\",.<>]{8,}$",
            message = "Пароль должен содержать минимум 8 символов, включая заглавные и строчные буквы латинского алфавита, цифры и специальные символы (кроме @/|\\*#&?)")
    @Schema(description = "Пароль", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 6, message = "Имя пользователя должно содержать минимум 6 символов")
    @Schema(description = "Имя пользователя", example = "Иван", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    @Schema(description = "Фамилия пользователя", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
    private String surname;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    private String patronymic;

    public RegistrationRequest() {}

    public RegistrationRequest(String email, String password, String login,
                               String name, String surname, String patronymic) {
        this.email = email;
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public String getLogin() { return login; }

    public void setLogin(String login) { this.login = login; }
}
