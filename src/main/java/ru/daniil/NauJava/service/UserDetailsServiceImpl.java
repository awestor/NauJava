package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.exception.ValidationException;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.RegistrationRequest;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.contains("@")) {
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        } else {
            return userRepository.findByLogin(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        }
    }

    public void assignRoleToUser(String login, String roleName) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ValidationException("Пользователь не найден: " + login));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Роль не найдена: " + roleName));

        user.addRole(role);
        userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void deleteUserByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new ValidationException("Роль с именем " + name + " уже существует");
        }

        Role role = new Role(name, description);
        return roleRepository.save(role);
    }

    public Optional<Role> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    public void deleteRoleByName(String name) {
        roleRepository.findByName(name).ifPresent(roleRepository::delete);
    }
}
