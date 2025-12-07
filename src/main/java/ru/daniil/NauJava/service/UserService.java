package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.request.update.UpdateAccountRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Получает пользователя из если он авторизован
     * @return объект сущности пользователя или null
     */
    Optional<User> getAuthUser();

    /**
     * Изменяет указанные в request данные о пользователе в БД
     * @param request UpdateAccountRequest, что содержит данные для обновления
     */
    void updateUserAccount(UpdateAccountRequest request);

    /**
     * Проверяет существование пользователя по email
     * @param email электронная почта пользователя
     * @return true - пользователя найден, иначе false
     */
    boolean userExists(String email);

    /**
     * Регистрирует нового пользователя в системе и назначает ему права роли "USER"
     * @param request RegistrationRequest что содержит регистрационные данные
     * @return сущность пользователя User
     */
    User registerUser(RegistrationRequest request);

    Optional<User> findByLogin(String login);
    List<User> findAllUsers();
    long countAllUsers();
}
