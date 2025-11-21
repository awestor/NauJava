package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.request.RegistrationRequest;

import java.util.Optional;

public interface UserService {
    /**
     * Находит пользователя по id и возвращает его объект
     * @param userId идентификатор пользователя
     * @return объект сущности пользователя или null
     */
    Optional<User> findUserById(Long userId);

    /**
     * Находит пользователя по email и возвращает его объект
     * @param email электронная почта пользователя
     * @return объект сущности пользователя или null
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Получает пользователя из если он авторизован
     * @return объект сущности пользователя или null
     */
    Optional<User> getAuthUser();

    /**
     * Обновляет логин авторизованного пользователя на новый
     * @param newLogin идентификатор пользователя
     */
    boolean updateLogin(String newLogin);

    /**
     * Обновляет пароль авторизованного пользователя на новый
     * @param newPassword идентификатор пользователя
     */
    boolean updatePassword(String newPassword);

    /**
     * Проверяет существование пользователя по email
     * @param email электронная почта пользователя
     * @return true - пользователя найден, иначе false
     */
    boolean userExists(String email);
}
