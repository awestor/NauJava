package ru.daniil.NauJava.mockTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.daniil.NauJava.entity.MealType;
import ru.daniil.NauJava.repository.MealTypeRepository;
import ru.daniil.NauJava.service.mealType.MealTypeServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealTypeServiceTest {

    @Mock
    private MealTypeRepository mealTypeRepository;

    @InjectMocks
    private MealTypeServiceImpl mealTypeService;

    private MealType breakfast;
    private MealType lunch;
    private MealType dinner;

    @BeforeEach
    void setUp() {
        breakfast = new MealType("Завтрак", "Описание");
        breakfast.setId(1L);

        lunch = new MealType("Обед", "Описание");
        lunch.setId(2L);

        dinner = new MealType("Ужин", "Описание");
        dinner.setId(3L);
    }

    @Test
    void getMealTypes_WhenTypesExist_ShouldReturnAllMealTypes() {
        List<MealType> mealTypes = Arrays.asList(breakfast, lunch, dinner);
        when(mealTypeRepository.findAll()).thenReturn(mealTypes);

        List<MealType> result = mealTypeService.getMealTypes();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(breakfast, lunch, dinner);
        verify(mealTypeRepository, times(1)).findAll();
    }

    @Test
    void getMealTypes_WhenNoTypes_ShouldReturnEmptyList() {
        when(mealTypeRepository.findAll()).thenReturn(List.of());

        List<MealType> result = mealTypeService.getMealTypes();

        assertThat(result).isEmpty();
        verify(mealTypeRepository, times(1)).findAll();
    }

    @Test
    void getMealTypes_ShouldReturnTypesWithCorrectOrder() {
        MealType snack = new MealType("Перекус", "Описание");
        snack.setId(4L);

        List<MealType> mealTypes = Arrays.asList(breakfast, lunch, dinner, snack);
        when(mealTypeRepository.findAll()).thenReturn(mealTypes);

        List<MealType> result = mealTypeService.getMealTypes();

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getName()).isEqualTo("Завтрак");
        assertThat(result.get(1).getName()).isEqualTo("Обед");
        assertThat(result.get(2).getName()).isEqualTo("Ужин");
        assertThat(result.get(3).getName()).isEqualTo("Перекус");
        verify(mealTypeRepository, times(1)).findAll();
    }

    @Test
    void getMealTypes_ShouldReturnTypesWithIds() {
        List<MealType> mealTypes = Arrays.asList(breakfast, lunch);
        when(mealTypeRepository.findAll()).thenReturn(mealTypes);

        List<MealType> result = mealTypeService.getMealTypes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(mealTypeRepository, times(1)).findAll();
    }
}
