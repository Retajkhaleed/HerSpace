package com.herspace.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * The workout planned for today, shown in the
 * "Today's Workout" card on the Dashboard.
 */
public class PlannedWorkout {

    private final String name;
    private final int durationMinutes;
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    public PlannedWorkout(String name, int durationMinutes) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Workout name is required.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }

        this.name = name.trim();
        this.durationMinutes = durationMinutes;
    }

    public String getName() {
        return name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean value) {
        completed.set(value);
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    @Override
    public String toString() {
        return name + " (" + durationMinutes + " minutes)";
    }
}
