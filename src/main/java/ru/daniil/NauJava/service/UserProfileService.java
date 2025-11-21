package ru.daniil.NauJava.service;

import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.request.RegistrationRequest;

public interface UserProfileService {
    void registerUserProfile(User newUser, RegistrationRequest request);
}
