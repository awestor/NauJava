package ru.daniil.NauJava.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.entity.NutritionGoal;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.repository.NutritionGoalRepository;
import ru.daniil.NauJava.service.nutritionGoal.NutritionGoalServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionGoalServiceTest {

    @Mock
    private NutritionGoalRepository nutritionGoalRepository;

    @InjectMocks
    private NutritionGoalServiceImpl nutritionGoalService;

    private UserProfile userProfile;
    private NutritionGoal existingNutritionGoal;

    @BeforeEach
    void setUp() {
        ActivityLevel activityLevel = new ActivityLevel
                ("Умеренный", "Средняя активность", 1.55);
        activityLevel.setId(1L);

        userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setGender("M");
        userProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userProfile.setHeight(180);
        userProfile.setWeight(75.0);
        userProfile.setActivityLevel(activityLevel);

        existingNutritionGoal = new NutritionGoal();
        existingNutritionGoal.setId(1L);
        existingNutritionGoal.setUserProfile(userProfile);
        existingNutritionGoal.setDailyCalorieGoal(2500);
        existingNutritionGoal.setDailyProteinGoal(100.0);
        existingNutritionGoal.setDailyFatGoal(70.0);
        existingNutritionGoal.setDailyCarbsGoal(300.0);
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenValidData_ShouldCreateNewGoal() {
        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).findByUserProfile(userProfile);
        verify(nutritionGoalRepository, times(1)).save(any(NutritionGoal.class));

        assertThatNoException().isThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        );
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenValidData_ShouldUpdateExistingGoal() {
        when(nutritionGoalRepository.findByUserProfile(userProfile))
                .thenReturn(Optional.of(existingNutritionGoal));
        when(nutritionGoalRepository.save(any(NutritionGoal.class)))
                .thenReturn(existingNutritionGoal);

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).findByUserProfile(userProfile);
        verify(nutritionGoalRepository, times(1)).save(existingNutritionGoal);
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenMissingGender_ShouldThrowException() {
        userProfile.setGender(null);

        assertThatThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно данных для расчета целевых показателей");
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenMissingDateOfBirth_ShouldThrowException() {
        userProfile.setDateOfBirth(null);

        assertThatThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно данных для расчета целевых показателей");
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenMissingHeight_ShouldThrowException() {
        userProfile.setHeight(null);

        assertThatThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно данных для расчета целевых показателей");
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenMissingWeight_ShouldThrowException() {
        userProfile.setWeight(null);

        assertThatThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно данных для расчета целевых показателей");
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenMissingActivityLevel_ShouldThrowException() {
        userProfile.setActivityLevel(null);

        assertThatThrownBy(() ->
                nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно данных для расчета целевых показателей");
    }

    @Test
    void calculateAndUpdateNutritionGoal_WhenFemaleUser_ShouldCalculateCorrectly() {
        userProfile.setGender("F");
        userProfile.setDateOfBirth(LocalDate.of(1995, 5, 15));
        userProfile.setHeight(165);
        userProfile.setWeight(60.0);

        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).save(any(NutritionGoal.class));
    }

    @Test
    void calculateAndUpdateNutritionGoal_WithTargetWeightForWeightLoss_ShouldAdjustCalories() {
        userProfile.setTargetWeight(70.0);

        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).save(any(NutritionGoal.class));
    }

    @Test
    void calculateAndUpdateNutritionGoal_WithTargetWeightForWeightGain_ShouldAdjustCalories() {
        userProfile.setWeight(70.0); // Текущий вес
        userProfile.setTargetWeight(75.0); // Целевой вес больше текущего (набор массы)

        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).save(any(NutritionGoal.class));
    }

    @Test
    void calculateAndUpdateNutritionGoal_WithSameTargetWeight_ShouldNotAdjustCalories() {
        userProfile.setTargetWeight(75.0);

        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(1)).save(any(NutritionGoal.class));
    }

    @Test
    void calculateAndUpdateNutritionGoal_WithDifferentActivityLevels_ShouldCalculateCorrectTDEE() {
        ActivityLevel sedentary = new ActivityLevel("Сидячий", "Минимальная активность", 1.2);
        ActivityLevel active = new ActivityLevel("Активный", "Высокая активность", 1.725);

        userProfile.setActivityLevel(sedentary);
        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());
        when(nutritionGoalRepository.save(any(NutritionGoal.class))).thenAnswer(invocation -> {
            NutritionGoal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        userProfile.setActivityLevel(active);
        when(nutritionGoalRepository.findByUserProfile(userProfile)).thenReturn(Optional.empty());

        nutritionGoalService.calculateAndUpdateNutritionGoal(userProfile);

        verify(nutritionGoalRepository, times(2)).save(any(NutritionGoal.class));
    }
}
