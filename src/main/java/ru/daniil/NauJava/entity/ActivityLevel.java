package ru.daniil.NauJava.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_activity_level")
public class ActivityLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_name", nullable = false, unique = true)
    private String levelName;

    @Column(name = "description")
    private String description;

    @Column(name = "multiplier", nullable = false)
    private Double multiplier;

    public ActivityLevel() {}

    public ActivityLevel(String levelName, String description, Double multiplier) {
        this.levelName = levelName;
        this.description = description;
        this.multiplier = multiplier;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
