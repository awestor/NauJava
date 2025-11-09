package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "execution_time_users")
    private Long executionTimeUsers;

    @Column(name = "execution_time_products")
    private Long executionTimeProducts;

    public Report() {
        this.createdAt = LocalDateTime.now();
    }

    public Report(ReportStatus status) {
        this();
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setExecutionTimeUsers(Long executionTimeUsers) { this.executionTimeUsers = executionTimeUsers; }
    public void setExecutionTimeProducts(Long executionTimeProducts) { this.executionTimeProducts = executionTimeProducts; }

}