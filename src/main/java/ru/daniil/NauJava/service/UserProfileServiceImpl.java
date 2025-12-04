package ru.daniil.NauJava.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.repository.UserProfileRepository;
import ru.daniil.NauJava.request.update.UpdateProfileRequest;
import ru.daniil.NauJava.service.activityLevel.ActivityLevelService;
import ru.daniil.NauJava.service.nutritionGoal.NutritionGoalService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final ActivityLevelService activityLevelService;
    private final NutritionGoalService nutritionGoalService;
    private final DailyReportRepository dailyReportRepository;
    private final UserService userService;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                              ActivityLevelService activityLevelService,
                              NutritionGoalService nutritionGoalService,
                              DailyReportRepository dailyReportRepository,
                              UserService userService) {
        this.userProfileRepository = userProfileRepository;
        this.activityLevelService = activityLevelService;
        this.nutritionGoalService = nutritionGoalService;
        this.dailyReportRepository = dailyReportRepository;
        this.userService = userService;
    }

    @Override
    public UserProfile getUserProfileByUser(User user) {
        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Профиль пользователя не найден"));
    }

    @Override
    public UserProfile getAuthUserProfile() {
        User user = userService.getAuthUser().orElseThrow();
        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Профиль пользователя не найден"));
    }

    @Override
    public void createUserProfileForUser(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfileRepository.save(userProfile);
    }

    public void updateUserProfile(UpdateProfileRequest request) {
        User currentUser = userService.getAuthUser().orElseThrow();
        UserProfile userProfile = getUserProfileByUser(currentUser);

        Double oldWeight = userProfile.getWeight();
        Integer oldHeight = userProfile.getHeight();
        ActivityLevel oldActivityLevel = userProfile.getActivityLevel();

        userProfile.setName(request.getName());
        userProfile.setSurname(request.getSurname());
        userProfile.setPatronymic(request.getPatronymic());
        userProfile.setDateOfBirth(request.getDateOfBirth());
        userProfile.setGender(request.getGender());
        userProfile.setHeight(request.getHeight());
        userProfile.setWeight(request.getWeight());
        userProfile.setTargetWeight(request.getTargetWeight());

        if (request.getActivityLevelId() != null) {
            ActivityLevel activityLevel = activityLevelService.getById(request.getActivityLevelId())
                    .orElseThrow(() -> new RuntimeException("Уровень активности не найден"));
            userProfile.setActivityLevel(activityLevel);
        } else {
            userProfile.setActivityLevel(null);
        }

        userProfileRepository.save(userProfile);
        boolean nutritionRecalculationNeeded =
                !Objects.equals(oldWeight, request.getWeight()) ||
                        !Objects.equals(oldHeight, request.getHeight()) ||
                        !Objects.equals(oldActivityLevel, userProfile.getActivityLevel()) ||
                        request.getTargetWeight() != null;

        if (nutritionRecalculationNeeded && hasCompleteProfile(userProfile)) {
            nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);
        }
    }

    @Override
    public void updateStreakIfNeeded(UserProfile userProfile) {
        LocalDate today = LocalDate.now();
        LocalDate lastUpdate = userProfile.getUpdatedAt().toLocalDate();

        if (!today.equals(lastUpdate)) {
            LocalDate yesterday = today.minusDays(1);
            boolean yesterdayGoalAchieved = dailyReportRepository
                    .findByUserIdAndReportDate(userProfile.getUser().getId(), yesterday)
                    .map(DailyReport::getGoalAchieved)
                    .orElse(false);

            if (yesterdayGoalAchieved) {
                userProfile.setCurrentStreak(userProfile.getCurrentStreak() + 1);
            } else {
                userProfile.setCurrentStreak(0);
            }

            userProfileRepository.save(userProfile);
        }
    }

    @Override
    public boolean hasCompleteProfile(UserProfile userProfile) {
        return userProfile.getDateOfBirth() != null &&
                userProfile.getGender() != null &&
                userProfile.getHeight() != null &&
                userProfile.getWeight() != null &&
                userProfile.getActivityLevel() != null;
    }

    private boolean isNutritionCalculationRequired(UpdateProfileRequest request, UserProfile existingProfile) {
        return !Objects.equals(request.getHeight(), existingProfile.getHeight()) ||
                !Objects.equals(request.getWeight(), existingProfile.getWeight()) ||
                !Objects.equals(request.getDateOfBirth(), existingProfile.getDateOfBirth()) ||
                !Objects.equals(request.getGender(), existingProfile.getGender()) ||
                !Objects.equals(request.getActivityLevelId(),
                        existingProfile.getActivityLevel() != null ? existingProfile.getActivityLevel().getId() : null);
    }

    /**
     * Получение профиля по логину пользователя
     */
    public UserProfile getProfileByUserLogin(String login) {
        User user = userService.findByLogin(login);
        return userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Профиль пользователя не найден"));
    }

    /**
     * Форматирование ФИО: Фамилия И.О.
     */
    public String formatFIO(UserProfile profile) {
        StringBuilder fio = new StringBuilder();

        // Фамилия полностью
        if (profile.getSurname() != null && !profile.getSurname().isEmpty()) {
            fio.append(profile.getSurname());
        } else {
            fio.append("-");
        }

        // Инициалы имени
        if (profile.getName() != null && !profile.getName().isEmpty()) {
            fio.append(" ");
            fio.append(profile.getName().charAt(0)).append(".");
        }

        // Инициалы отчества
        if (profile.getPatronymic() != null && !profile.getPatronymic().isEmpty()) {
            fio.append(profile.getPatronymic().charAt(0)).append(".");
        }

        return fio.toString().trim();
    }

    /**
     * Получение времени последнего обновления профиля
     */
    public LocalDateTime getLastProfileUpdate(Long userId) {
        return userProfileRepository.findLastUpdateByUserId(userId);
    }

    /**
     * Получение среднего значения currentStreak
     */
    public Double getAverageCurrentStreak() {
        return userProfileRepository.findAverageCurrentStreak();
    }

    /**
     * Получение всех профилей пользователей
     */
    public List<UserProfile> getAllUserProfiles() {
        return (List<UserProfile>) userProfileRepository.findAll();
    }
}