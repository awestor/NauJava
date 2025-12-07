package ru.daniil.NauJava.response;

import ru.daniil.NauJava.enums.ReportStatus;

public class ReportResponse {
    private Long id;
    private String status;
    private String periodStart;
    private String periodEnd;
    private String createdAt;
    private String completedAt;
    private Long totalExecutionTime;

    public ReportResponse() {
    }

    public ReportResponse(Long id, String status, String content,
                          String periodStart, String periodEnd,
                          String createdAt, String completedAt,
                          Long totalExecutionTime) {
        this.id = id;
        this.status = status;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.totalExecutionTime = totalExecutionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public Long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(Long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }
}
