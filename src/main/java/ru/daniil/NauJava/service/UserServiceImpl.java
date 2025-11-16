package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Transactional
@Service
public class UserServiceImpl implements UserService {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!$%^()_+\\-=\\[\\]{};:'\",.<>])[A-Za-z\\d!$%^()_+\\-=\\[\\]{};:'\",.<>]{8,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^.{6,}$");
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    public User createAdminUser(String email, String login, String password, String name, String surname) {
        return createUserWithRole(email, login, password, name, surname, "ADMIN");
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

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getAuthUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        User userDetails = (User) authentication.getPrincipal();
        return Optional.ofNullable(userDetails);
    }
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
