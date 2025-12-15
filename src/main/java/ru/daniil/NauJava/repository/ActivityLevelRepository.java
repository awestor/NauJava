package ru.daniil.NauJava.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.daniil.NauJava.entity.ActivityLevel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLevelRepository  extends CrudRepository<ActivityLevel, Long> {
    List<ActivityLevel> findAllByOrderByIdAsc();

    Optional<ActivityLevel> findByLevelName(String levelName);
}
