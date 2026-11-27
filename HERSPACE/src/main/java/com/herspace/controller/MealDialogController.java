
package com.herspace.controller;

import com.herspace.model.Meal;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MealDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private TextField mealNameField;

    @FXML
    private ComboBox<String> mealTypeComboBox;

    @FXML
    private TextField caloriesField;

    @FXML
    private DatePicker mealDatePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveMealButton;

    private Meal resultMeal;

    // Initializes the meal types and button events.
    @FXML
    private void initialize() {

        mealTypeComboBox.getItems().addAll(
                "Breakfast",
                "Lunch",
                "Dinner",
                "Snack"
        );

        mealDatePicker.setValue(LocalDate.now());

        saveMealButton.setOnAction(e -> saveMeal());

        cancelButton.setOnAction(e -> closeDialog());
    }

    // Sets the default date for a new meal.
    public void setAddMode(LocalDate date) {
        mealDatePicker.setValue(date);
    }

    // Prepares the dialog with the selected meal's data.
    public void setEditMode(Meal meal) {

        dialogTitle.setText("Edit Meal");

        saveMealButton.setText("Save Changes");

        mealNameField.setText(meal.getMealName());

        mealTypeComboBox.setValue(meal.getMealType());

        caloriesField.setText(
                String.valueOf(meal.getCalories())
        );

        mealDatePicker.setValue(meal.getDate());
    }

    // Validates the input and creates a meal object.
    private void saveMeal() {

        String name = mealNameField.getText().trim();

        String type = mealTypeComboBox.getValue();

        String caloriesText = caloriesField.getText().trim();

        LocalDate date = mealDatePicker.getValue();

        if (name.isEmpty() || type == null
                || caloriesText.isEmpty() || date == null) {

            showError("Please complete all fields.");
            return;
        }

        double calories;

        try {

            calories = Double.parseDouble(caloriesText);

        } catch (NumberFormatException e) {

            showError("Please enter a valid number.");
            return;
        }

        if (!Double.isFinite(calories) || calories <= 0) {
            showError("Please enter a finite number greater than zero.");
            return;
        }

        resultMeal = new Meal(name, type, calories, date);

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

    // Closes the current dialog window.
    private void closeDialog() {

        Stage stage = (Stage) cancelButton
                .getScene().getWindow();

        stage.close();
    }

    public Meal getResultMeal() {
        return resultMeal;
    }
}
