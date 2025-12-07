package ru.daniil.NauJava.response;

import ru.daniil.NauJava.enums.ReportStatus;

public class ReportDataResponse {
    private String status;
    private String periodStart;
    private String periodEnd;
    private Long totalExecutionTime;

    public ReportDataResponse(String status, String periodStart, String periodEnd, Long totalExecutionTime) {
        this.status = status;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalExecutionTime = totalExecutionTime;
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

    public Long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(Long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }
}
