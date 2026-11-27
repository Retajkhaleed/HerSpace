package com.herspace.model;

import java.time.LocalDate;

public class Meal {

    private String mealName;
    private String mealType;
    private double calories;
    private LocalDate date;

    public Meal(String mealName, String mealType,
                double calories, LocalDate date) {
        this.mealName = mealName;
        this.mealType = mealType;
        this.calories = calories;
        this.date = date;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}