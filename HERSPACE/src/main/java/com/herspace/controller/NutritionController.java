
package com.herspace.controller;

import com.herspace.model.Meal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NutritionController {

    @FXML
    private Button addMealButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label totalCaloriesLabel;

    @FXML
    private Label mealCountLabel;

    @FXML
    private TableView<Meal> mealTable;

    @FXML
    private TableColumn<Meal, String> mealNameColumn;

    @FXML
    private TableColumn<Meal, String> mealTypeColumn;

    @FXML
    private TableColumn<Meal, String> caloriesColumn;

    @FXML
    private TableColumn<Meal, Void> actionsColumn;

    private static final List<Meal> meals = new ArrayList<>();

    private final DecimalFormat calorieFormat =
            new DecimalFormat("0.##",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    // Initializes the table, selected date and page events.
    @FXML
    private void initialize() {

        datePicker.setValue(LocalDate.now());

        datePicker.setEditable(false);

        configureTable();

        addMealButton.setOnAction(e -> openAddMealDialog());

        datePicker.valueProperty().addListener(
                (observable, oldDate, newDate) -> refreshPage()
        );

        refreshPage();
    }

    // Connects the table columns to the meal data.
    private void configureTable() {

        mealNameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getMealName()
                )
        );

        mealTypeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getMealType()
                )
        );

        caloriesColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        calorieFormat.format(
                                data.getValue().getCalories()
                        )
                )
        );

        caloriesColumn.setComparator((first, second) -> {
            try {
                return Double.compare(
                        calorieFormat.parse(first).doubleValue(),
                        calorieFormat.parse(second).doubleValue()
                );
            } catch (java.text.ParseException e) {
                return first.compareTo(second);
            }
        });

        mealTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Disables row selection and table focus traversal.
        mealTable.setSelectionModel(null);
        mealTable.setFocusTraversable(false);

        configureActionsColumn();
    }

    // Creates edit and delete buttons for each meal row.
    private void configureActionsColumn() {

        actionsColumn.setCellFactory(column ->

                new TableCell<Meal, Void>() {

            private final Button editButton =
                    new Button("Edit");

            private final Button deleteButton =
                    new Button("Delete");

            private final HBox buttons =
                    new HBox(8, editButton, deleteButton);

            {
                editButton.getStyleClass().add("secondary-button");

                deleteButton.getStyleClass().add("danger-button");

                buttons.setAlignment(Pos.CENTER);

                editButton.setOnAction(e -> {

                    Meal meal = getTableRow().getItem();

                    if (meal != null) {
                        openEditMealDialog(meal);
                    }

                    e.consume();
                });

                deleteButton.setOnAction(e -> {

                    Meal meal = getTableRow().getItem();

                    if (meal != null) {
                        deleteMeal(meal);
                    }

                    e.consume();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    // Filters meals by the selected date and updates the summary.
    private void refreshPage() {

        LocalDate selectedDate = datePicker.getValue();

        Predicate<Meal> matchesDate =
                meal -> meal.getDate().equals(selectedDate);

        List<Meal> dailyMeals = meals.stream()
                .filter(matchesDate)
                .collect(Collectors.toList());

        mealTable.setItems(
                FXCollections.observableArrayList(dailyMeals)
        );

        // Refreshes the displayed values after editing a meal.
        mealTable.refresh();

        double totalCalories = dailyMeals.stream()
                .collect(Collectors.summingDouble(Meal::getCalories));

        long mealCount = dailyMeals.stream()
                .collect(Collectors.counting());

        totalCaloriesLabel.setText(
                calorieFormat.format(totalCalories)
        );

        mealCountLabel.setText(
                String.valueOf(mealCount)
        );
    }

    // Opens the add dialog and stores the returned meal.
    private void openAddMealDialog() {

        Meal meal = showMealDialog(null);

        if (meal != null) {

            meals.add(meal);

            datePicker.setValue(meal.getDate());

            refreshPage();
        }
    }

    // Opens the edit dialog and updates the selected meal.
    private void openEditMealDialog(Meal originalMeal) {

        Meal updatedMeal = showMealDialog(originalMeal);

        if (updatedMeal != null) {

            originalMeal.setMealName(updatedMeal.getMealName());

            originalMeal.setMealType(updatedMeal.getMealType());

            originalMeal.setCalories(updatedMeal.getCalories());

            originalMeal.setDate(updatedMeal.getDate());

            datePicker.setValue(updatedMeal.getDate());

            refreshPage();
        }
    }

    // Displays the shared dialog in add or edit mode.
    private Meal showMealDialog(Meal existingMeal) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/herspace/view/MealDialog.fxml"
                    )
            );

            Parent root = loader.load();

            MealDialogController controller =
                    loader.getController();

            Stage stage = new Stage();

            if (existingMeal == null) {

                controller.setAddMode(datePicker.getValue());

                stage.setTitle("Add Meal");

            } else {

                controller.setEditMode(existingMeal);

                stage.setTitle("Edit Meal");
            }

            stage.setScene(new Scene(root));

            stage.initOwner(
                    addMealButton.getScene().getWindow()
            );

            stage.initModality(Modality.WINDOW_MODAL);

            stage.setResizable(false);

            stage.showAndWait();

            return controller.getResultMeal();

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");

            alert.setHeaderText(null);

            alert.setContentText(
                    "Unable to open the meal dialog."
            );

            alert.showAndWait();

            return null;
        }
    }

    // Removes a meal after the user confirms the deletion.
    private void deleteMeal(Meal meal) {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Delete Meal");

        alert.setHeaderText("Delete this meal?");

        alert.setContentText(
                "Are you sure you want to delete "
                + meal.getMealName() + "?"
        );

        alert.getButtonTypes().setAll(
                ButtonType.YES,
                ButtonType.CANCEL
        );

        alert.initOwner(
                mealTable.getScene().getWindow()
        );

        if (alert.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.YES) {

            meals.remove(meal);

            refreshPage();
        }
    }
}
