package ru.daniil.NauJava;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.enums.RoleType;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.RegistrationRequest;
import ru.daniil.NauJava.service.UserDetailsServiceImpl;
import ru.daniil.NauJava.service.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Проверяет работу сервиса "UserDetailsServiceImpl", на корректную отработку:
 * при создании новой роли в БД
 * при назначении роли пользователю
 * при снятии с пользователя конкретной роли
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDetailsServiceTest {
    @Autowired
    private UserDetailsServiceImpl userAuthService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        cleanupTestData();

        userRole = roleRepository.findByName("USER").orElseThrow();
        adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        RegistrationRequest register = new RegistrationRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "TestUser",
                "forUserDetailServiceTest",
                null
        );
        User user = userService.registerUser(register);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void createRole_WhenDuplicateRole_ShouldThrowException() {
        userAuthService.createRole("MANAGER", "Менеджер системы");

        assertThatThrownBy(() ->
                userAuthService.createRole("MANAGER", "Другой менеджер")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль с именем MANAGER уже существует");
    }

    @Test
    void assignAdminRoleToUser_WhenValidData_ShouldAssignRole() {
        userAuthService.assignRoleToUser("testuser", RoleType.ADMIN.toString());

        User updatedUser = userRepository.findByLogin("testuser").orElseThrow();
        assertThat(updatedUser.getRoles().toArray()).contains(userRole, adminRole);
    }

    @Test
    void assignRoleToUser_WhenUserNotFound_ShouldThrowException() {
        assertThatThrownBy(() ->
                userAuthService.assignRoleToUser("nonexistent", "ADMIN")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");
    }

    @Test
    void assignRoleToUser_WhenRoleNotFound_ShouldThrowException() {
        assertThatThrownBy(() ->
                userAuthService.assignRoleToUser("testuser", "INVALID_ROLE")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль не найдена: INVALID_ROLE");
    }

    @Test
    void createRole_WhenNewRole_ShouldCreateRole() {
        Role newRole = userAuthService.createRole("MANAGER", "Менеджер системы");

        assertThat(newRole.getId()).isNotNull();
        assertThat(newRole.getName()).isEqualTo("MANAGER");
        assertThat(newRole.getDescription()).isEqualTo("Менеджер системы");
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
