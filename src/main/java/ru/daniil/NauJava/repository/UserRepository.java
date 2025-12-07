package ru.daniil.NauJava.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "users")
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Находит всех пользователей с указанным именем
     * @param login логин пользователя
     */
    @Query("FROM User WHERE login = :login")
    List<User> findByLoginIgnoreCase(String login);

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByIdAsc();

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
