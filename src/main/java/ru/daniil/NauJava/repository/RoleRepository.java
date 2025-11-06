package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import ru.daniil.NauJava.entity.Role;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
