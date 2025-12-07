package ru.daniil.NauJava.entity;

import jakarta.persistence.*;
import ru.daniil.NauJava.enums.ReportStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.CREATED;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "report_period_start", nullable = false)
    private LocalDate reportPeriodStart;

    @Column(name = "report_period_end", nullable = false)
    private LocalDate reportPeriodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_execution_time")
    private Long totalExecutionTime;

    @Column(name = "total_users_registered")
    private Long totalUsersRegistered;

    @Column(name = "total_products_created")
    private Long totalProductsCreated;

    @Column(name = "average_meals_per_active_user")
    private Double averageMealsPerActiveUser;

    @Column(name = "total_daily_reports_created")
    private Long totalDailyReportsCreated;

    @Column(name = "active_users_count")
    private Long activeUsersCount;

    /**
     * Конструктор по умолчанию для формирования сущности отчёта.
     * Указывает время создания сущности.
     */
    public Report() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Конструктор для формирования сущности отчёта с указанным статусом
     * и временем создания.
     */
    public Report(LocalDate reportPeriodStart, LocalDate reportPeriodEnd) {
        this();
        this.reportPeriodStart = reportPeriodStart;
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDate getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDate reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDate getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDate reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(Long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public Long getTotalUsersRegistered() {
        return totalUsersRegistered;
    }

    public void setTotalUsersRegistered(Long totalUsersRegistered) {
        this.totalUsersRegistered = totalUsersRegistered;
    }

    public Long getTotalProductsCreated() {
        return totalProductsCreated;
    }

    public void setTotalProductsCreated(Long totalProductsCreated) {
        this.totalProductsCreated = totalProductsCreated;
    }

    public Double getAverageMealsPerActiveUser() {
        return averageMealsPerActiveUser;
    }

    public void setAverageMealsPerActiveUser(Double averageMealsPerActiveUser) {
        this.averageMealsPerActiveUser = averageMealsPerActiveUser;
    }

    public Long getTotalDailyReportsCreated() {
        return totalDailyReportsCreated;
    }

    public void setTotalDailyReportsCreated(Long totalDailyReportsCreated) {
        this.totalDailyReportsCreated = totalDailyReportsCreated;
    }

    public Long getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(Long activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }
}