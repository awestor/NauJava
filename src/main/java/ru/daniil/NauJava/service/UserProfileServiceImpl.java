package ru.daniil.NauJava.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.request.RegistrationRequest;

@Transactional
@Service
public class UserProfileServiceImpl  implements UserProfileService{
    @Override
    public void registerUserProfile(User newUser, RegistrationRequest request) {
        UserProfile profile = new UserProfile(
                request.getName(),
                request.getSurname(),
                request.getPatronymic()
        );
        profile.setUser(newUser);
    }
}
