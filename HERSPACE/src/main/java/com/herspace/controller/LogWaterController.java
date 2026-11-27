package com.herspace.controller;

import com.herspace.model.DashboardData;
import java.util.function.Predicate;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller of the "Log Water" window (LogWater.fxml).
 * Opened from the Dashboard to add the water the user drank.
 */
public class LogWaterController {

    @FXML
    private TextField waterField;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    // The amount entered by the user (null if cancelled)
    private Double amount;

    @FXML
    public void initialize() {

        // Action events
        addButton.setOnAction(e -> addWater());
        cancelButton.setOnAction(e -> closeWindow());

        // Property binding: Add is disabled while the field is empty
        addButton.disableProperty().bind(
                waterField.textProperty().isEmpty());
    }

    public Double getAmount() {
        return amount;
    }

    private void addWater() {

        String input = waterField.getText().trim();

        // Empty field
        if (input.isEmpty()) {
            showError("Please enter the amount of water.");
            return;
        }

        try {

            double liters = Double.parseDouble(input);

            Predicate<Double> validAmount =
                    w -> w >= 0.1 && w <= DashboardData.MAX_WATER_PER_ENTRY;

            if (!validAmount.test(liters)) {
                showError("Amount must be between 0.1 and "
                        + DashboardData.MAX_WATER_PER_ENTRY + " liters.");
                return;
            }

            amount = liters;

            closeWindow();

        } catch (NumberFormatException e) {
            showError("Please enter a valid number.");
        }
    }

    private void closeWindow() {

        Stage stage = (Stage) addButton.getScene().getWindow();

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
