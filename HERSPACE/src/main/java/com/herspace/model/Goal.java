package com.herspace.model;

import java.time.LocalDate;

/**
 * A personal fitness goal, for example "Complete 10 workouts"
 * with a target of 10 workouts before a deadline.
 *
 * This class only holds data and goal rules. It does not know
 * anything about the user interface (MVC model).
 */
public class Goal {

    // Status values shown on the Goals page
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";

    private String title;
    private String description;
    private double target;
    private double current;
    private String unit;
    private final LocalDate startDate;
    private LocalDate deadline;

    public Goal(String title, String description, double target,
                String unit, double current,
                LocalDate startDate, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.target = target;
        this.unit = unit;
        this.current = current;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    // ----- Calculated values -----

    // Progress from 0.0 to 1.0 (current divided by target, capped at 100%)
    public double getProgress() {
        if (target <= 0) {
            return 0;
        }
        return Math.min(1.0, Math.max(0.0, current / target));
    }

    // Progress as a whole percentage from 0 to 100
    public int getPercentage() {
        return (int) Math.round(getProgress() * 100);
    }

    // A goal is completed as soon as the target is reached
    public boolean isCompleted() {
        return target > 0 && current >= target;
    }

    public String getStatus() {
        return isCompleted() ? STATUS_COMPLETED : STATUS_IN_PROGRESS;
    }

    // True when the deadline has passed and the goal is not completed
    public boolean isOverdue() {
        return !isCompleted() && deadline.isBefore(LocalDate.now());
    }

    // Adds progress, never going above the target
    public void addProgress(double amount) {
        current = Math.min(target, current + amount);
    }

    // ----- Getters and setters -----

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
