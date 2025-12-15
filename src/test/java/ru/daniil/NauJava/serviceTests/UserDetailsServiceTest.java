package ru.daniil.NauJava.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.service.UserDetailsServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role("USER", "Обычный пользователь");
        userRole.setId(1L);

        adminRole = new Role("ADMIN", "Администратор системы");
        adminRole.setId(2L);

        testUser = new User("test@example.com", "testUser", "Password123!");
        testUser.setId(1L);
        testUser.addRole(userRole);
    }

    @Test
    void loadUserByUsername_WhenLoginExists_ShouldReturnUser() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("testUser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getPassword()).isEqualTo("Password123!");

        verify(userRepository, times(1)).findByLogin("testUser");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_WhenEmailExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testUser");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("nonexistent")
        ).isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");
    }

    @Test
    void assignRoleToUser_WhenValidData_ShouldAssignRole() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        userDetailsService.assignRoleToUser("testUser", "ADMIN");

        assertThat(testUser.getRoles()).hasSize(2);
        assertThat(testUser.getRoles()).contains(userRole, adminRole);

        verify(userRepository, times(1)).findByLogin("testUser");
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void assignRoleToUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.assignRoleToUser("nonexistent", "ADMIN")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не найден: nonexistent");

        verify(userRepository, never()).save(any());
    }

    @Test
    void assignRoleToUser_WhenRoleNotFound_ShouldThrowException() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.assignRoleToUser("testUser", "NONEXISTENT")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль не найдена: NONEXISTENT");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createRole_WhenNewRole_ShouldCreateRole() {
        when(roleRepository.existsByName("NEW_ROLE")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(3L);
            return role;
        });

        Role result = userDetailsService.createRole("NEW_ROLE", "Новая роль");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NEW_ROLE");
        assertThat(result.getDescription()).isEqualTo("Новая роль");

        verify(roleRepository, times(1)).existsByName("NEW_ROLE");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createRole_WhenDuplicateRole_ShouldThrowException() {
        when(roleRepository.existsByName("USER")).thenReturn(true);

        assertThatThrownBy(() ->
                userDetailsService.createRole("USER", "Обычный пользователь")
        ).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Роль с именем USER уже существует");

        verify(roleRepository, never()).save(any());
    }

    @Test
    void findRoleByName_WhenExists_ShouldReturnRole() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        Optional<Role> result = userDetailsService.findRoleByName("USER");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("USER");
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    void deleteRoleByName_WhenExists_ShouldDeleteRole() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        userDetailsService.deleteRoleByName("USER");

        verify(roleRepository, times(1)).findByName("USER");
        verify(roleRepository, times(1)).delete(userRole);
    }

    @Test
    void deleteRoleByName_WhenNotExists_ShouldDoNothing() {
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        userDetailsService.deleteRoleByName("NONEXISTENT");

        verify(roleRepository, times(1)).findByName("NONEXISTENT");
        verify(roleRepository, never()).delete(any());
    }
}
