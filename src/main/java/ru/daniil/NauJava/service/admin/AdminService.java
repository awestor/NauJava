package ru.daniil.NauJava.service.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.request.UserDetailsResponse;
import ru.daniil.NauJava.request.UsersListResponse;
import ru.daniil.NauJava.request.UsersStatisticsResponse;
import ru.daniil.NauJava.service.MealService;
import ru.daniil.NauJava.service.UserProfileService;
import ru.daniil.NauJava.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final MealService mealService;

    public AdminService(UserService userService,
                        UserProfileService userProfileService,
                        MealService mealService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.mealService = mealService;
    }

    /**
     * Получение списка пользователей с информацией о последней активности
     */
    public List<UsersListResponse> getUsersWithLastActivity() {
        List<User> allUsers = userService.findAllUsers();

        return allUsers.stream()
                .map(user -> {
                    UsersListResponse response = new UsersListResponse();
                    response.setLogin(user.getLogin());
                    response.setEmail(user.getEmail());

                    try {
                        UserProfile profile = userProfileService.getUserProfileByUser(user);

                        // Формируем ФИО
                        String fio = userProfileService.formatFIO(profile);
                        response.setFio(fio);
                        response.setStreak(profile.getCurrentStreak());
                    } catch (EntityNotFoundException e) {
                        response.setFio("-");
                        response.setStreak(0);
                    }

                    // Определяем последнюю активность
                    LocalDateTime lastActivity = getLastUserActivity(user.getId());
                    response.setLastActivity(lastActivity != null ?
                            lastActivity.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                            null);

                    return response;
                })
                .sorted((a, b) -> {
                    // Сортировка по последней активности (сначала новые)
                    LocalDateTime aDate = a.getLastActivity() != null ?
                            LocalDateTime.parse(a.getLastActivity()) :
                            LocalDateTime.MIN;
                    LocalDateTime bDate = b.getLastActivity() != null ?
                            LocalDateTime.parse(b.getLastActivity()) :
                            LocalDateTime.MIN;
                    return bDate.compareTo(aDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Определение последней активности пользователя
     */
    private LocalDateTime getLastUserActivity(Long userId) {
        LocalDateTime lastMealActivity = mealService.getLastMealActivityByUserId(userId);
        LocalDateTime lastProfileUpdate = userProfileService.getLastProfileUpdate(userId);

        if (lastMealActivity == null && lastProfileUpdate == null) {
            return null;
        }

        if (lastMealActivity == null) {
            return lastProfileUpdate;
        }

        if (lastProfileUpdate == null) {
            return lastMealActivity;
        }

        return lastMealActivity.isAfter(lastProfileUpdate) ? lastMealActivity : lastProfileUpdate;
    }

    /**
     * Получение детальной информации о пользователе по логину
     */
    public UserDetailsResponse getUserDetailsByLogin(String login) {
        User user = userService.findByLogin(login);
        UserProfile profile = userProfileService.getUserProfileByUser(user);

        UserDetailsResponse response = new UserDetailsResponse();
        response.setLogin(user.getLogin());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());

        response.setName(profile.getName());
        response.setSurname(profile.getSurname());
        response.setPatronymic(profile.getPatronymic());
        response.setCurrentStreak(profile.getCurrentStreak());

        if (profile.getActivityLevel() != null) {
            response.setActivityLevel(profile.getActivityLevel().getLevelName());
        }

        if (profile.getNutritionGoal() != null) {
            response.setDailyCalorieGoal(profile.getNutritionGoal().getDailyCalorieGoal());
        }

        // Определяем последнюю активность
        LocalDateTime lastActivity = getLastUserActivity(user.getId());
        response.setLastActivity(lastActivity);

        return response;
    }

    /**
     * Статистика по пользователям
     */
    public UsersStatisticsResponse getUsersStatistics() {
        long totalUsers = userService.countAllUsers();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long activeToday = mealService.countUsersWithActivityAfter(todayStart);

        Double avgStreak = userProfileService.getAverageCurrentStreak();

        UsersStatisticsResponse response = new UsersStatisticsResponse();
        response.setTotalUsers(totalUsers);
        response.setActiveToday(activeToday != null ? activeToday : 0);
        response.setAverageStreak(avgStreak != null ? Math.round(avgStreak) : 0);

        return response;
    }
}
