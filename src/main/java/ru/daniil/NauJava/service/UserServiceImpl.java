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
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.request.update.UpdateAccountRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!$%^()_+\\-=\\[\\]{};:'\",.<>])[A-Za-z\\d!$%^()_+\\-=\\[\\]{};:'\",.<>]{8,}$"
    );
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;

        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User registerUser(RegistrationRequest request) {
        validateUserData(request.getEmail(), request.getPassword(), request.getLogin());

        User user = new User(
                request.getEmail(),
                request.getLogin(),
                passwordEncoder.encode(request.getPassword())
        );

        Role userRole = roleRepository.findByName(RoleType.USER.toString())
                .orElseThrow(() -> new NotFoundException("Роль USER не найдена в системе"));
        user.addRole(userRole);

        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserAccount(UpdateAccountRequest request) {
        User currentUser = getAuthUser().orElseThrow(() ->
                new RuntimeException("Пользователь не авторизован"));

        if (!currentUser.getLogin().equals(request.getLogin())) {
            userRepository.findByLogin(request.getLogin()).ifPresent(user -> {
                throw new RuntimeException("Пользователь с таким логином уже существует");
            });
        }

        if (!currentUser.getEmail().equals(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                throw new RuntimeException("Пользователь с таким email уже существует");
            });
        }

        currentUser.setLogin(request.getLogin());
        currentUser.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(currentUser);
    }

    private void validateUserData(String email, String password, String login) {
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

    @Override
    public Optional<User> getAuthUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        User userDetails = (User) authentication.getPrincipal();
        return Optional.ofNullable(userDetails);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return userRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
