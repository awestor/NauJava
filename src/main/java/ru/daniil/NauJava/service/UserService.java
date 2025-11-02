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
     * Проверяет существование пользователя по email
     * @param email электронная почта пользователя
     * @return true - пользователя найден, иначе false
     */
    boolean userExists(String email);
}
