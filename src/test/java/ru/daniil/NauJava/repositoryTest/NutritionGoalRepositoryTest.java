package ru.daniil.NauJava.repositoryTest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.NutritionGoal;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.repository.NutritionGoalRepository;
import ru.daniil.NauJava.repository.UserProfileRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class NutritionGoalRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NutritionGoalRepository nutritionGoalRepository;

    private UserProfile testProfile;
    private NutritionGoal testGoal;
    private UserProfile testProfile2;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail("goaltest1@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("goaltest2@example.com").ifPresent(userRepository::delete);

        User testUser = new User("goaltest1@example.com", "goalUser1", "password123");
        User testUser2 = new User("goaltest2@example.com", "goalUser2", "password456");

        userRepository.save(testUser);
        userRepository.save(testUser2);

        testProfile = new UserProfile("Иван", "Иванов", "Тест");
        testProfile.setUser(testUser);
        userProfileRepository.save(testProfile);

        testProfile2 = new UserProfile("Петр", "Петров", "Тест");
        testProfile2.setUser(testUser2);
        userProfileRepository.save(testProfile2);

        testGoal = new NutritionGoal(testProfile, 2000);
        testGoal.setDailyProteinGoal(150.0);
        testGoal.setDailyFatGoal(65.0);
        testGoal.setDailyCarbsGoal(250.0);

        nutritionGoalRepository.save(testGoal);
    }

    @Test
    void findByUserProfile_WhenGoalExists_ShouldReturnGoal() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfile(testProfile);

        assertThat(foundGoal).isPresent();
        assertThat(foundGoal.get().getDailyCalorieGoal()).isEqualTo(2000);
        assertThat(foundGoal.get().getUserProfile().getId()).isEqualTo(testProfile.getId());
    }

    @Test
    void findByUserProfile_WhenGoalNotExists_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfile(testProfile2);

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void findByUserProfile_WhenProfileIsNull_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfile(null);

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void findByUserProfileId_WhenGoalExists_ShouldReturnGoal() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfileId(testProfile.getId());

        assertThat(foundGoal).isPresent();
        assertThat(foundGoal.get().getDailyCalorieGoal()).isEqualTo(2000);
    }

    @Test
    void findByUserProfileId_WhenGoalNotExists_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfileId(testProfile2.getId());

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void findByUserProfileId_WhenIdIsNull_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfileId(null);

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void findByUserProfileId_WhenIdIsInvalid_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findByUserProfileId(-1L);

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void saveGoal_WithValidData_ShouldPersistCorrectly() {
        NutritionGoal newGoal = new NutritionGoal(testProfile2, 1800);
        newGoal.setDailyProteinGoal(120.0);
        newGoal.setDailyFatGoal(50.0);
        newGoal.setDailyCarbsGoal(200.0);

        NutritionGoal savedGoal = nutritionGoalRepository.save(newGoal);

        assertThat(savedGoal.getId()).isNotNull();
        assertThat(savedGoal.getDailyCalorieGoal()).isEqualTo(1800);

        Optional<NutritionGoal> retrievedGoal = nutritionGoalRepository.findByUserProfileId(testProfile2.getId());
        assertThat(retrievedGoal).isPresent();
    }

    @Test
    void saveGoal_WithoutProfile_ShouldThrowException() {
        NutritionGoal goalWithoutProfile = new NutritionGoal();
        goalWithoutProfile.setDailyCalorieGoal(2000);

        assertThatThrownBy(() -> nutritionGoalRepository.save(goalWithoutProfile))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveSecondGoalForSameProfile_ShouldNotBeAllowed() {
        NutritionGoal secondGoal = new NutritionGoal(testProfile, 2200);

        assertThatThrownBy(() -> nutritionGoalRepository.save(secondGoal))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveGoal_WithZeroCalories_ShouldBeAllowed() {
        NutritionGoal goal = new NutritionGoal(testProfile2, 0);

        NutritionGoal savedGoal = nutritionGoalRepository.save(goal);
        assertThat(savedGoal.getDailyCalorieGoal()).isZero();
    }

    @Test
    void saveGoal_WithNullNutrients_ShouldUseDefaults() {
        NutritionGoal goal = new NutritionGoal(testProfile2, 2000);

        NutritionGoal savedGoal = nutritionGoalRepository.save(goal);

        assertThat(savedGoal.getDailyProteinGoal()).isNull();
        assertThat(savedGoal.getDailyFatGoal()).isNull();
        assertThat(savedGoal.getDailyCarbsGoal()).isNull();
    }

    @Test
    void updateGoal_ShouldPersistChanges() {
        testGoal.setDailyCalorieGoal(2200);
        testGoal.setDailyProteinGoal(160.0);

        nutritionGoalRepository.save(testGoal);

        Optional<NutritionGoal> updatedGoal = nutritionGoalRepository.findById(testGoal.getId());
        assertThat(updatedGoal).isPresent();
        assertThat(updatedGoal.get().getDailyCalorieGoal()).isEqualTo(2200);
        assertThat(updatedGoal.get().getDailyProteinGoal()).isEqualTo(160.0);
    }

    @Test
    void deleteByUserProfile_ShouldRemoveGoal() {
        nutritionGoalRepository.deleteByUserProfile(testProfile);

        Optional<NutritionGoal> deletedGoal = nutritionGoalRepository.findByUserProfile(testProfile);
        assertThat(deletedGoal).isEmpty();
    }

    @Test
    void deleteByUserProfile_WhenProfileHasNoGoal_ShouldDoNothing() {
        nutritionGoalRepository.deleteByUserProfile(testProfile2);

        Optional<NutritionGoal> goal = nutritionGoalRepository.findByUserProfile(testProfile2);
        assertThat(goal).isEmpty();
    }

    @Test
    void findById_WhenGoalExists_ShouldReturnGoal() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findById(testGoal.getId());

        assertThat(foundGoal).isPresent();
        assertThat(foundGoal.get().getId()).isEqualTo(testGoal.getId());
    }

    @Test
    void findById_WhenGoalNotExists_ShouldReturnEmpty() {
        Optional<NutritionGoal> foundGoal = nutritionGoalRepository.findById(999999L);

        assertThat(foundGoal).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllGoals() {
        NutritionGoal secondGoal = new NutritionGoal(testProfile2, 1800);
        nutritionGoalRepository.save(secondGoal);

        Iterable<NutritionGoal> allGoals = nutritionGoalRepository.findAll();

        long count = 0;
        for (NutritionGoal goal : allGoals) {
            count++;
        }

        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void saveGoal_WithExtremeValues_ShouldHandleCorrectly() {
        NutritionGoal extremeGoal = new NutritionGoal(testProfile2, 10000);
        extremeGoal.setDailyProteinGoal(1000.0);
        extremeGoal.setDailyFatGoal(500.0);
        extremeGoal.setDailyCarbsGoal(2000.0);

        NutritionGoal savedGoal = nutritionGoalRepository.save(extremeGoal);

        assertThat(savedGoal.getDailyCalorieGoal()).isEqualTo(10000);
        assertThat(savedGoal.getDailyProteinGoal()).isEqualTo(1000.0);
    }

    @Test
    void saveGoal_WithDecimalValues_ShouldPersistCorrectly() {
        NutritionGoal decimalGoal = new NutritionGoal(testProfile2, 1850);
        decimalGoal.setDailyProteinGoal(125.75);
        decimalGoal.setDailyFatGoal(42.25);
        decimalGoal.setDailyCarbsGoal(187.50);

        NutritionGoal savedGoal = nutritionGoalRepository.save(decimalGoal);

        assertThat(savedGoal.getDailyProteinGoal()).isEqualTo(125.75);
        assertThat(savedGoal.getDailyFatGoal()).isEqualTo(42.25);
        assertThat(savedGoal.getDailyCarbsGoal()).isEqualTo(187.50);
    }
}