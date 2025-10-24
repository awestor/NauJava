package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.UserProfile;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "userProfiles")
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByUserIdAndDailyCalorieGoalIsNotNull(Long userId);
}
