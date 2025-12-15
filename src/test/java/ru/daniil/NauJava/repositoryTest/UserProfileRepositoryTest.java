package ru.daniil.NauJava.repositoryTest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.repository.UserProfileRepository;
import ru.daniil.NauJava.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserProfileRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;
    private User testUser2;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail("profiletest1@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("profiletest2@example.com").ifPresent(userRepository::delete);

        testUser = new User("profiletest1@example.com", "profileuser1", "password123");
        testUser2 = new User("profiletest2@example.com", "profileuser2", "password456");

        userRepository.save(testUser);
        userRepository.save(testUser2);

        testProfile = new UserProfile("Иван", "Иванов", "Иванович");
        testProfile.setUser(testUser);
        testProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testProfile.setGender("M");
        testProfile.setHeight(180);
        testProfile.setWeight(75.5);
        testProfile.setTargetWeight(70.0);
        testProfile.setCurrentStreak(5);

        userProfileRepository.save(testProfile);
    }

    @Test
    void findByUser_WhenProfileExists_ShouldReturnProfile() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUser(testUser);

        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getName()).isEqualTo("Иван");
        assertThat(foundProfile.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUser_WhenProfileNotExists_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUser(testUser2);

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void findByUser_WhenUserIsNull_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUser(null);

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void findByUserId_WhenProfileExists_ShouldReturnProfile() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(testUser.getId());

        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUserId_WhenProfileNotExists_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(testUser2.getId());

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void findByUserId_WhenUserIdIsNull_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(null);

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void findByUserId_WhenUserIdIsInvalid_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(-1L);

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void saveProfile_WithValidData_ShouldPersistCorrectly() {
        UserProfile newProfile = new UserProfile("Петр", "Петров", "Петрович");
        newProfile.setUser(testUser2);
        newProfile.setDateOfBirth(LocalDate.of(1995, 5, 15));
        newProfile.setGender("M");
        newProfile.setHeight(175);
        newProfile.setWeight(70.0);

        UserProfile savedProfile = userProfileRepository.save(newProfile);

        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getName()).isEqualTo("Петр");

        Optional<UserProfile> retrievedProfile = userProfileRepository.findByUserId(testUser2.getId());
        assertThat(retrievedProfile).isPresent();
    }

    @Test
    void saveProfile_WithoutUser_ShouldThrowException() {
        UserProfile profileWithoutUser = new UserProfile("Без", "Пользователя", "");

        assertThatThrownBy(() -> userProfileRepository.save(profileWithoutUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveSecondProfileForSameUser_ShouldNotBeAllowed() {
        UserProfile secondProfile = new UserProfile("Второй", "Профиль", "Ошибка");
        secondProfile.setUser(testUser);

        assertThatThrownBy(() -> userProfileRepository.save(secondProfile))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateProfile_ShouldPersistChanges() {
        testProfile.setName("ОбновленноеИмя");
        testProfile.setWeight(80.0);
        testProfile.setCurrentStreak(10);

        userProfileRepository.save(testProfile);

        Optional<UserProfile> updatedProfile = userProfileRepository.findById(testProfile.getId());
        assertThat(updatedProfile).isPresent();
        assertThat(updatedProfile.get().getName()).isEqualTo("ОбновленноеИмя");
        assertThat(updatedProfile.get().getWeight()).isEqualTo(80.0);
        assertThat(updatedProfile.get().getCurrentStreak()).isEqualTo(10);
    }

    @Test
    void deleteProfile_ShouldRemoveProfile() {
        Long profileId = testProfile.getId();
        userProfileRepository.delete(testProfile);

        Optional<UserProfile> deletedProfile = userProfileRepository.findById(profileId);
        assertThat(deletedProfile).isEmpty();
    }

    @Test
    void findLastUpdateByUserId_WhenProfileExists_ShouldReturnDateTime() {
        LocalDateTime lastUpdate = userProfileRepository.findLastUpdateByUserId(testUser.getId());

        assertThat(lastUpdate).isNotNull();
        assertThat(lastUpdate).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void findLastUpdateByUserId_WhenProfileNotExists_ShouldReturnNull() {
        LocalDateTime lastUpdate = userProfileRepository.findLastUpdateByUserId(testUser2.getId());

        assertThat(lastUpdate).isNull();
    }

    @Test
    void findLastUpdateByUserId_WhenUserIdIsNull_ShouldReturnNull() {
        LocalDateTime lastUpdate = userProfileRepository.findLastUpdateByUserId(null);

        assertThat(lastUpdate).isNull();
    }

    @Test
    void findAverageCurrentStreak_WithMultipleProfiles_ShouldCalculateAverage() {
        UserProfile secondProfile = new UserProfile("Мария", "Иванова", "Петровна");
        secondProfile.setUser(testUser2);
        secondProfile.setCurrentStreak(15);
        userProfileRepository.save(secondProfile);

        Double average = userProfileRepository.findAverageCurrentStreak();

        assertThat(average).isNotNull();
        assertThat(average).isEqualTo(10.0);
    }

    @Test
    void findAverageCurrentStreak_WithNoProfiles_ShouldReturnNull() {
        userProfileRepository.deleteAll();

        Double average = userProfileRepository.findAverageCurrentStreak();

        assertThat(average).isNull();
    }

    @Test
    void findAverageCurrentStreak_WithZeroStreaks_ShouldReturnZero() {
        testProfile.setCurrentStreak(0);
        userProfileRepository.save(testProfile);

        Double average = userProfileRepository.findAverageCurrentStreak();

        assertThat(average).isEqualTo(0.0);
    }

    @Test
    void findById_WhenProfileExists_ShouldReturnProfile() {
        Optional<UserProfile> foundProfile = userProfileRepository.findById(testProfile.getId());

        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getId()).isEqualTo(testProfile.getId());
    }

    @Test
    void findById_WhenProfileNotExists_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findById(999999L);

        assertThat(foundProfile).isEmpty();
    }

    @Test
    void findById_WhenIdIsZero_ShouldReturnEmpty() {
        Optional<UserProfile> foundProfile = userProfileRepository.findById(0L);

        assertThat(foundProfile).isEmpty();
    }
}