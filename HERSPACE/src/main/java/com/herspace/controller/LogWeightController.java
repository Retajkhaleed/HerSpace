package com.herspace.controller;

import com.herspace.model.WeightRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LogWeightController {

@FXML
private TextField weightField;

@FXML
private DatePicker datePicker;

@FXML
private Button addButton;

@FXML
private Button cancelButton;

private List<WeightRecord> records;
private WeightRecord newRecord;

@FXML
public void initialize() {

    datePicker.setValue(LocalDate.now());

    // Action 
    addButton.setOnAction(e -> addWeight());
    cancelButton.setOnAction(e -> closeWindow());

    // Property Binding
    addButton.disableProperty().bind(
            weightField.textProperty().isEmpty()
            .or(datePicker.valueProperty().isNull())
    );
}

public void setRecords(List<WeightRecord> records) {
    this.records = records;
}

public WeightRecord getNewRecord() {
    return newRecord;
}

private void addWeight() {

    String input = weightField.getText().trim();
    LocalDate date = datePicker.getValue();

    // Empty fields
    if (input.isEmpty() || date == null) {
        showError("Please complete all fields.");
        return;
    }

    try {

        double weight = Double.parseDouble(input);

        Predicate<Double> validWeight =
                w -> w > 0 && w <= 500;

        if (!validWeight.test(weight)) {
            showError("Please enter a valid weight.");
            return;
        }

        // Date validation
        if (date.isAfter(LocalDate.now())) {
            showError("Date cannot be in the future.");
            return;
        }

        // Check duplicate dates
        boolean duplicate = records.stream()
                .anyMatch(r -> r.getDate().equals(date));

        if (duplicate) {
            showError("A record already exists for this date.");
            return;
        }

        newRecord = new WeightRecord(date, weight);

        closeWindow();

    } catch (NumberFormatException e) {
        showError("Please enter a valid number.");
    }
}

private void closeWindow() {

    Stage stage = (Stage) addButton
            .getScene().getWindow();

    stage.close();
}

private void showError(String message) {

    Alert alert = new Alert(Alert.AlertType.ERROR);

    alert.setTitle("Input Error");
    alert.setHeaderText(null);
    alert.setContentText(message);

    alert.showAndWait();
}
}