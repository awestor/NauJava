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
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!$%^()_+\\-=\\[\\]{};:'\",.<>])[A-Za-z\\d!$%^()_+\\-=\\[\\]{};:'\",.<>]{8,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^.{6,}$");

    public UserDetailsServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public User registerUser(RegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ValidationException("Пользователь с логином " + request.getLogin() + " уже существует");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPatronymic(request.getPatronymic());

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ValidationException("Роль USER не найдена в системе"));
        user.addRole(userRole);

        return userRepository.save(user);
    }

    @Transactional
    public User createUserWithRole(String email, String login, String password, String name,
                                   String surname, String roleName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Пользователь с email " + email + " уже существует");
        }

        if (userRepository.findByLogin(login).isPresent()) {
            throw new ValidationException("Пользователь с логином " + login + " уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setSurname(surname);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Роль " + roleName + " не найдена в системе"));
        user.addRole(role);

        return userRepository.save(user);
    }

    @Transactional
    public void assignRoleToUser(String login, String roleName) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ValidationException("Пользователь не найден: " + login));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Роль не найдена: " + roleName));

        user.addRole(role);
        userRepository.save(user);
    }

    public User createAdminUser(String email, String login, String password, String name, String surname) {
        return createUserWithRole(email, login, password, name, surname, "ADMIN");
    }

    @Transactional()
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional()
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional()
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

    private void validateRegistrationRequest(RegistrationRequest request) {
        validateUserData(request.getEmail(), request.getPassword(), request.getName());

        if (request.getSurname() == null || request.getSurname().trim().isEmpty()) {
            throw new ValidationException("Фамилия обязательна для заполнения");
        }
    }

    private void validateUserData(String email, String password, String name) {
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Пароль должен содержать минимум 8 символов, включая заглавные и строчные буквы латинского алфавита, цифры и специальные символы (кроме @/|\\*#&?)");
        }

        if (name == null || !USERNAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Имя пользователя должно содержать минимум 6 символов");
        }
    }
}
