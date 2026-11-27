package com.herspace.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Holds all the information displayed on the Dashboard.
 *
 * Relationships between the model classes:
 *   DashboardData 1 ---- * DailyRecord     (the last 7 days)
 *   DashboardData 1 ---- 1 PlannedWorkout  (today's workout)
 *
 * One shared instance is kept for the whole application run,
 * so the values stay the same when the user leaves the
 * Dashboard and comes back to it.
 */
public class DashboardData {

    // Daily water target used by the weekly summary (liters)
    public static final double DAILY_WATER_TARGET = 2.0;

    // Largest amount of water accepted in a single entry (liters)
    public static final double MAX_WATER_PER_ENTRY = 3.0;

    private static DashboardData instance;

    private final ObservableList<DailyRecord> weekRecords =
            FXCollections.observableArrayList();

    private final PlannedWorkout todayWorkout;

    private final String goalTitle;
    private final int goalTarget;

    private final List<String> tips;
    private int currentTipIndex = 0;

    private DashboardData() {

        // Sample data for the last 7 days (today is the last record).
        // These can later be replaced by data from the other pages.
        int[] calories = {1520, 1380, 1610, 1290, 1475, 1550, 1450};
        double[] water = {2.1, 1.6, 2.4, 1.9, 2.0, 1.4, 1.8};
        int[] workouts = {1, 0, 0, 1, 0, 0, 1};

        LocalDate today = LocalDate.now();

        // Build the 7 records with a stream instead of a loop
        weekRecords.setAll(
            IntStream.range(0, 7)
                .mapToObj(i -> new DailyRecord(
                        today.minusDays(6 - i),
                        calories[i],
                        water[i],
                        workouts[i]))
                .collect(Collectors.toList())
        );

        todayWorkout = new PlannedWorkout("Lower Body Workout", 30);

        goalTitle = "Complete 10 workouts";
        goalTarget = 10;

        tips = Arrays.asList(
            "Stay consistent! Small daily efforts lead to big results.",
            "Drink a glass of water before every meal.",
            "Rest days are part of the plan, your muscles grow while you rest.",
            "Warm up for 5 minutes before you start any workout.",
            "Add protein to your breakfast to stay full for longer."
        );
    }

    // Returns the single shared Dashboard data object
    public static DashboardData getInstance() {
        if (instance == null) {
            instance = new DashboardData();
        }
        return instance;
    }

    // ===== Basic getters =====

    public ObservableList<DailyRecord> getWeekRecords() {
        return weekRecords;
    }

    // Today's record is always the last one in the list
    public DailyRecord getToday() {
        return weekRecords.get(weekRecords.size() - 1);
    }

    public PlannedWorkout getTodayWorkout() {
        return todayWorkout;
    }

    public String getGoalTitle() {
        return goalTitle;
    }

    public int getGoalTarget() {
        return goalTarget;
    }

    // ===== Actions =====

    /**
     * Adds water to today's record after validating the amount.
     *
     * @throws IllegalArgumentException if the amount is not valid
     */
    public void addWater(double liters) {

        if (liters <= 0 || liters > MAX_WATER_PER_ENTRY) {
            throw new IllegalArgumentException(
                "Water must be between 0.1 and " + MAX_WATER_PER_ENTRY + " L.");
        }

        DailyRecord today = getToday();
        today.setWaterLiters(today.getWaterLiters() + liters);
    }

    // Marks today's workout as done and counts it once
    public void completeTodayWorkout() {

        if (todayWorkout.isCompleted()) {
            return;
        }

        todayWorkout.setCompleted(true);

        DailyRecord today = getToday();
        today.setWorkoutsCompleted(today.getWorkoutsCompleted() + 1);
    }

    // Moves to the next tip and returns it
    public String nextTip() {
        currentTipIndex = (currentTipIndex + 1) % tips.size();
        return getCurrentTip();
    }

    public String getCurrentTip() {
        return tips.get(currentTipIndex);
    }

    // ===== Weekly statistics (streams + Collectors) =====

    // Total workouts done in the last 7 days
    public int getWeeklyWorkouts() {
        return weekRecords.stream()
                .collect(Collectors.summingInt(DailyRecord::getWorkoutsCompleted));
    }

    // Average calories per day in the last 7 days
    public double getAverageCalories() {
        return weekRecords.stream()
                .collect(Collectors.averagingInt(DailyRecord::getCalories));
    }

    // Total water drunk in the last 7 days
    public double getWeeklyWater() {
        return weekRecords.stream()
                .collect(Collectors.summingDouble(DailyRecord::getWaterLiters));
    }

    // Number of days where the water target was reached
    public long getDaysWaterTargetReached() {

        Map<Boolean, Long> split = weekRecords.stream()
                .collect(Collectors.partitioningBy(
                        r -> r.getWaterLiters() >= DAILY_WATER_TARGET,
                        Collectors.counting()));

        return split.get(true);
    }

    // Goal progress from 0.0 to 1.0
    public double getGoalProgress() {
        return Math.min(1.0, (double) getWeeklyWorkouts() / goalTarget);
    }
}
