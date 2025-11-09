package ru.daniil.NauJava.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при создании отчета")
public class ReportCreationResponse {

    @Schema(description = "ID созданного отчета", example = "1")
    private Long reportId;

    @Schema(description = "Статус операции", example = "success")
    private String status;

    @Schema(description = "Сообщение", example = "Report creation started")
    private String message;

    public ReportCreationResponse() {}

    public ReportCreationResponse(Long reportId, String status, String message) {
        this.reportId = reportId;
        this.status = status;
        this.message = message;
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}