package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.request.update.UpdateAccountRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Возвращает пользователя по его логину
     * @param login логин пользователя
     * @return Пользователь или null
     */
    Optional<User> findByLogin(String login);

    /**
     * Регистрирует нового пользователя в системе и назначает ему права роли "USER"
     * @param request RegistrationRequest что содержит регистрационные данные
     * @return сущность пользователя User
     */
    User registerUser(RegistrationRequest request);

    /**
     * Регистрирует нового пользователя в системе и назначает ему права роли переданные в roleName
     * если такая существует
     * @param request RegistrationRequest что содержит регистрационные данные
     * @return сущность пользователя User
     */
    User registerUserWithRole(RegistrationRequest request, String roleName);

    /**
     * Получает пользователя из если он авторизован
     * @return объект сущности пользователя или null
     */
    Optional<User> getAuthUser();

    /**
     * Возвращает всех пользователей зарегистрированных в БД
     * @return список пользователей
     */
    List<User> findAllUsers();

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
     * Считает количество пользователей зарегистрированных в системе
     * @return число пользователей
     */
    long countAllUsers();

    /**
     * Считает количество пользователей зарегистрированных в системе в диапазоне дат
     * @param start начало диапазона дат
     * @param end конец диапазона дат
     * @return число пользователей
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
