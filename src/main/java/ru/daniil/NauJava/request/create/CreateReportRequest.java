package ru.daniil.NauJava.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class CreateReportRequest {
    @NotNull(message = "Дата начала обязательна")
    @PastOrPresent(message = "Дата начала не может быть будущей")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания обязательна")
    @PastOrPresent(message = "Дата окончания не может быть будущей")
    private LocalDate endDate;

    public CreateReportRequest() {}

    public CreateReportRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
