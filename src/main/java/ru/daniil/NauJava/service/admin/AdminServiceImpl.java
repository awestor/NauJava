package ru.daniil.NauJava.service.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.response.UserDetailsResponse;
import ru.daniil.NauJava.response.UsersListResponse;
import ru.daniil.NauJava.response.UsersStatisticsResponse;
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
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final MealService mealService;

    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD-LOGGER");

    public AdminServiceImpl(UserService userService,
                            UserProfileService userProfileService,
                            MealService mealService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.mealService = mealService;
    }

    @Cacheable(value = "admin-users-list",
            key = "'users'")
    @Override
    public List<UsersListResponse> getUsersWithLastActivity() {
        methodLogger.info("{AdminServiceImpl.getUsersWithLastActivity} |" +
                " Данные в кеше отсутствуют. Происходит обращение к БД.");
        List<User> allUsers = userService.findAllUsers();
        System.out.println("В БД найдено " + allUsers.size() + " пользователей.");
        return allUsers.stream()
                .map(user -> {
                    System.out.println("Вход в цикл обработки пользователя с ID:" + user.getId() + ". Его имя: " + user.getUserProfile().getName());
                    UsersListResponse response = new UsersListResponse();
                    response.setLogin(user.getLogin());
                    response.setEmail(user.getEmail());

                    try {
                        methodLogger.info("{AdminServiceImpl.getUsersWithLastActivity} |" +
                                " Происходит обращение к userProfileService.getUserProfileByUser");
                        UserProfile profile = userProfileService.getUserProfileByUser(user);

                        methodLogger.info("{AdminServiceImpl.getUsersWithLastActivity} |" +
                                " Происходит обращение к userProfileService.formatFIO");
                        String fio = userProfileService.formatFIO(profile);
                        response.setFio(fio);
                        response.setStreak(profile.getCurrentStreak());
                    } catch (EntityNotFoundException e) {
                        response.setFio("-");
                        response.setStreak(0);
                    }
                    LocalDateTime lastActivity = getLastUserActivity(user.getId());
                    response.setLastActivity(lastActivity != null ?
                            lastActivity.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                            null);

                    return response;
                })
                .sorted((a, b) -> {
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
     * @param userId идентификатор пользователя
     * @return дату и время последней активности
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

    @Override
    public UserDetailsResponse getUserDetailsByLogin(String login) {
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
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

        LocalDateTime lastActivity = getLastUserActivity(user.getId());
        response.setLastActivity(lastActivity);

        return response;
    }

    @Cacheable(value = "admin-users-stats",
            key = "'stats'")
    @Override
    public UsersStatisticsResponse getUsersStatistics() {
        System.out.println("Данные в кеше отсутствуют. Происходит обращение к БД.");
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
