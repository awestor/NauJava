package ru.daniil.NauJava.response;

public class ReportCreationResponse {
    private Long reportId;
    private String status;
    private String message;
    private String periodStart;
    private String periodEnd;

    public ReportCreationResponse() {
    }

    public ReportCreationResponse(Long reportId, String status, String message,
                                  String periodStart, String periodEnd) {
        this.reportId = reportId;
        this.status = status;
        this.message = message;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }
}