package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "userProfiles")
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);

    @Query("SELECT MAX(up.updatedAt) FROM UserProfile up WHERE up.user.id = :userId")
    LocalDateTime findLastUpdateByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(up.currentStreak) FROM UserProfile up")
    Double findAverageCurrentStreak();
}
