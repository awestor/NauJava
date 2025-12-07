package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.enums.RoleType;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.request.update.UpdateAccountRequest;
import ru.daniil.NauJava.service.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;
    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        userRole = new Role(RoleType.USER.toString(), "Обычный пользователь");
        userRole.setId(1L);

        testUser = new User("test@example.com", "testUser", "Password123!");
        testUser.setId(1L);
        testUser.addRole(userRole);

        registrationRequest = new RegistrationRequest(
                "newuser@example.com",
                "Password123!",
                "new_user"
        );

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerUser_WhenValidData_ShouldCreateUser() {
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByLogin("new_user")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("Password123!");
        when(roleRepository.findByName(RoleType.USER.toString())).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        User result = userService.registerUser(registrationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getLogin()).isEqualTo("new_user");
        assertThat(result.getPassword()).isEqualTo("Password123!");
        assertThat(result.getRoles()).contains(userRole);

        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, times(1)).existsByLogin("new_user");
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(roleRepository, times(1)).findByName(RoleType.USER.toString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WhenDuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.registerUser(registrationRequest)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с email newuser@example.com уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_WhenDuplicateLogin_ShouldThrowException() {
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByLogin("new_user")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.registerUser(registrationRequest)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь с логином new_user уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_WhenInvalidEmail_ShouldThrowException() {
        RegistrationRequest invalidRequest = new RegistrationRequest(
                "invalid-email",
                "Password123!",
                "new_user"
        );

        assertThatThrownBy(() ->
                userService.registerUser(invalidRequest)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректный формат email");
    }

    @Test
    void registerUser_WhenInvalidPassword_ShouldThrowException() {
        RegistrationRequest invalidRequest = new RegistrationRequest(
                "newuser@example.com",
                "weak",
                "new_user"
        );

        assertThatThrownBy(() ->
                userService.registerUser(invalidRequest)
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пароль должен содержать"); //....
    }

    @Test
    void updateUserAccount_WhenValidData_ShouldUpdateUser() {
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByLogin("updatedUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateAccountRequest updateRequest = new UpdateAccountRequest(
                "updatedUser",
                "updated@example.com",
                "NewPassword123!");

        userService.updateUserAccount(updateRequest);

        assertThat(testUser.getLogin()).isEqualTo("updatedUser");
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");

        verify(userRepository, times(1)).findByLogin("updatedUser");
        verify(userRepository, times(1)).findByEmail("updated@example.com");
        verify(passwordEncoder, times(1)).encode("NewPassword123!");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserAccount_WhenDuplicateLogin_ShouldThrowException() {
        User anotherUser = new User("another@example.com", "anotherUser", "password");
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByLogin("anotherUser")).thenReturn(Optional.of(anotherUser));

        UpdateAccountRequest updateRequest = new UpdateAccountRequest(
                "anotherUser",
                "test@example.com");

        assertThatThrownBy(() ->
                userService.updateUserAccount(updateRequest)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь с таким логином уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAccount_WhenNoPasswordChange_ShouldNotEncodePassword() {
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByLogin("updatedUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateAccountRequest updateRequest = new UpdateAccountRequest(
                "updatedUser",
                "updated@example.com");
        userService.updateUserAccount(updateRequest);

        assertThat(testUser.getLogin()).isEqualTo("updatedUser");
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getPassword()).isEqualTo("Password123!");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getAuthUser_WhenAuthenticated_ShouldReturnUser() {
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<User> result = userService.getAuthUser();

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void getAuthUser_WhenNotAuthenticated_ShouldReturnEmpty() {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<User> result = userService.getAuthUser();

        assertThat(result).isEmpty();
    }

    @Test
    void findByLogin_WhenExists_ShouldReturnUser() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        User result = userService.findByLogin("testUser").orElse(null);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByLogin("testUser");
    }

    @Test
    void findByLogin_WhenNotExists_ShouldThrowException() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThat(userService.findByLogin("nonexistent")).isEqualTo(Optional.empty());
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        User user2 = new User("user2@example.com", "user2", "password2");
        user2.setId(2L);

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAllByOrderByIdAsc()).thenReturn(users);

        List<User> result = userService.findAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testUser, user2);
        verify(userRepository, times(1)).findAllByOrderByIdAsc();
    }

    @Test
    void countAllUsers_ShouldReturnCount() {
        when(userRepository.count()).thenReturn(5L);

        long result = userService.countAllUsers();

        assertThat(result).isEqualTo(5L);
        verify(userRepository, times(1)).count();
    }

    @Test
    void userExists_WhenEmailExists_ShouldReturnTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        boolean result = userService.userExists("test@example.com");

        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void userExists_WhenEmailNotExists_ShouldReturnFalse() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = userService.userExists("nonexistent@example.com");

        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }
}
