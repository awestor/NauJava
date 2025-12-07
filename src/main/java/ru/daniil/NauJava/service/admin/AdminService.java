package ru.daniil.NauJava.service.admin;

import ru.daniil.NauJava.response.UserDetailsResponse;
import ru.daniil.NauJava.response.UsersListResponse;
import ru.daniil.NauJava.response.UsersStatisticsResponse;

import java.util.List;

public interface AdminService {
    /**
     * Получение списка пользователей с информацией о последней активности
     * @return список пользователей с датой их последней активности
     */
    List<UsersListResponse> getUsersWithLastActivity();

    /**
     * Получение детальной информации о пользователе по логину
     * @param login логин пользователя
     * @return UserDetailsResponse, что содержит данные о пользователе и его последней активности
     */
    UserDetailsResponse getUserDetailsByLogin(String login);

    /**
     * Статистика по пользователям
     * @return UsersStatisticsResponse содержащий общую статистику по пользователям
     */
    UsersStatisticsResponse getUsersStatistics();
}
