package ru.daniil.NauJava;


import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void createRole(){
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = new Role("USER", "Обычный пользователь");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role("ADMIN", "Администратор системы");
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("MODERATOR").isEmpty()) {
            Role moderatorRole = new Role("MODERATOR", "Модератор контента");
            roleRepository.save(moderatorRole);
        }

        List<Role> result = (ArrayList<Role>)roleRepository.findAll();
        assertThat(result).extracting(Role::getName)
                .containsExactlyInAnyOrder("USER", "ADMIN", "MODERATOR");
    }
}
