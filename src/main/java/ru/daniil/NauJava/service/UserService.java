package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.User;

public interface UserService {
    /**
     * Находит пользователя по email и возвращает его объект
     * @param email электронная почта пользователя
     * @return объект сущности пользователя
     */
    User findUserByEmail(String email);

    /**
     * Проверяет существование пользователя по email
     * @param email электронная почта пользователя
     * @return true - пользователя найден, иначе false
     */
    boolean userExists(String email);
}
