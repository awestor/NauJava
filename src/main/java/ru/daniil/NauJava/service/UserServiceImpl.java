package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.UserRepository;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Transactional
    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
