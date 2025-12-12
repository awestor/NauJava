package ru.daniil.NauJava.repositoryTest;


import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.repository.RoleRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_WhenRoleExists_ShouldReturnRole() {
        Optional<Role> userRole = roleRepository.findByName("USER");
        Optional<Role> adminRole = roleRepository.findByName("ADMIN");

        assertThat(userRole).isPresent();
        assertThat(userRole.get().getDescription()).contains("Обычный пользователь");

        assertThat(adminRole).isPresent();
        assertThat(adminRole.get().getDescription()).contains("Администратор");
    }

    @Test
    void findByName_WhenRoleNotExists_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findByName("NONEXISTENT");

        assertThat(role).isEmpty();
    }

    @Test
    void findByName_WhenNameIsNull_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findByName(null);

        assertThat(role).isEmpty();
    }

    @Test
    void findByName_WhenNameIsEmpty_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findByName("");

        assertThat(role).isEmpty();
    }

    @Test
    void findByName_WhenNameIsBlank_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findByName("   ");

        assertThat(role).isEmpty();
    }

    @Test
    void findByName_IsCaseSensitive() {
        Optional<Role> role = roleRepository.findByName("user");

        assertThat(role).isEmpty();
    }


    @Test
    void existsByName_WhenRoleExists_ShouldReturnTrue() {
        boolean userExists = roleRepository.existsByName("USER");
        boolean adminExists = roleRepository.existsByName("ADMIN");

        assertThat(userExists).isTrue();
        assertThat(adminExists).isTrue();
    }

    @Test
    void existsByName_WhenRoleNotExists_ShouldReturnFalse() {
        boolean exists = roleRepository.existsByName("NONEXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_WhenNameIsNull_ShouldReturnFalse() {
        boolean exists = roleRepository.existsByName(null);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_WhenNameIsEmpty_ShouldReturnFalse() {
        boolean exists = roleRepository.existsByName("");

        assertThat(exists).isFalse();
    }

    @Test
    void saveNewRole_WithDuplicateName_ShouldThrowException() {
        Role duplicateRole = new Role("USER", "Дубликат пользователя");

        assertThatThrownBy(() -> roleRepository.save(duplicateRole))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveNewRole_WithoutName_ShouldThrowException() {
        Role roleWithoutName = new Role(null, "Без имени");

        assertThatThrownBy(() -> roleRepository.save(roleWithoutName))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveNewRole_WithEmptyName_ShouldReturnRoleWithEmptyName() {
        Role roleWithEmptyName = new Role("", "Пустое имя");

        Role savedRole = roleRepository.save(roleWithEmptyName);

        assertThat(savedRole.getName()).isEqualTo("");
    }

    @Test
    void saveNewRole_WithLongName_ShouldPersist() {
        String longName = "VERY_LONG_ROLE_NAME_THAT_EXCEEDS_NORMAL_LENGTH";
        Role roleWithLongName = new Role(longName, "Роль с длинным именем");

        Role savedRole = roleRepository.save(roleWithLongName);

        assertThat(savedRole.getName()).isEqualTo(longName);
    }

    @Test
    void saveNewRole_WithSpecialCharacters_ShouldPersist() {
        Role roleWithSpecialChars = new Role("SPECIAL-ROLE_123", "Роль со спецсимволами");

        Role savedRole = roleRepository.save(roleWithSpecialChars);

        assertThat(savedRole.getName()).isEqualTo("SPECIAL-ROLE_123");
    }

    @Test
    void updateRole_ShouldPersistChanges() {
        Optional<Role> existingRole = roleRepository.findByName("USER");
        assertThat(existingRole).isPresent();

        Role roleToUpdate = existingRole.get();
        String originalName = roleToUpdate.getName();
        roleToUpdate.setDescription("Обновленное описание пользователя");

        roleRepository.save(roleToUpdate);

        Optional<Role> updatedRole = roleRepository.findById(roleToUpdate.getId());
        assertThat(updatedRole).isPresent();
        assertThat(updatedRole.get().getDescription()).isEqualTo("Обновленное описание пользователя");

        assertThat(updatedRole.get().getName()).isEqualTo(originalName);
    }

    @Test
    void deleteRole_ShouldRemoveRole() {
        Role roleToDelete = new Role("TO_DELETE", "Роль для удаления");
        roleRepository.save(roleToDelete);

        Long idToDelete = roleToDelete.getId();
        roleRepository.delete(roleToDelete);

        Optional<Role> deletedRole = roleRepository.findById(idToDelete);
        assertThat(deletedRole).isEmpty();
    }

    @Test
    void findById_WhenRoleExists_ShouldReturnRole() {
        Optional<Role> userRole = roleRepository.findByName("USER");
        assertThat(userRole).isPresent();

        Optional<Role> roleById = roleRepository.findById(userRole.get().getId());
        assertThat(roleById).isPresent();
        assertThat(roleById.get().getName()).isEqualTo("USER");
    }

    @Test
    void findById_WhenRoleNotExists_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findById(999999L);

        assertThat(role).isEmpty();
    }

    @Test
    void findById_WhenIdIsZero_ShouldReturnEmpty() {
        Optional<Role> role = roleRepository.findById(0L);

        assertThat(role).isEmpty();
    }

    @Test
    void existsById_WhenRoleExists_ShouldReturnTrue() {
        Optional<Role> userRole = roleRepository.findByName("USER");
        assertThat(userRole).isPresent();

        boolean exists = roleRepository.existsById(userRole.get().getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenRoleNotExists_ShouldReturnFalse() {
        boolean exists = roleRepository.existsById(999999L);

        assertThat(exists).isFalse();
    }

    @Test
    void existsById_WhenIdIsZero_ShouldReturnFalse() {
        boolean exists = roleRepository.existsById(0L);

        assertThat(exists).isFalse();
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        Iterable<Role> allRoles = roleRepository.findAll();

        long count = 0;
        boolean hasUser = false;
        boolean hasAdmin = false;

        for (Role role : allRoles) {
            count++;
            if ("USER".equals(role.getName())) hasUser = true;
            if ("ADMIN".equals(role.getName())) hasAdmin = true;
        }

        assertThat(count).isGreaterThanOrEqualTo(2);
        assertThat(hasUser).isTrue();
        assertThat(hasAdmin).isTrue();
    }

    @Test
    void count_ShouldReturnNumberOfRoles() {
        long count = roleRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}
