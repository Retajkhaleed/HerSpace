package com.herspace.model;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * One day of activity shown on the Dashboard:
 * calories eaten, water drunk and workouts completed.
 *
 * JavaFX properties are used so the Dashboard labels
 * can be bound to these values and update automatically.
 */
public class DailyRecord {

    private final LocalDate date;
    private final IntegerProperty calories;
    private final DoubleProperty waterLiters;
    private final IntegerProperty workoutsCompleted;

    public DailyRecord(LocalDate date, int calories,
                       double waterLiters, int workoutsCompleted) {

        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (calories < 0 || waterLiters < 0 || workoutsCompleted < 0) {
            throw new IllegalArgumentException("Values cannot be negative.");
        }

        this.date = date;
        this.calories = new SimpleIntegerProperty(calories);
        this.waterLiters = new SimpleDoubleProperty(waterLiters);
        this.workoutsCompleted = new SimpleIntegerProperty(workoutsCompleted);
    }

    // ----- Date -----
    public LocalDate getDate() {
        return date;
    }

    // ----- Calories -----
    public int getCalories() {
        return calories.get();
    }

    public void setCalories(int value) {
        calories.set(value);
    }

    public IntegerProperty caloriesProperty() {
        return calories;
    }

    // ----- Water -----
    public double getWaterLiters() {
        return waterLiters.get();
    }

    public void setWaterLiters(double value) {
        waterLiters.set(value);
    }

    public DoubleProperty waterLitersProperty() {
        return waterLiters;
    }

    // ----- Workouts -----
    public int getWorkoutsCompleted() {
        return workoutsCompleted.get();
    }

    public void setWorkoutsCompleted(int value) {
        workoutsCompleted.set(value);
    }

    public IntegerProperty workoutsCompletedProperty() {
        return workoutsCompleted;
    }

    @Override
    public String toString() {
        return date + ": " + getCalories() + " kcal, "
                + getWaterLiters() + " L, "
                + getWorkoutsCompleted() + " workouts";
    }
}
