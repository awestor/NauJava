package ru.daniil.NauJava.config.PostConstructInit;

import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.repository.RoleRepository;

@Component
public class RolePostConstructInitializer {

    private final RoleRepository roleRepository;

    public RolePostConstructInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Order(1)
    public void initializeRoles() {
        createRoleIfNotExists("USER", "Обычный пользователь системы");
        createRoleIfNotExists("ADMIN", "Администратор системы");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role(name, description);
            roleRepository.save(role);
            System.out.println("Создана роль: " + name);
        }
    }
}