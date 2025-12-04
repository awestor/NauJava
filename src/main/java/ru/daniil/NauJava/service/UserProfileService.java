package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.request.update.UpdateProfileRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface UserProfileService {
    /**
     * Возвращает данные профиля переданного пользователя
     * @param user сущность пользователя
     * @return сущность с данными профиля пользователя
     */
    UserProfile getUserProfileByUser(User user);

    /**
     * Возвращает данные профиля авторизованного пользователя
     * @return сущность с данными профиля пользователя
     */
    UserProfile getAuthUserProfile();

    /**
     * Создаёт профиль для переданного пользователя
     * @param user сущность пользователя
     */
    void createUserProfileForUser(User user);

    /**
     * Обновляет данные профиля пользователя
     * @param request UpdateProfileRequest, что содержит данные для обновления
     */
    void updateUserProfile(UpdateProfileRequest request);

    /**
     * Обновляет Серию правильного питания, если нужно
     * @param userProfile профиль пользователя
     */
    void updateStreakIfNeeded(UserProfile userProfile);

    /**
     * Проверяет, полностью ли заполнен профиль пользователя
     * @param userProfile профиль пользователя
     * @return true или false
     */
    boolean hasCompleteProfile(UserProfile userProfile);

    UserProfile getProfileByUserLogin(String login);

    String formatFIO(UserProfile profile);
    LocalDateTime getLastProfileUpdate(Long userId);
    Double getAverageCurrentStreak();
    List<UserProfile> getAllUserProfiles();
}
