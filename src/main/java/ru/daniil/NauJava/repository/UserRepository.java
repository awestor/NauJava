package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Находит всех пользователей с указанным именем
     * @param name имя пользователя
     */
    @Query("FROM User WHERE name = :name")
    List<User> findByNameIgnoreCase(String name);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

}
