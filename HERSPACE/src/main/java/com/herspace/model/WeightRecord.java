package com.herspace.model;

import java.time.LocalDate;

public class WeightRecord {

private final LocalDate date;
private final double weight;

public WeightRecord(LocalDate date, double weight) {
    this.date = date;
    this.weight = weight;
}

public LocalDate getDate() {
    return date;
}

public double getWeight() {
    return weight;
}
}