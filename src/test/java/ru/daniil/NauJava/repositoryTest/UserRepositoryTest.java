package ru.daniil.NauJava.repositoryTest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail("test1@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("test2@example.com").ifPresent(userRepository::delete);

        testUser = new User("test1@example.com", "testUser1", "password123");
        User testUser2 = new User("test2@example.com", "testUser2", "password456");

        userRepository.save(testUser);
        userRepository.save(testUser2);
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        Optional<User> foundUser = userRepository.findByEmail("test1@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByEmail_WhenEmailIsNull_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByEmail(null);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByEmail_WhenEmailIsEmptyString_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByEmail("");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByEmail_WithCaseSensitive_ShouldBeCaseSensitive() {
        Optional<User> foundUser = userRepository.findByEmail("TEST1@EXAMPLE.COM");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByLogin_WhenUserExists_ShouldReturnUser() {
        Optional<User> foundUser = userRepository.findByLogin("testUser1");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getLogin()).isEqualTo("testUser1");
    }

    @Test
    void findByLogin_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByLogin("nonexistent");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByLogin_WhenLoginIsNull_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByLogin(null);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByLogin_WhenLoginIsEmptyString_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByLogin("");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByLogin_WithCaseSensitive_ShouldBeCaseSensitive() {
        Optional<User> foundUser = userRepository.findByLogin("TestUser1");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByLogin_WhenLoginExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByLogin("testUser1");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLogin_WhenLoginNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByLogin("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByLogin_WhenLoginIsNull_ShouldReturnFalse() {
        boolean exists = userRepository.existsByLogin(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByLogin_WhenLoginIsEmptyString_ShouldReturnFalse() {
        boolean exists = userRepository.existsByLogin("");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("test1@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailIsNull_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailIsEmptyString_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("");
        assertThat(exists).isFalse();
    }

    @Test
    void findAllByOrderByIdAsc_ShouldReturnAllUsersSortedById() {
        List<User> users = userRepository.findAllByOrderByIdAsc();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 1; i < users.size(); i++) {
            assertThat(users.get(i).getId()).isGreaterThan(users.get(i - 1).getId());
        }

        assertThat(users).extracting(User::getEmail)
                .contains("test1@example.com", "test2@example.com");
    }

    @Test
    void findAllByOrderByIdAsc_WhenNoUsers_ShouldReturnEmptyList() {
        userRepository.deleteAll();

        List<User> users = userRepository.findAllByOrderByIdAsc();
        assertThat(users).isEmpty();
    }

    @Test
    void countByCreatedAtBetween_WhenUsersInRange_ShouldCountCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);

        Long count = userRepository.countByCreatedAtBetween(start, end);

        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void countByCreatedAtBetween_WhenNoUsersInRange_ShouldReturnZero() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(20);

        Long count = userRepository.countByCreatedAtBetween(start, end);
        assertThat(count).isZero();
    }

    @Test
    void countByCreatedAtBetween_WhenStartDateAfterEndDate_ShouldReturnZero() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        Long count = userRepository.countByCreatedAtBetween(start, end);
        assertThat(count).isZero();
    }

    @Test
    void saveUser_WithDuplicateEmail_ShouldThrowException() {
        User duplicateEmailUser = new User("test1@example.com", "differentLogin", "password");

        assertThatThrownBy(() -> userRepository.save(duplicateEmailUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveUser_WithDuplicateLogin_ShouldThrowException() {
        User duplicateLoginUser = new User("different@example.com", "testUser1", "password");

        assertThatThrownBy(() -> userRepository.save(duplicateLoginUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveUser_WithNullEmail_ShouldThrowException() {
        User nullEmailUser = new User(null, "login", "password");

        assertThatThrownBy(() -> userRepository.save(nullEmailUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveUser_WithNullLogin_ShouldThrowException() {
        User nullLoginUser = new User("email@example.com", null, "password");

        assertThatThrownBy(() -> userRepository.save(nullLoginUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveUser_WithNullPassword_ShouldThrowException() {
        User nullPasswordUser = new User("email@example.com", "login", null);

        assertThatThrownBy(() -> userRepository.save(nullPasswordUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateUser_ShouldPersistChanges() {
        testUser.setEmail("updated@example.com");
        testUser.setLogin("updatedLogin");
        userRepository.save(testUser);

        Optional<User> updatedUser = userRepository.findById(testUser.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.get().getLogin()).isEqualTo("updatedLogin");
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        Long userId = testUser.getId();
        userRepository.delete(testUser);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        Optional<User> foundUser = userRepository.findById(testUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findById(999999L);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findById_WhenIdIsZero_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findById(0L);
        assertThat(foundUser).isEmpty();
    }
}