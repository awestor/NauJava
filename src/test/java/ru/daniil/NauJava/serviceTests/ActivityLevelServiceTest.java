package ru.daniil.NauJava.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.repository.ActivityLevelRepository;
import ru.daniil.NauJava.service.activityLevel.ActivityLevelServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLevelServiceTest {

    @Mock
    private ActivityLevelRepository activityLevelRepository;

    @InjectMocks
    private ActivityLevelServiceImpl activityLevelService;

    private ActivityLevel sedentary;
    private ActivityLevel moderate;
    private ActivityLevel active;

    @BeforeEach
    void setUp() {
        sedentary = new ActivityLevel("Sedentary", "Мало или нет физической активности", 1.2);
        sedentary.setId(1L);

        moderate = new ActivityLevel("Moderate", "Умеренная активность", 1.55);
        moderate.setId(2L);

        active = new ActivityLevel("Active", "Высокая активность", 1.725);
        active.setId(3L);
    }

    @Test
    void getById_WhenExists_ShouldReturnActivityLevel() {
        when(activityLevelRepository.findById(1L)).thenReturn(Optional.of(sedentary));

        Optional<ActivityLevel> result = activityLevelService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getLevelName()).isEqualTo("Sedentary");
        assertThat(result.get().getMultiplier()).isEqualTo(1.2);

        verify(activityLevelRepository, times(1)).findById(1L);
    }

    @Test
    void getById_WhenNotExists_ShouldReturnEmpty() {
        when(activityLevelRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ActivityLevel> result = activityLevelService.getById(999L);

        assertThat(result).isEmpty();
        verify(activityLevelRepository, times(1)).findById(999L);
    }

    @Test
    void getAllActivityLevels_ShouldReturnAllLevels() {
        List<ActivityLevel> levels = Arrays.asList(sedentary, moderate, active);
        when(activityLevelRepository.findAll()).thenReturn(levels);

        List<ActivityLevel> result = activityLevelService.getAllActivityLevels();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(sedentary, moderate, active);

        verify(activityLevelRepository, times(1)).findAll();
    }

    @Test
    void getAllActivityLevels_WhenEmpty_ShouldReturnEmptyList() {
        when(activityLevelRepository.findAll()).thenReturn(List.of());

        List<ActivityLevel> result = activityLevelService.getAllActivityLevels();

        assertThat(result).isEmpty();
        verify(activityLevelRepository, times(1)).findAll();
    }
}
