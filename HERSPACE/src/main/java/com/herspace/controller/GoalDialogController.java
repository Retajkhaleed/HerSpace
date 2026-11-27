package com.herspace.controller;

import com.herspace.model.Goal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller of the Add / Edit Goal dialog (GoalDialog.fxml).
 * Validates the input and returns a Goal to the Goals page.
 */
public class GoalDialogController {

    private static final int MAX_TITLE_LENGTH = 60;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final double MAX_TARGET = 1_000_000;

    @FXML
    private Label dialogTitle;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField targetField;

    @FXML
    private ComboBox<String> unitComboBox;

    @FXML
    private TextField currentField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveGoalButton;

    private final DecimalFormat numberFormat =
            new DecimalFormat("0.##",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    // Existing goals, used to detect duplicate titles
    private List<Goal> existingGoals;

    // The goal being edited (null when adding a new goal)
    private Goal editedGoal;

    private Goal resultGoal;

    // Initializes the unit list and the dialog events.
    @FXML
    private void initialize() {

        unitComboBox.getItems().addAll(
                "workouts", "minutes", "km", "steps", "days", "kg"
        );

        deadlinePicker.setValue(LocalDate.now().plusMonths(1));

        currentField.setText("0");

        saveGoalButton.setOnAction(e -> saveGoal());

        cancelButton.setOnAction(e -> closeDialog());

        // Save stays disabled until the required fields are filled
        saveGoalButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                .or(targetField.textProperty().isEmpty())
                .or(unitComboBox.valueProperty().isNull())
                .or(deadlinePicker.valueProperty().isNull())
        );

        // Hide the old message as soon as the user edits a field
        titleField.textProperty().addListener(
                (obs, oldValue, newValue) -> hideError());
        targetField.textProperty().addListener(
                (obs, oldValue, newValue) -> hideError());
        currentField.textProperty().addListener(
                (obs, oldValue, newValue) -> hideError());
    }

    // Gives the dialog the goals that already exist.
    public void setExistingGoals(List<Goal> goals) {
        this.existingGoals = goals;
    }

    // Prepares the dialog with the selected goal's data.
    public void setEditMode(Goal goal) {

        editedGoal = goal;

        dialogTitle.setText("Edit Goal");

        saveGoalButton.setText("Save Changes");

        titleField.setText(goal.getTitle());

        descriptionArea.setText(goal.getDescription());

        targetField.setText(numberFormat.format(goal.getTarget()));

        unitComboBox.setValue(goal.getUnit());

        currentField.setText(numberFormat.format(goal.getCurrent()));

        deadlinePicker.setValue(goal.getDeadline());
    }

    // Validates the input and creates a goal object.
    private void saveGoal() {

        String title = titleField.getText().trim();

        String description = descriptionArea.getText().trim();

        String unit = unitComboBox.getValue();

        LocalDate deadline = deadlinePicker.getValue();

        if (title.isEmpty() || unit == null || deadline == null
                || targetField.getText().trim().isEmpty()) {

            showError("Please complete all required fields.");
            return;
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            showError("The title cannot be longer than "
                    + MAX_TITLE_LENGTH + " characters.");
            return;
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            showError("The description cannot be longer than "
                    + MAX_DESCRIPTION_LENGTH + " characters.");
            return;
        }

        // Rejects another goal that has the same title
        Predicate<Goal> sameTitle = goal ->
                goal != editedGoal
                && goal.getTitle().equalsIgnoreCase(title);

        if (existingGoals != null
                && existingGoals.stream().anyMatch(sameTitle)) {
            showError("A goal with this title already exists.");
            return;
        }

        double target;
        double current;

        try {

            target = Double.parseDouble(targetField.getText().trim());

            String currentText = currentField.getText().trim();

            current = currentText.isEmpty()
                    ? 0 : Double.parseDouble(currentText);

        } catch (NumberFormatException e) {

            showError("Please enter valid numbers for target and progress.");
            return;
        }

        // Positive finite number up to the maximum
        Predicate<Double> validTarget = value ->
                Double.isFinite(value) && value > 0 && value <= MAX_TARGET;

        if (!validTarget.test(target)) {
            showError("The target must be greater than 0 and at most "
                    + numberFormat.format(MAX_TARGET) + ".");
            return;
        }

        if (!Double.isFinite(current) || current < 0) {
            showError("Progress cannot be negative.");
            return;
        }

        if (current > target) {
            showError("Progress cannot be greater than the target.");
            return;
        }

        LocalDate startDate = (editedGoal == null)
                ? LocalDate.now() : editedGoal.getStartDate();

        if (deadline.isBefore(startDate)) {
            showError("The deadline cannot be before the start date ("
                    + startDate + ").");
            return;
        }

        resultGoal = new Goal(title, description, target,
                unit, current, startDate, deadline);

        closeDialog();
    }

    // Displays a validation message and adjusts the window size.
    private void showError(String message) {

        errorLabel.setText(message);

        errorLabel.setVisible(true);

        errorLabel.setManaged(true);

        Stage stage = (Stage) errorLabel.getScene().getWindow();

        stage.sizeToScene();
    }

    private void hideError() {

        errorLabel.setVisible(false);

        errorLabel.setManaged(false);
    }

    // Closes the current dialog window.
    private void closeDialog() {

        Stage stage = (Stage) cancelButton
                .getScene().getWindow();

        stage.close();
    }

    public Goal getResultGoal() {
        return resultGoal;
    }
}

    // Returns null when the user cancelled.
    public Double getResultAmount() {
        return resultAmount;
    }
}
