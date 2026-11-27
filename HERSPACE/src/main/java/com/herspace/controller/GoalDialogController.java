package com.herspace.controller;

import com.herspace.model.Goal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller of the Log Progress dialog (LogGoalProgress.fxml).
 * Returns the amount the user wants to add to a goal.
 */
public class LogGoalProgressController {

    @FXML
    private Label goalNameLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final DecimalFormat numberFormat =
            new DecimalFormat("0.##",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private double remaining;

    private Double resultAmount;

    // Initializes the dialog events.
    @FXML
    private void initialize() {

        saveButton.setOnAction(e -> saveProgress());

        cancelButton.setOnAction(e -> closeDialog());

        // The button stays disabled while the field is empty
        saveButton.disableProperty().bind(
                amountField.textProperty().isEmpty());

        amountField.textProperty().addListener((obs, oldValue, newValue) -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
    }

    // Shows the selected goal's current state.
    public void setGoal(Goal goal) {

        goalNameLabel.setText(goal.getTitle());

        summaryLabel.setText(
                numberFormat.format(goal.getCurrent()) + " / "
                + numberFormat.format(goal.getTarget()) + " "
                + goal.getUnit() + " (" + goal.getPercentage() + "%)");

        amountLabel.setText("Amount to add (" + goal.getUnit() + ")");

        remaining = goal.getTarget() - goal.getCurrent();
    }

    // Validates the amount entered by the user.
    private void saveProgress() {

        double amount;

        try {

            amount = Double.parseDouble(amountField.getText().trim());

        } catch (NumberFormatException e) {

            showError("Please enter a valid number.");
            return;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            showError("Please enter a number greater than zero.");
            return;
        }

        if (amount > remaining + 1e-9) {
            showError("This is more than the remaining "
                    + numberFormat.format(remaining) + ".");
            return;
        }

        resultAmount = amount;

        closeDialog();
    }

    private void showError(String message) {

        errorLabel.setText(message);

        errorLabel.setVisible(true);

        errorLabel.setManaged(true);

        Stage stage = (Stage) errorLabel.getScene().getWindow();

        stage.sizeToScene();
    }

    private void closeDialog() {

        Stage stage = (Stage) cancelButton
                .getScene().getWindow();

        stage.close();
    }

    // Returns null when the user cancelled.
    public Double getResultAmount() {
        return resultAmount;
    }
}
