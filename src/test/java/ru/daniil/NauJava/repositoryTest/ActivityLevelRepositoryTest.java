package ru.daniil.NauJava.repositoryTest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.ActivityLevel;
import ru.daniil.NauJava.repository.ActivityLevelRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ActivityLevelRepositoryTest {

    @Autowired
    private ActivityLevelRepository activityLevelRepository;

    @Test
    void findAllByOrderByIdAsc_ShouldReturnSortedLevels() {
        List<ActivityLevel> levels = activityLevelRepository.findAllByOrderByIdAsc();

        assertThat(levels).isNotEmpty();

        for (int i = 1; i < levels.size(); i++) {
            assertThat(levels.get(i).getId())
                    .as("Level %d should have greater ID than level %d", i, i-1)
                    .isGreaterThan(levels.get(i-1).getId());
        }

        List<String> levelNames = levels.stream()
                .map(ActivityLevel::getLevelName)
                .toList();

        assertThat(levelNames)
                .contains("Сидячий образ жизни", "Легкая активность", "Умеренная активность");
    }

    @Test
    void findByLevelName_WhenLevelExists_ShouldReturnLevel() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName("Сидячий образ жизни");

        assertThat(level).isPresent();
        assertThat(level.get().getMultiplier()).isEqualTo(1.2);
        assertThat(level.get().getDescription()).contains("Сижу дома");
    }

    @Test
    void findByLevelName_WhenLevelNotExists_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName("Несуществующий уровень");

        assertThat(level).isEmpty();
    }

    @Test
    void findByLevelName_WhenNameIsNull_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName(null);

        assertThat(level).isEmpty();
    }

    @Test
    void findByLevelName_WhenNameIsEmpty_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName("");

        assertThat(level).isEmpty();
    }

    @Test
    void findByLevelName_WhenNameIsBlank_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName("   ");

        assertThat(level).isEmpty();
    }

    @Test
    void findByLevelName_IsCaseSensitive() {
        Optional<ActivityLevel> level = activityLevelRepository.findByLevelName("СИДЯЧИЙ ОБРАЗ ЖИЗНИ");

        assertThat(level).isEmpty();
    }

    @Test
    void findById_WhenLevelExists_ShouldReturnLevel() {
        List<ActivityLevel> allLevels = activityLevelRepository.findAllByOrderByIdAsc();
        assertThat(allLevels).isNotEmpty();

        Long firstId = allLevels.get(0).getId();
        Optional<ActivityLevel> level = activityLevelRepository.findById(firstId);

        assertThat(level).isPresent();
        assertThat(level.get().getId()).isEqualTo(firstId);
    }

    @Test
    void findById_WhenLevelNotExists_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findById(999999L);

        assertThat(level).isEmpty();
    }

    @Test
    void findById_WhenIdIsZero_ShouldReturnEmpty() {
        Optional<ActivityLevel> level = activityLevelRepository.findById(0L);

        assertThat(level).isEmpty();
    }

    @Test
    void saveNewActivityLevel_WithValidData_ShouldPersist() {
        ActivityLevel newLevel = new ActivityLevel(
                "Тестовый уровень", "Тестовое описание", 2.0);

        ActivityLevel savedLevel = activityLevelRepository.save(newLevel);

        assertThat(savedLevel.getId()).isNotNull();
        assertThat(savedLevel.getLevelName()).isEqualTo("Тестовый уровень");

        Optional<ActivityLevel> retrievedLevel = activityLevelRepository.findByLevelName("Тестовый уровень");
        assertThat(retrievedLevel).isPresent();
    }

    @Test
    void saveNewActivityLevel_WithDuplicateName_ShouldThrowException() {
        ActivityLevel duplicateLevel = new ActivityLevel(
                "Сидячий образ жизни", "Дубликат", 1.0);

        assertThatThrownBy(() -> activityLevelRepository.save(duplicateLevel))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveNewActivityLevel_WithoutName_ShouldThrowException() {
        ActivityLevel levelWithoutName = new ActivityLevel(
                null, "Без имени", 1.0);

        assertThatThrownBy(() -> activityLevelRepository.save(levelWithoutName))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveNewActivityLevel_WithoutMultiplier_ShouldThrowException() {
        ActivityLevel levelWithoutMultiplier = new ActivityLevel(
                "Без множителя", "Описание", null);

        assertThatThrownBy(() -> activityLevelRepository.save(levelWithoutMultiplier))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveNewActivityLevel_WithVeryLargeMultiplier_ShouldPersist() {
        ActivityLevel levelWithLargeMultiplier = new ActivityLevel(
                "Большой", "Большой множитель", 100.0);

        ActivityLevel savedLevel = activityLevelRepository.save(levelWithLargeMultiplier);

        assertThat(savedLevel.getMultiplier()).isEqualTo(100.0);
    }

    @Test
    void updateActivityLevel_ShouldPersistChanges() {
        Optional<ActivityLevel> existingLevel = activityLevelRepository.findByLevelName("Сидячий образ жизни");
        assertThat(existingLevel).isPresent();

        ActivityLevel levelToUpdate = existingLevel.get();
        String originalName = levelToUpdate.getLevelName();
        levelToUpdate.setDescription("Обновленное описание");
        levelToUpdate.setMultiplier(1.25);

        activityLevelRepository.save(levelToUpdate);

        Optional<ActivityLevel> updatedLevel = activityLevelRepository.findById(levelToUpdate.getId());
        assertThat(updatedLevel).isPresent();
        assertThat(updatedLevel.get().getDescription()).isEqualTo("Обновленное описание");
        assertThat(updatedLevel.get().getMultiplier()).isEqualTo(1.25);
        assertThat(updatedLevel.get().getLevelName()).isEqualTo(originalName);
    }

    @Test
    void deleteActivityLevel_ShouldRemoveLevel() {
        ActivityLevel levelToDelete = new ActivityLevel(
                "Для удаления", "Будет удален", 1.0);
        activityLevelRepository.save(levelToDelete);

        Long idToDelete = levelToDelete.getId();
        activityLevelRepository.delete(levelToDelete);

        Optional<ActivityLevel> deletedLevel = activityLevelRepository.findById(idToDelete);
        assertThat(deletedLevel).isEmpty();
    }

    @Test
    void existsById_WhenLevelExists_ShouldReturnTrue() {
        List<ActivityLevel> allLevels = activityLevelRepository.findAllByOrderByIdAsc();
        assertThat(allLevels).isNotEmpty();

        boolean exists = activityLevelRepository.existsById(allLevels.get(0).getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenLevelNotExists_ShouldReturnFalse() {
        boolean exists = activityLevelRepository.existsById(999999L);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_WhenIdIsZero_ShouldReturnFalse() {
        boolean exists = activityLevelRepository.existsById(0L);
        assertThat(exists).isFalse();
    }

    @Test
    void count_ShouldReturnNumberOfLevels() {
        long count = activityLevelRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(5);
    }

    @Test
    void testStandardLevelsHaveCorrectValues() {
        List<ActivityLevel> levels = activityLevelRepository.findAllByOrderByIdAsc();

        for (ActivityLevel level : levels) {
            switch (level.getLevelName()) {
                case "Сидячий образ жизни":
                    assertThat(level.getMultiplier()).isEqualTo(1.2);
                    break;
                case "Легкая активность":
                    assertThat(level.getMultiplier()).isEqualTo(1.375);
                    break;
                case "Умеренная активность":
                    assertThat(level.getMultiplier()).isEqualTo(1.55);
                    break;
                case "Высокая активность ":
                    assertThat(level.getMultiplier()).isEqualTo(1.725);
                    break;
                case "Ежедневная активность":
                    assertThat(level.getMultiplier()).isEqualTo(1.9);
                    break;
            }
        }
    }
}