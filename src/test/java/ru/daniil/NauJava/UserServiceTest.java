package ru.daniil.NauJava;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserProfileRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserDetailsServiceImpl;
import ru.daniil.NauJava.service.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Проверяет работу сервиса "UserServiceImpl", на корректную отработку:
 * при создании пользователя по регистрационной форме
 * при получении созданных пользователей
 * при проверке на существование пользователей в БД
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Autowired
    private UserDetailsServiceImpl userAuthService;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;
    private Role adminRole;

    @BeforeAll
    void voidRoleSetUp(){
        roleRepository.deleteAll();

        Role userRole = new Role("USER", "Обычный пользователь");
        Role adminRole = new Role("ADMIN", "Администратор системы");

        roleRepository.save(userRole);
        roleRepository.save(adminRole);
        this.userRole = roleRepository.findByName("USER").orElseThrow();
        this.adminRole = roleRepository.findByName("ADMIN").orElseThrow();
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        RegistrationRequest register = new RegistrationRequest(
                "test@example.com",
                "Password123!",
                "testUser",
                "TestUser",
                "forUserServiceTest",
                null
        );
        userService.registerUser(register);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @AfterAll
    void RoleDown() {
        roleRepository.deleteAll();
    }

    @Test
    void createUser_WhenValidData_ShouldCreateUser() {
        RegistrationRequest register = new RegistrationRequest(
                "test1@example.com",
                "Password123!",
                "testUser1",
                "TestUser",
                "forUserServiceTest",
                null
        );
        User user = userService.registerUser(register);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test1@example.com");
        assertThat(user.getLogin()).isEqualTo("testUser1");
        assertThat(passwordEncoder.matches("Password123!", user.getPassword())).isTrue();
        assertThat(user.getRoles().toArray()).contains(userRole);
    }

    @Test
    void createUser_WhenDuplicateEmail_ShouldThrowException() {
        RegistrationRequest register2 = new RegistrationRequest(
                "test@example.com",
                "Password123!",
                "testUser1",
                "TestUser",
                "forUserServiceTest",
                null
        );

        assertThatThrownBy(() ->
                userService.registerUser(register2)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с email test@example.com уже существует");
    }

    @Test
    void createUser_WhenDuplicateLogin_ShouldThrowException() {
        RegistrationRequest register2 = new RegistrationRequest(
                "test1@example.com",
                "Password123!",
                "testUser",
                "TestUser",
                "forUserServiceTest",
                null
        );

        assertThatThrownBy(() ->
                userService.registerUser(register2)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с логином testUser уже существует");
    }

    @Test
    void loadUserByUsername_WhenUserNotExists_ShouldThrowException() {
        assertThatThrownBy(() ->
                userAuthService.loadUserByUsername("nonexistent")
        ).isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");
    }
}