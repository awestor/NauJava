package ru.daniil.NauJava.repositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.NauJava.entity.MealType;
import ru.daniil.NauJava.repository.MealTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MealTypeRepositoryTest {

    @Autowired
    private MealTypeRepository mealTypeRepository;

    private MealType breakfastType;

    @BeforeEach
    void setUp() {
        mealTypeRepository.deleteAll();

        breakfastType = new MealType("Breakfast", "Утренний прием пищи");
        mealTypeRepository.save(breakfastType);

        MealType lunchType = new MealType("Lunch", "Обед");
        mealTypeRepository.save(lunchType);
    }

    @Test
    void findByName_WhenExists_ShouldReturnMealType() {
        Optional<MealType> foundType = mealTypeRepository.findByName("Breakfast");

        assertThat(foundType).isPresent();
        assertThat(foundType.get().getName()).isEqualTo("Breakfast");
        assertThat(foundType.get().getDescription()).isEqualTo("Утренний прием пищи");
    }

    @Test
    void findByName_WhenNotExists_ShouldReturnEmpty() {
        Optional<MealType> foundType = mealTypeRepository.findByName("Dinner");

        assertThat(foundType).isEmpty();
    }

    @Test
    void findByName_WhenNameIsNull_ShouldReturnEmpty() {
        Optional<MealType> foundType = mealTypeRepository.findByName(null);

        assertThat(foundType).isEmpty();
    }

    @Test
    void findByName_WhenNameIsEmpty_ShouldReturnEmpty() {
        Optional<MealType> foundType = mealTypeRepository.findByName("");

        assertThat(foundType).isEmpty();
    }

    @Test
    void findByName_ShouldBeCaseSensitive() {
        Optional<MealType> foundType = mealTypeRepository.findByName("BREAKFAST");

        assertThat(foundType).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllMealTypes() {
        Iterable<MealType> allTypes = mealTypeRepository.findAll();

        assertThat(allTypes).hasSize(2);
        assertThat(allTypes).extracting(MealType::getName)
                .containsExactlyInAnyOrder("Breakfast", "Lunch");
    }

    @Test
    void saveMealType_WithDuplicateName_ShouldThrowException() {
        MealType duplicateType = new MealType("Breakfast", "Дубликат завтрака");

        assertThatThrownBy(() -> mealTypeRepository.save(duplicateType))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveMealType_WithNullName_ShouldThrowException() {
        MealType nullNameType = new MealType(null, "Описание");

        assertThatThrownBy(() -> mealTypeRepository.save(nullNameType))
                .isInstanceOf(Exception.class);
    }

    @Test
    void saveMealType_WithEmptyName_ShouldThrowException() {
        MealType emptyNameType = new MealType("", "Описание");

        assertThatThrownBy(() -> mealTypeRepository.save(emptyNameType))
                .isInstanceOf(Exception.class);
    }

    @Test
    void updateMealType_ShouldPersistChanges() {
        breakfastType.setDescription("Updated breakfast description");
        mealTypeRepository.save(breakfastType);

        MealType updatedType = mealTypeRepository.findById(breakfastType.getId()).orElseThrow();
        assertThat(updatedType.getDescription()).isEqualTo("Updated breakfast description");
    }

    @Test
    void deleteMealType_ShouldRemoveMealType() {
        Long typeId = breakfastType.getId();
        mealTypeRepository.delete(breakfastType);

        boolean exists = mealTypeRepository.existsById(typeId);
        assertThat(exists).isFalse();
    }

    @Test
    void createMealType_WithValidData_ShouldSucceed() {
        MealType dinnerType = new MealType("Dinner", "Ужин");

        assertThatCode(() -> mealTypeRepository.save(dinnerType))
                .doesNotThrowAnyException();

        List<MealType> allTypes = (List<MealType>) mealTypeRepository.findAll();
        assertThat(allTypes).hasSize(3);
    }

    @Test
    void findById_WhenExists_ShouldReturnMealType() {
        Optional<MealType> foundType = mealTypeRepository.findById(breakfastType.getId());

        assertThat(foundType).isPresent();
        assertThat(foundType.get().getId()).isEqualTo(breakfastType.getId());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        Optional<MealType> foundType = mealTypeRepository.findById(999L);

        assertThat(foundType).isEmpty();
    }
}