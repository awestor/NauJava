package ru.daniil.NauJava.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.daniil.NauJava.entity.Role;

import java.util.Optional;

public interface UserDetailsServiceCustom extends UserDetailsService {

    void assignRoleToUser(String login, String roleName);

    void removeRoleToUser(Long id, String roleName);

    Role createRole(String name, String description);

    Optional<Role> findRoleByName(String name);

    void deleteRoleByName(String name);
}
