package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.enums.RoleType;
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

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^.{3,}$");
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, UserProfileService userProfileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userProfileService = userProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegistrationRequest request) {
        validateUserData(request.getName(), request.getSurname(), request.getEmail(),
                request.getPassword(), request.getLogin());

        User user = new User(
                request.getEmail(),
                request.getLogin(),
                passwordEncoder.encode(request.getPassword())
        );

        Role userRole = roleRepository.findByName(RoleType.USER.toString())
                .orElseThrow(() -> new NotFoundException("Роль USER не найдена в системе"));
        user.addRole(userRole);

        User newUser = userRepository.save(user);

        userProfileService.registerUserProfile(newUser, request);

        return newUser;
    }

    public boolean updateLogin(String newLogin) {
        getAuthUser().ifPresent(user -> user.setLogin(newLogin));
        return true;
    }

    public boolean updatePassword(String newPassword) {
        getAuthUser().ifPresent(user -> user.setPassword(passwordEncoder.encode(newPassword)));
        return true;
    }

    private void validateUserData(String name, String surname, String email, String password, String login) {
        if (name == null || !USERNAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Имя пользователя должно содержать минимум 3 символа");
        }
        if (surname == null || surname.trim().isEmpty()) {
            throw new ValidationException("Фамилия обязательна для заполнения");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Пароль должен содержать минимум 8 символов, включая заглавные и строчные буквы латинского алфавита, цифры и специальные символы (кроме @/|\\*#&?)");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Пользователь с email " + email + " уже существует");
        }
        if (userRepository.existsByLogin(login)) {
            throw new ValidationException("Пользователь с логином " + login + " уже существует");
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

    public void deleteUserByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
