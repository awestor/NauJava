package ru.daniil.NauJava;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.RegistrationRequest;
import ru.daniil.NauJava.service.UserDetailsServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Autowired
    private UserDetailsServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        cleanupTestData();
        userRole = roleRepository.findByName("USER").orElseThrow();
        adminRole = roleRepository.findByName("ADMIN").orElseThrow();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void createUserWithRole_WhenValidData_ShouldCreateUser() {
        User user = userService.createUserWithRole(
                "test@example.com",
                "testuser",
                "Password123!",
                "Test",
                "User",
                "USER"
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getLogin()).isEqualTo("testuser");
        assertThat(user.getName()).isEqualTo("Test");
        assertThat(user.getSurname()).isEqualTo("User");
        assertThat(passwordEncoder.matches("Password123!", user.getPassword())).isTrue();
        assertThat(user.getRoles().toArray()).contains(userRole);
    }

    @Test
    void createUserWithRole_WhenDuplicateEmail_ShouldThrowException() {
        userService.createUserWithRole("test@example.com", "testuser1", "Password123!", "Test", "User", "USER");

        assertThatThrownBy(() ->
                userService.createUserWithRole("test@example.com", "testuser2", "Password123!", "Test", "User", "USER")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с email test@example.com уже существует");
    }

    @Test
    void createUserWithRole_WhenDuplicateLogin_ShouldThrowException() {
        userService.createUserWithRole("test1@example.com", "testuser", "Password123!", "Test", "User", "USER");

        assertThatThrownBy(() ->
                userService.createUserWithRole("test2@example.com", "testuser", "Password123!", "Test", "User", "USER")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с логином testuser уже существует");
    }

    @Test
    void createUserWithRole_WhenInvalidRole_ShouldThrowException() {
        assertThatThrownBy(() ->
                userService.createUserWithRole("test@example.com", "testuser", "Password123!", "Test", "User", "INVALID_ROLE")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль INVALID_ROLE не найдена в системе");
    }

    @Test
    void createRole_WhenNewRole_ShouldCreateRole() {
        Role newRole = userService.createRole("MANAGER", "Менеджер системы");

        assertThat(newRole.getId()).isNotNull();
        assertThat(newRole.getName()).isEqualTo("MANAGER");
        assertThat(newRole.getDescription()).isEqualTo("Менеджер системы");
    }

    @Test
    void createRole_WhenDuplicateRole_ShouldThrowException() {
        userService.createRole("MANAGER", "Менеджер системы");

        assertThatThrownBy(() ->
                userService.createRole("MANAGER", "Другой менеджер")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль MANAGER уже существует");
    }

    @Test
    void assignRoleToUser_WhenValidData_ShouldAssignRole() {
        User user = userService.createUserWithRole("test@example.com", "testuser", "Password123!", "Test", "User", "USER");

        userService.assignRoleToUser("testuser", "ADMIN");

        User updatedUser = userRepository.findByLogin("testuser").orElseThrow();
        assertThat(updatedUser.getRoles().toArray()).contains(userRole, adminRole);
    }

    @Test
    void assignRoleToUser_WhenUserNotFound_ShouldThrowException() {
        assertThatThrownBy(() ->
                userService.assignRoleToUser("nonexistent", "ADMIN")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");
    }

    @Test
    void assignRoleToUser_WhenRoleNotFound_ShouldThrowException() {
        userService.createUserWithRole("test@example.com", "testuser", "Password123!", "Test", "User", "USER");

        assertThatThrownBy(() ->
                userService.assignRoleToUser("testuser", "INVALID_ROLE")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль не найдена: INVALID_ROLE");
    }

    @Test
    void registerUser_WhenValidData_ShouldCreateUserWithUserRole() {
        User user = userService.registerUser(new RegistrationRequest(
                "newuser@example.com", "Password123!", "newuser", "New", "User", null
        ));

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("newuser@example.com");
        assertThat(user.getLogin()).isEqualTo("newuser");
        assertThat(user.getRoles().toArray()).contains(userRole);
        assertThat(user.getRoles().toArray()).contains(userRole);
        assertThat(user.getRoles().toArray()).doesNotContain(adminRole);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUser() {
        userService.createUserWithRole("test@example.com", "testuser", "Password123!", "Test", "User", "USER");

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getAuthorities()).isNotNull();
    }

    @Test
    void loadUserByUsername_WhenUserNotExists_ShouldThrowException() {
        assertThatThrownBy(() ->
                userService.loadUserByUsername("nonexistent")
        ).isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");
    }

    private void cleanupTestData() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = new Role("USER", "Обычный пользователь");
        Role adminRole = new Role("ADMIN", "Администратор системы");
        Role moderatorRole = new Role("MODERATOR", "Модератор контента");

        roleRepository.save(userRole);
        roleRepository.save(adminRole);
        roleRepository.save(moderatorRole);
    }
}