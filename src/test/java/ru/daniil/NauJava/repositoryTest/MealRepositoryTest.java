package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.*;
import ru.daniil.NauJava.repository.DailyReportRepository;
import ru.daniil.NauJava.repository.MealRepository;
import ru.daniil.NauJava.repository.MealTypeRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MealRepositoryTest {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private MealTypeRepository mealTypeRepository;

    private User testUser;
    private DailyReport dailyReport;
    private Meal testMeal;

    @BeforeEach
    void setUp() {
        mealRepository.deleteAll();
        dailyReportRepository.deleteAll();
        userRepository.deleteAll();
        mealTypeRepository.deleteAll();
        
        testUser = new User("test@example.com", "testUser", "password123");
        userRepository.save(testUser);
        
        MealType breakfastType = new MealType("Breakfast", "Утренний прием пищи");
        mealTypeRepository.save(breakfastType);
        
        dailyReport = new DailyReport(testUser, LocalDate.now());
        dailyReportRepository.save(dailyReport);
        
        testMeal = new Meal(dailyReport, breakfastType);
        mealRepository.save(testMeal);
    }

    @Test
    void findByDailyReportId_WhenMealsExist_ShouldReturnList() {
        List<Meal> meals = mealRepository.findByDailyReportId(dailyReport.getId());

        assertThat(meals).isNotEmpty();
        assertThat(meals).extracting(Meal::getDailyReport)
                .extracting(DailyReport::getId)
                .containsOnly(dailyReport.getId());
    }

    @Test
    void findByDailyReportId_WhenNoMeals_ShouldReturnEmpty() {
        List<Meal> meals = mealRepository.findByDailyReportId(999L);

        assertThat(meals).isEmpty();
    }

    @Test
    void findByDailyReportUserIdAndDailyReportReportDate_WhenExists_ShouldReturnList() {
        List<Meal> meals = mealRepository.findByDailyReportUserIdAndDailyReportReportDate(
                testUser.getId(),
                LocalDate.now()
        );

        assertThat(meals).isNotEmpty();
    }

    @Test
    void findByDailyReportUserIdAndDailyReportReportDate_WhenWrongDate_ShouldReturnEmpty() {
        List<Meal> meals = mealRepository.findByDailyReportUserIdAndDailyReportReportDate(
                testUser.getId(),
                LocalDate.now().minusDays(1)
        );

        assertThat(meals).isEmpty();
    }

    @Test
    void findLastMealActivityByUserId_WhenHasMeals_ShouldReturnDateTime() {
        LocalDateTime lastActivity = mealRepository.findLastMealActivityByUserId(testUser.getId());

        assertThat(lastActivity).isNotNull();
        assertThat(lastActivity).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void findLastMealActivityByUserId_WhenNoMeals_ShouldReturnNull() {
        LocalDateTime lastActivity = mealRepository.findLastMealActivityByUserId(999L);

        assertThat(lastActivity).isNull();
    }

    @Test
    void countUsersWithActivityAfter_ShouldCountCorrectly() {
        Long count = mealRepository.countUsersWithActivityAfter(LocalDateTime.now().minusDays(1));

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countUsersWithActivityAfter_WhenNoActivity_ShouldReturnZero() {
        Long count = mealRepository.countUsersWithActivityAfter(LocalDateTime.now().plusDays(1));

        assertThat(count).isZero();
    }

    @Test
    void findDistinctUserIdsWithMealsBetween_ShouldReturnUserIds() {
        List<Long> userIds = mealRepository.findDistinctUserIdsWithMealsBetween(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThat(userIds).contains(testUser.getId());
    }

    @Test
    void findDistinctUserIdsWithMealsBetween_WhenNoMeals_ShouldReturnEmpty() {
        List<Long> userIds = mealRepository.findDistinctUserIdsWithMealsBetween(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(20)
        );

        assertThat(userIds).isEmpty();
    }

    @Test
    void countMealsForUsersBetweenDates_ShouldCountCorrectly() {
        List<Long> userIds = List.of(testUser.getId());
        Long count = mealRepository.countMealsForUsersBetweenDates(
                userIds,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThat(count).isEqualTo(1L);
    }
    
    @Test
    void updateMeal_ShouldPersistChanges() {
        MealType lunchType = new MealType("Lunch", "Обед");
        mealTypeRepository.save(lunchType);

        testMeal.setMealType(lunchType);
        testMeal.setEatenAt(LocalDateTime.now().minusHours(2));
        mealRepository.save(testMeal);

        Meal updatedMeal = mealRepository.findById(testMeal.getId()).orElseThrow();
        assertThat(updatedMeal.getMealType()).isEqualTo("Lunch");
        assertThat(updatedMeal.getEatenAt()).isBefore(LocalDateTime.now().minusHours(1));
    }

    @Test
    void deleteMeal_ShouldRemoveMeal() {
        Long mealId = testMeal.getId();
        mealRepository.delete(testMeal);

        boolean exists = mealRepository.existsById(mealId);
        assertThat(exists).isFalse();
    }

    @Test
    void createMultipleMealsForSameReport_ShouldSucceed() {
        MealType lunchType = new MealType("Lunch", "Обед");
        mealTypeRepository.save(lunchType);

        Meal secondMeal = new Meal(dailyReport, lunchType);
        mealRepository.save(secondMeal);

        List<Meal> meals = mealRepository.findByDailyReportId(dailyReport.getId());
        assertThat(meals).hasSize(2);
    }
}