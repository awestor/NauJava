package ru.daniil.NauJava.mockTests;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.entity.NutritionGoal;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.response.UserDetailsResponse;
import ru.daniil.NauJava.response.UsersListResponse;
import ru.daniil.NauJava.response.UsersStatisticsResponse;
import ru.daniil.NauJava.service.MealService;
import ru.daniil.NauJava.service.UserProfileService;
import ru.daniil.NauJava.service.UserService;
import ru.daniil.NauJava.service.admin.AdminServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private MealService mealService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user1;
    private User user2;
    private UserProfile profile1;
    private UserProfile profile2;


    @BeforeEach
    void setUp() {
        ActivityLevel activityLevel = new ActivityLevel
                ("Умеренный", "Средняя активность", 1.55);
        activityLevel.setId(1L);

        user1 = new User(
                "user1@test.com",
                "user1",
                "Password123!"
        );
        user1.setId(1L);
        user1.setCreatedAt(LocalDateTime.now().minusDays(10));

        user2 = new User(
                "user2@test.com",
                "user2",
                "Password123!"
        );
        user2.setId(2L);
        user2.setCreatedAt(LocalDateTime.now().minusDays(5));

        profile1 = new UserProfile(
                "Иван",
                "Иванов",
                "Иванович"
        );
        profile1.setId(1L);
        profile1.setCurrentStreak(5);
        profile1.setGender("M");
        profile1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        profile1.setHeight(180);
        profile1.setWeight(75.0);
        profile1.setActivityLevel(activityLevel);
        profile1.setNutritionGoal(new NutritionGoal(profile1, 2000));

        profile2 = new UserProfile(
                "Петр",
                "Петров",
                null
        );
        profile2.setId(1L);
        profile2.setCurrentStreak(3);

        user1.setUserProfile(profile1);
        user2.setUserProfile(profile2);
    }

    @Test
    void getUsersWithLastActivity_Success() {
        List<User> users = Arrays.asList(user1, user2);
        when(userService.findAllUsers()).thenReturn(users);
        when(userProfileService.getUserProfileByUser(user1)).thenReturn(profile1);
        when(userProfileService.getUserProfileByUser(user2)).thenReturn(profile2);
        when(userProfileService.formatFIO(profile1)).thenReturn("Иванов Иван Иванович");
        when(userProfileService.formatFIO(profile2)).thenReturn("Петров Петр");

        LocalDateTime lastActivity1 = LocalDateTime.now().minusHours(2);
        LocalDateTime lastActivity2 = LocalDateTime.now().minusHours(1);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(lastActivity1);
        when(mealService.getLastMealActivityByUserId(2L)).thenReturn(lastActivity2);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(lastActivity1.minusDays(1));
        when(userProfileService.getLastProfileUpdate(2L)).thenReturn(lastActivity2.minusDays(1));

        List<UsersListResponse> result = adminService.getUsersWithLastActivity();

        assertNotNull(result);
        assertEquals(2, result.size());

        UsersListResponse response1 = result.get(0);
        assertEquals("user2", response1.getLogin());
        assertEquals("user2@test.com", response1.getEmail());
        assertEquals("Петров Петр", response1.getFio());
        assertEquals(3, response1.getStreak());
        assertNotNull(response1.getLastActivity());

        UsersListResponse response2 = result.get(1);
        assertEquals("user1", response2.getLogin());
        assertEquals("user1@test.com", response2.getEmail());
        assertEquals("Иванов Иван Иванович", response2.getFio());
        assertEquals(5, response2.getStreak());
        assertNotNull(response2.getLastActivity());

        verify(userService, times(1)).findAllUsers();
        verify(userProfileService, times(2)).getUserProfileByUser(any(User.class));
    }

    @Test
    void getUsersWithLastActivity_OrderByLastActivityDesc() {
        List<User> users = Arrays.asList(user1, user2);
        when(userService.findAllUsers()).thenReturn(users);
        when(userProfileService.getUserProfileByUser(user1)).thenReturn(profile1);
        when(userProfileService.getUserProfileByUser(user2)).thenReturn(profile2);

        LocalDateTime recentActivity = LocalDateTime.now().minusHours(1);
        LocalDateTime olderActivity = LocalDateTime.now().minusDays(2);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(recentActivity);
        when(mealService.getLastMealActivityByUserId(2L)).thenReturn(olderActivity);
        when(userProfileService.getLastProfileUpdate(anyLong())).thenReturn(null);

        List<UsersListResponse> result = adminService.getUsersWithLastActivity();

        assertEquals(2, result.size());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime firstActivity = LocalDateTime.parse(result.get(0).getLastActivity(), formatter);
        LocalDateTime secondActivity = LocalDateTime.parse(result.get(1).getLastActivity(), formatter);

        assertTrue(firstActivity.isAfter(secondActivity));
    }

    @Test
    void getUserDetailsByLogin_Success() {
        String login = "user1";
        when(userService.findByLogin(login)).thenReturn(Optional.of(user1));
        when(userProfileService.getUserProfileByUser(user1)).thenReturn(profile1);

        LocalDateTime lastActivity = LocalDateTime.now().minusHours(3);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(lastActivity);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(lastActivity.minusDays(1));

        UserDetailsResponse result = adminService.getUserDetailsByLogin(login);

        assertNotNull(result);
        assertEquals("user1", result.getLogin());
        assertEquals("user1@test.com", result.getEmail());
        assertEquals("Иван", result.getName());
        assertEquals("Иванов", result.getSurname());
        assertEquals("Иванович", result.getPatronymic());
        assertEquals(5, result.getCurrentStreak());
        assertEquals("Умеренный", result.getActivityLevel());
        assertEquals(2000, result.getDailyCalorieGoal());
        assertEquals(lastActivity, result.getLastActivity());

        verify(userService, times(1)).findByLogin(login);
        verify(userProfileService, times(1)).getUserProfileByUser(user1);
    }

    @Test
    void getUserDetailsByLogin_UserNotFound() {
        String login = "nonexistent";
        when(userService.findByLogin(login)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.getUserDetailsByLogin(login)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userService, times(1)).findByLogin(login);
    }

    @Test
    void getUserDetailsByLogin_WithoutActivityLevelAndNutritionGoal() {
        when(userService.findByLogin("user2")).thenReturn(Optional.of(user2));

        when(userProfileService.getUserProfileByUser(user2)).thenReturn(profile2);

        when(mealService.getLastMealActivityByUserId(2L)).thenReturn(null);
        when(userProfileService.getLastProfileUpdate(2L)).thenReturn(null);

        UserDetailsResponse result = adminService.getUserDetailsByLogin("user2");

        assertNotNull(result);
        assertEquals("Петр", result.getName());
        assertEquals("Петров", result.getSurname());
        assertNull(result.getPatronymic());
        assertEquals(3, result.getCurrentStreak());
        assertNull(result.getActivityLevel());
        assertNull(result.getDailyCalorieGoal());
        assertNull(result.getLastActivity());
    }

    @Test
    void getUsersStatistics_Success() {
        when(userService.countAllUsers()).thenReturn(100L);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        when(mealService.countUsersWithActivityAfter(todayStart)).thenReturn(25L);

        when(userProfileService.getAverageCurrentStreak()).thenReturn(4.7);

        UsersStatisticsResponse result = adminService.getUsersStatistics();

        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(25L, result.getActiveToday());
        assertEquals(5L, result.getAverageStreak());

        verify(userService, times(1)).countAllUsers();
        verify(mealService, times(1)).countUsersWithActivityAfter(todayStart);
        verify(userProfileService, times(1)).getAverageCurrentStreak();
    }

    @Test
    void getUsersStatistics_WithNullValues() {
        when(userService.countAllUsers()).thenReturn(50L);
        when(mealService.countUsersWithActivityAfter(any())).thenReturn(null);
        when(userProfileService.getAverageCurrentStreak()).thenReturn(null);

        UsersStatisticsResponse result = adminService.getUsersStatistics();

        assertNotNull(result);
        assertEquals(50L, result.getTotalUsers());
        assertEquals(0L, result.getActiveToday());
        assertEquals(0L, result.getAverageStreak());
    }

    @Test
    void getUsersStatistics_ActiveTodayZero() {
        when(userService.countAllUsers()).thenReturn(10L);
        when(mealService.countUsersWithActivityAfter(any())).thenReturn(0L);
        when(userProfileService.getAverageCurrentStreak()).thenReturn(2.1);

        UsersStatisticsResponse result = adminService.getUsersStatistics();

        assertEquals(10L, result.getTotalUsers());
        assertEquals(0L, result.getActiveToday());
        assertEquals(2L, result.getAverageStreak());
    }

    @Test
    void getLastUserActivity_OnlyMealActivity() {
        LocalDateTime mealActivity = LocalDateTime.now().minusHours(2);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(mealActivity);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(null);

        java.lang.reflect.Method method = null;
        try {
            method = AdminServiceImpl.class.getDeclaredMethod("getLastUserActivity", Long.class);
            method.setAccessible(true);

            LocalDateTime result = (LocalDateTime) method.invoke(adminService, 1L);

            assertEquals(mealActivity, result);
        } catch (Exception e) {
            fail("Не удалось вызвать приватный метод: " + e.getMessage());
        }
    }

    @Test
    void getLastUserActivity_OnlyProfileUpdate() {
        LocalDateTime profileUpdate = LocalDateTime.now().minusDays(1);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(null);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(profileUpdate);

        try {
            java.lang.reflect.Method method = AdminServiceImpl.class.getDeclaredMethod("getLastUserActivity", Long.class);
            method.setAccessible(true);

            LocalDateTime result = (LocalDateTime) method.invoke(adminService, 1L);

            assertEquals(profileUpdate, result);
        } catch (Exception e) {
            fail("Не удалось вызвать приватный метод: " + e.getMessage());
        }
    }

    @Test
    void getLastUserActivity_BothActivities_MealIsLater() {
        LocalDateTime mealActivity = LocalDateTime.now().minusHours(2);
        LocalDateTime profileUpdate = LocalDateTime.now().minusDays(1);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(mealActivity);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(profileUpdate);

        try {
            java.lang.reflect.Method method = AdminServiceImpl.class.getDeclaredMethod("getLastUserActivity", Long.class);
            method.setAccessible(true);

            LocalDateTime result = (LocalDateTime) method.invoke(adminService, 1L);

            assertEquals(mealActivity, result);
            assertTrue(mealActivity.isAfter(profileUpdate));
        } catch (Exception e) {
            fail("Не удалось вызвать приватный метод: " + e.getMessage());
        }
    }

    @Test
    void getLastUserActivity_BothActivities_ProfileUpdateIsLater() {
        LocalDateTime mealActivity = LocalDateTime.now().minusDays(2);
        LocalDateTime profileUpdate = LocalDateTime.now().minusHours(1);
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(mealActivity);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(profileUpdate);

        try {
            java.lang.reflect.Method method = AdminServiceImpl.class.getDeclaredMethod("getLastUserActivity", Long.class);
            method.setAccessible(true);

            LocalDateTime result = (LocalDateTime) method.invoke(adminService, 1L);

            assertEquals(profileUpdate, result);
            assertTrue(profileUpdate.isAfter(mealActivity));
        } catch (Exception e) {
            fail("Не удалось вызвать приватный метод: " + e.getMessage());
        }
    }

    @Test
    void getLastUserActivity_NoActivities() {
        when(mealService.getLastMealActivityByUserId(1L)).thenReturn(null);
        when(userProfileService.getLastProfileUpdate(1L)).thenReturn(null);

        try {
            java.lang.reflect.Method method = AdminServiceImpl.class.getDeclaredMethod("getLastUserActivity", Long.class);
            method.setAccessible(true);

            LocalDateTime result = (LocalDateTime) method.invoke(adminService, 1L);

            assertNull(result);
        } catch (Exception e) {
            fail("Не удалось вызвать приватный метод: " + e.getMessage());
        }
    }

    @Test
    void getUsersWithLastActivity_CacheAnnotationPresent() throws NoSuchMethodException {
        java.lang.reflect.Method method = AdminServiceImpl.class.getMethod("getUsersWithLastActivity");
        assertNotNull(method.getAnnotation(org.springframework.cache.annotation.Cacheable.class));
    }

    @Test
    void getUsersStatistics_CacheAnnotationPresent() throws NoSuchMethodException {
        java.lang.reflect.Method method = AdminServiceImpl.class.getMethod("getUsersStatistics");
        assertNotNull(method.getAnnotation(org.springframework.cache.annotation.Cacheable.class));
    }
}