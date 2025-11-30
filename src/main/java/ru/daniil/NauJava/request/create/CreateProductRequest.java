package ru.daniil.NauJava.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request используемый для создания продукта
 */
@Schema(description = "Модель данных для создания нового продукта")
public class CreateProductRequest {

    @NotBlank(message = "Название продукта обязательно")
    @Size(min = 2, max = 100, message = "Название продукта должно содержать от 2 до 100 символов")
    @Schema(description = "Название продукта", example = "Яблоко", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Калорийность обязательна")
    @Min(value = 0, message = "Калорийность не может быть отрицательной")
    @Max(value = 1000, message = "Калорийность не может превышать 1000 ккал")
    @Schema(description = "Калорийность на 100 грамм", example = "52.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double caloriesPer100g;

    @NotNull(message = "Содержание белков обязательно")
    @DecimalMin(value = "0.0", message = "Содержание белков не может быть отрицательным")
    @DecimalMax(value = "100.0", message = "Содержание белков не может превышать 100г")
    @Schema(description = "Белки на 100 грамм", example = "0.3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double proteinsPer100g;

    @NotNull(message = "Содержание жиров обязательно")
    @DecimalMin(value = "0.0", message = "Содержание жиров не может быть отрицательным")
    @DecimalMax(value = "100.0", message = "Содержание жиров не может превышать 100г")
    @Schema(description = "Жиры на 100 грамм", example = "0.2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double fatsPer100g;

    @NotNull(message = "Содержание углеводов обязательно")
    @DecimalMin(value = "0.0", message = "Содержание углеводов не может быть отрицательным")
    @DecimalMax(value = "100.0", message = "Содержание углеводов не может превышать 100г")
    @Schema(description = "Углеводы на 100 грамм", example = "14.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double carbsPer100g;

    public CreateProductRequest() {}

    /**
     * Конструктор используемый для инициализации объекта с заполнением всех полей.
     * @param name название продукта
     * @param caloriesPer100g калории в 100 грамм продукта
     * @param proteinsPer100g белки в 100 грамм продукта
     * @param fatsPer100g жиры в 100 грамм продукта
     * @param carbsPer100g углеводы в 100 грамм продукта
     */
    public CreateProductRequest(String name, Double caloriesPer100g, Double proteinsPer100g,
                                Double fatsPer100g, Double carbsPer100g) {
        this.name = name;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinsPer100g = proteinsPer100g;
        this.fatsPer100g = fatsPer100g;
        this.carbsPer100g = carbsPer100g;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(Double caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }

    public Double getProteinsPer100g() { return proteinsPer100g; }
    public void setProteinsPer100g(Double proteinsPer100g) { this.proteinsPer100g = proteinsPer100g; }

    public Double getFatsPer100g() { return fatsPer100g; }
    public void setFatsPer100g(Double fatsPer100g) { this.fatsPer100g = fatsPer100g; }

    public Double getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(Double carbsPer100g) { this.carbsPer100g = carbsPer100g; }
}