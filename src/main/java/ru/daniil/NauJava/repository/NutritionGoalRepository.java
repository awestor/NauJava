package ru.daniil.NauJava.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.NutritionGoal;
import ru.daniil.NauJava.entity.UserProfile;

import java.util.Optional;

@Repository
public interface NutritionGoalRepository extends CrudRepository<NutritionGoal, Long> {
    Optional<NutritionGoal> findByUserProfile(UserProfile userProfile);
    Optional<NutritionGoal> findByUserProfileId(Long userProfileId);

    @Modifying
    @Query("DELETE FROM NutritionGoal ng WHERE ng.userProfile = :userProfile")
    void deleteByUserProfile(@Param("userProfile") UserProfile userProfile);
}
