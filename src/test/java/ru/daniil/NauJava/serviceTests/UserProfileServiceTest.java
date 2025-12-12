package ru.daniil.NauJava.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.entity.DailyReport;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.repository.UserProfileRepository;
import ru.daniil.NauJava.request.update.UpdateProfileRequest;
import ru.daniil.NauJava.service.UserProfileServiceImpl;
import ru.daniil.NauJava.service.UserService;
import ru.daniil.NauJava.service.activityLevel.ActivityLevelService;
import ru.daniil.NauJava.service.nutritionGoal.NutritionGoalService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ActivityLevelService activityLevelService;

    @Mock
    private NutritionGoalService nutritionGoalService;

    @Mock
    private DailyReportRepository dailyReportRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User testUser;
    private UserProfile testProfile;
    private ActivityLevel moderateActivity;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "testUser", "password");
        testUser.setId(1L);

        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setName("Иван");
        testProfile.setSurname("Иванов");
        testProfile.setPatronymic("Иванович");
        testProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testProfile.setGender("M");
        testProfile.setHeight(180);
        testProfile.setWeight(75.0);
        testProfile.setTargetWeight(70.0);
        testProfile.setCurrentStreak(5);
        testProfile.setUpdatedAt(LocalDateTime.now().minusDays(1));

        moderateActivity = new ActivityLevel("Moderate", "Умеренная активность", 1.55);
        moderateActivity.setId(2L);
        testProfile.setActivityLevel(moderateActivity);

        updateRequest = new UpdateProfileRequest(
                "Петр",
                "Петров",
                "Петрович",
                LocalDate.of(1985, 5, 15),
                "M",
                175,
                80.0,
                75.0,
                2L
        );
    }

    @Test
    void getUserProfileByUser_WhenExists_ShouldReturnProfile() {
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));

        UserProfile result = userProfileService.getUserProfileByUser(testUser);

        assertThat(result).isEqualTo(testProfile);
        verify(userProfileRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getUserProfileByUser_WhenNotExists_ShouldThrowException() {
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userProfileService.getUserProfileByUser(testUser)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Профиль пользователя не найден");
    }

    @Test
    void getAuthUserProfile_WhenAuthenticated_ShouldReturnProfile() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));

        UserProfile result = userProfileService.getAuthUserProfile();

        assertThat(result).isEqualTo(testProfile);
        verify(userService, times(1)).getAuthUser();
        verify(userProfileRepository, times(1)).findByUser(testUser);
    }

    @Test
    void createUserProfileForUser_ShouldCreateProfile() {
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            profile.setId(2L);
            return profile;
        });

        userProfileService.createUserProfileForUser(testUser);

        verify(userProfileRepository, times(1)).save(argThat(profile ->
                profile.getUser() == testUser &&
                        profile.getName() == null &&
                        profile.getSurname() == null &&
                        profile.getPatronymic() == null
        ));
    }

    @Test
    void updateUserProfile_WhenValidData_ShouldUpdateProfile() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(activityLevelService.getById(2L)).thenReturn(Optional.of(moderateActivity));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        userProfileService.updateUserProfile(updateRequest);

        assertThat(testProfile.getName()).isEqualTo("Петр");
        assertThat(testProfile.getSurname()).isEqualTo("Петров");
        assertThat(testProfile.getPatronymic()).isEqualTo("Петрович");
        assertThat(testProfile.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 5, 15));
        assertThat(testProfile.getHeight()).isEqualTo(175);
        assertThat(testProfile.getWeight()).isEqualTo(80.0);
        assertThat(testProfile.getTargetWeight()).isEqualTo(75.0);
        assertThat(testProfile.getActivityLevel()).isEqualTo(moderateActivity);

        verify(userProfileRepository, times(1)).save(testProfile);
        verify(nutritionGoalService, times(1)).calculateAndUpdateNutritionGoal(testProfile);
    }

    @Test
    void updateUserProfile_WhenActivityLevelNotFound_ShouldThrowException() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(activityLevelService.getById(2L)).thenReturn(Optional.empty());

        UpdateProfileRequest invalidRequest = new UpdateProfileRequest(
                2L
        );

        assertThatThrownBy(() ->
                userProfileService.updateUserProfile(invalidRequest)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Уровень активности не найден");

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_WhenNoActivityLevelId_ShouldSetNull() {
        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        UpdateProfileRequest requestWithoutActivity = new UpdateProfileRequest(
                null
        );

        userProfileService.updateUserProfile(requestWithoutActivity);

        assertThat(testProfile.getActivityLevel()).isNull();
        verify(userProfileRepository, times(1)).save(testProfile);
    }

    @Test
    void updateUserProfile_WhenNoNutritionRecalculationNeeded_ShouldNotCallNutritionService() {
        testProfile.setWeight(80.0);
        testProfile.setHeight(175);

        when(userService.getAuthUser()).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        UpdateProfileRequest sameDataRequest = new UpdateProfileRequest(
                "НовоеИмя",
                175,
                80.0
        );

        userProfileService.updateUserProfile(sameDataRequest);

        verify(nutritionGoalService, never()).calculateAndUpdateNutritionGoal(any());
    }

    @Test
    void updateStreakIfNeeded_WhenGoalAchievedYesterday_ShouldIncrementStreak() {
        testProfile.setUpdatedAt(LocalDateTime.now().minusDays(2));
        DailyReport yesterdayReport = new DailyReport();
        yesterdayReport.setGoalAchieved(true);

        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now().minusDays(1)))
                .thenReturn(Optional.of(yesterdayReport));
        when(userProfileRepository.save(testProfile)).thenReturn(testProfile);

        int oldStreak = testProfile.getCurrentStreak();

        userProfileService.updateStreakIfNeeded(testProfile);

        assertThat(testProfile.getCurrentStreak()).isEqualTo(oldStreak + 1);
        verify(userProfileRepository, times(1)).save(testProfile);
    }

    @Test
    void updateStreakIfNeeded_WhenGoalNotAchievedYesterday_ShouldResetStreak() {
        testProfile.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testProfile.setCurrentStreak(5);
        DailyReport yesterdayReport = new DailyReport();
        yesterdayReport.setGoalAchieved(false);

        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now().minusDays(1)))
                .thenReturn(Optional.of(yesterdayReport));
        when(userProfileRepository.save(testProfile)).thenReturn(testProfile);

        userProfileService.updateStreakIfNeeded(testProfile);

        assertThat(testProfile.getCurrentStreak()).isEqualTo(0);
        verify(userProfileRepository, times(1)).save(testProfile);
    }

    @Test
    void updateStreakIfNeeded_WhenNoReportYesterday_ShouldResetStreak() {
        testProfile.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testProfile.setCurrentStreak(5);

        when(dailyReportRepository.findByUserIdAndReportDate(1L, LocalDate.now().minusDays(1)))
                .thenReturn(Optional.empty());
        when(userProfileRepository.save(testProfile)).thenReturn(testProfile);

        userProfileService.updateStreakIfNeeded(testProfile);

        assertThat(testProfile.getCurrentStreak()).isEqualTo(0);
        verify(userProfileRepository, times(1)).save(testProfile);
    }

    @Test
    void updateStreakIfNeeded_WhenUpdatedToday_ShouldNotChangeStreak() {
        testProfile.setUpdatedAt(LocalDateTime.now());
        int oldStreak = testProfile.getCurrentStreak();

        userProfileService.updateStreakIfNeeded(testProfile);

        assertThat(testProfile.getCurrentStreak()).isEqualTo(oldStreak);
        verify(userProfileRepository, never()).save(testProfile);
        verify(dailyReportRepository, never()).findByUserIdAndReportDate(anyLong(), any());
    }

    @Test
    void hasCompleteProfile_WhenAllDataPresent_ShouldReturnTrue() {
        UserProfile completeProfile = new UserProfile();
        completeProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        completeProfile.setGender("M");
        completeProfile.setHeight(180);
        completeProfile.setWeight(75.0);
        completeProfile.setActivityLevel(moderateActivity);

        boolean result = userProfileService.hasCompleteProfile(completeProfile);

        assertThat(result).isTrue();
    }

    @Test
    void hasCompleteProfile_WhenMissingData_ShouldReturnFalse() {
        UserProfile incompleteProfile = new UserProfile();
        incompleteProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        incompleteProfile.setGender("M");
        incompleteProfile.setHeight(180);
        incompleteProfile.setWeight(75.0);

        boolean result = userProfileService.hasCompleteProfile(incompleteProfile);

        assertThat(result).isFalse();
    }

    @Test
    void formatFIO_WhenAllDataPresent_ShouldFormatCorrectly() {
        testProfile.setSurname("Иванов");
        testProfile.setName("Иван");
        testProfile.setPatronymic("Иванович");

        String result = userProfileService.formatFIO(testProfile);

        assertThat(result).isEqualTo("Иванов И.И.");
    }

    @Test
    void formatFIO_WhenMissingName_ShouldFormatWithDash() {
        testProfile.setSurname(null);
        testProfile.setName("Иван");
        testProfile.setPatronymic("Иванович");

        String result = userProfileService.formatFIO(testProfile);

        assertThat(result).isEqualTo("- И.И.");
    }

    @Test
    void formatFIO_WhenMissingPatronymic_ShouldFormatWithoutIt() {
        testProfile.setSurname("Иванов");
        testProfile.setName("Иван");
        testProfile.setPatronymic(null);

        String result = userProfileService.formatFIO(testProfile);

        assertThat(result).isEqualTo("Иванов И.");
    }
}
