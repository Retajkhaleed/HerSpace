package com.herspace.controller;

import com.herspace.model.Goal;
import com.herspace.model.GoalStore;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller of the Goals page (Goals.fxml).
 *
 * Lets the user create, edit, delete and track personal fitness
 * goals. The data is kept in GoalStore (model); this class only
 * handles events and shows the data.
 */
public class GoalsController {

    private static final String FILTER_ALL = "All Goals";

    @FXML
    private Button addGoalButton;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private Label totalGoalsLabel;

    @FXML
    private Label inProgressLabel;

    @FXML
    private Label completedLabel;

    @FXML
    private Label placeholderLabel;

    @FXML
    private TableView<Goal> goalTable;

    @FXML
    private TableColumn<Goal, Goal> goalColumn;

    @FXML
    private TableColumn<Goal, Goal> progressColumn;

    @FXML
    private TableColumn<Goal, Goal> deadlineColumn;

    @FXML
    private TableColumn<Goal, Goal> statusColumn;

    @FXML
    private TableColumn<Goal, Goal> actionsColumn;

    private final GoalStore store = GoalStore.getInstance();

    private final DecimalFormat numberFormat =
            new DecimalFormat("0.##",
                    DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    // Initializes the table, the filter and the page events.
    @FXML
    private void initialize() {

        statusFilterComboBox.getItems().addAll(
                FILTER_ALL,
                Goal.STATUS_IN_PROGRESS,
                Goal.STATUS_COMPLETED
        );

        statusFilterComboBox.setValue(FILTER_ALL);

        configureTable();

        addGoalButton.setOnAction(e -> openAddGoalDialog());

        // Property event: the list changes as soon as the filter changes
        statusFilterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> refreshPage()
        );

        refreshPage();
    }

    // Connects the table columns to the goal data.
    private void configureTable() {

        // Every column receives the whole goal and draws its own cell
        List<TableColumn<Goal, Goal>> columns = List.of(
                goalColumn, progressColumn,
                deadlineColumn, statusColumn, actionsColumn
        );

        columns.forEach(column -> {
            column.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(data.getValue()));
            column.setSortable(false);
        });

        goalColumn.setCellFactory(column -> new GoalCell(this::buildGoalCell));

        progressColumn.setCellFactory(column ->
                new GoalCell(this::buildProgressCell));

        deadlineColumn.setCellFactory(column ->
                new GoalCell(this::buildDeadlineCell));

        statusColumn.setCellFactory(column ->
                new GoalCell(this::buildStatusCell));

        configureActionsColumn();

        goalTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Disables row selection and table focus traversal.
        goalTable.setSelectionModel(null);
        goalTable.setFocusTraversable(false);

        // Mouse event: double-click a row to edit the goal
        goalTable.setRowFactory(table -> {

            TableRow<Goal> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !row.isEmpty()) {

                    openEditGoalDialog(row.getItem());
                }
            });

            return row;
        });
    }

    // Table cell that draws a node built from the goal of its row.
    private static class GoalCell extends TableCell<Goal, Goal> {

        private final java.util.function.Function<Goal, javafx.scene.Node> builder;

        GoalCell(java.util.function.Function<Goal, javafx.scene.Node> builder) {
            this.builder = builder;

            // Slightly smaller padding so the columns have more room
            setStyle("-fx-padding: 6 8 6 8;");
        }

        @Override
        protected void updateItem(Goal goal, boolean empty) {

            super.updateItem(goal, empty);

            if (empty || goal == null) {
                setGraphic(null);
            } else {
                setGraphic(builder.apply(goal));
            }
        }
    }

    // Goal title with its description underneath.
    private javafx.scene.Node buildGoalCell(Goal goal) {

        Label title = new Label(goal.getTitle());
        title.getStyleClass().add("body-text");
        title.setStyle("-fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(2, title);
        box.setAlignment(Pos.CENTER_LEFT);

        if (!goal.getDescription().isEmpty()) {

            Label description = new Label(goal.getDescription());
            description.getStyleClass().add("secondary-text");
            description.setMaxWidth(Double.MAX_VALUE);

            box.getChildren().add(description);
        }

        return box;
    }

    // Progress bar with the completion percentage and the
    // current value compared with the target (for example 7 / 10 workouts).
    private javafx.scene.Node buildProgressCell(Goal goal) {

        ProgressBar bar = new ProgressBar(goal.getProgress());
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        Label percent = new Label(goal.getPercentage() + "%");
        percent.getStyleClass().add("body-text");
        percent.setStyle("-fx-font-weight: bold;");
        percent.setMinWidth(Region.USE_PREF_SIZE);

        HBox barRow = new HBox(8, bar, percent);
        barRow.setAlignment(Pos.CENTER_LEFT);

        Label values = new Label(
                numberFormat.format(goal.getCurrent()) + " / "
                + numberFormat.format(goal.getTarget()) + " "
                + goal.getUnit());
        values.getStyleClass().add("secondary-text");

        VBox box = new VBox(4, barRow, values);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    // Deadline date with the days left or an overdue warning.
    private javafx.scene.Node buildDeadlineCell(Goal goal) {

        Label date = new Label(goal.getDeadline().format(dateFormat));
        date.getStyleClass().add("body-text");

        VBox box = new VBox(2, date);
        box.setAlignment(Pos.CENTER_LEFT);

        if (!goal.isCompleted()) {

            long daysLeft = ChronoUnit.DAYS.between(
                    LocalDate.now(), goal.getDeadline());

            Label note = new Label();

            if (daysLeft < 0) {
                note.setText("Overdue");
                note.getStyleClass().add("error-message");
            } else {
                note.setText(daysLeft == 0
                        ? "Due today" : daysLeft + " days left");
                note.getStyleClass().add("secondary-text");
            }

            box.getChildren().add(note);
        }

        return box;
    }

    // Status badge: In Progress or Completed.
    private javafx.scene.Node buildStatusCell(Goal goal) {

        Label badge = new Label(goal.getStatus());

        badge.getStyleClass().add(goal.isCompleted()
                ? "success-message" : "warning-message");

        badge.setStyle("-fx-padding: 4 12 4 12; -fx-font-size: 12px;");

        HBox box = new HBox(badge);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    // Creates log, edit and delete buttons for each goal row.
    private void configureActionsColumn() {

        actionsColumn.setCellFactory(column ->

                new TableCell<Goal, Goal>() {

            private final Button logButton =
                    new Button("Log");

            private final Button editButton =
                    new Button("Edit");

            private final Button deleteButton =
                    new Button("Delete");

            private final HBox buttons =
                    new HBox(6, logButton, editButton, deleteButton);

            {
                setStyle("-fx-padding: 6 4 6 4;");

                logButton.getStyleClass().add("primary-button");

                editButton.getStyleClass().add("secondary-button");

                deleteButton.getStyleClass().add("danger-button");

                // Smaller buttons so the three of them fit in the column
                for (Button button : List.of(
                        logButton, editButton, deleteButton)) {
                    button.setStyle("-fx-padding: 6 8 6 8;");
                }

                buttons.setAlignment(Pos.CENTER);

                logButton.setOnAction(e -> {

                    Goal goal = getItem();

                    if (goal != null) {
                        openLogProgressDialog(goal);
                    }

                    e.consume();
                });

                editButton.setOnAction(e -> {

                    Goal goal = getItem();

                    if (goal != null) {
                        openEditGoalDialog(goal);
                    }

                    e.consume();
                });

                deleteButton.setOnAction(e -> {

                    Goal goal = getItem();

                    if (goal != null) {
                        deleteGoal(goal);
                    }

                    e.consume();
                });
            }

            @Override
            protected void updateItem(Goal goal, boolean empty) {

                super.updateItem(goal, empty);

                if (empty || goal == null) {
                    setGraphic(null);
                } else {
                    // A completed goal has nothing left to log
                    logButton.setDisable(goal.isCompleted());
                    setGraphic(buttons);
                }
            }
        });
    }

    // Filters and sorts the goals, then updates the summary cards.
    private void refreshPage() {

        String selectedFilter = statusFilterComboBox.getValue();

        Predicate<Goal> matchesFilter = goal ->
                FILTER_ALL.equals(selectedFilter)
                || goal.getStatus().equals(selectedFilter);

        // Closest deadline first, then alphabetically
        List<Goal> visibleGoals = store.getGoals().stream()
                .filter(matchesFilter)
                .sorted(Comparator.comparing(Goal::getDeadline)
                        .thenComparing(Goal::getTitle,
                                String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        goalTable.setItems(
                FXCollections.observableArrayList(visibleGoals)
        );

        // Refreshes the displayed values after editing a goal.
        goalTable.refresh();

        placeholderLabel.setText(store.getGoals().isEmpty()
                ? "No goals yet. Click + Add Goal to create one."
                : "No goals match this filter.");

        // Statistics: number of goals for each status
        Map<String, Long> countByStatus = store.getGoals().stream()
                .collect(Collectors.groupingBy(
                        Goal::getStatus, Collectors.counting()));

        long inProgress = countByStatus.getOrDefault(
                Goal.STATUS_IN_PROGRESS, 0L);

        long completed = countByStatus.getOrDefault(
                Goal.STATUS_COMPLETED, 0L);

        totalGoalsLabel.setText(String.valueOf(inProgress + completed));

        inProgressLabel.setText(String.valueOf(inProgress));

        completedLabel.setText(String.valueOf(completed));
    }

    // Opens the add dialog and stores the returned goal.
    private void openAddGoalDialog() {

        Goal goal = showGoalDialog(null);

        if (goal != null) {

            store.add(goal);

            // Make sure the new goal is visible
            statusFilterComboBox.setValue(FILTER_ALL);

            refreshPage();
        }
    }

    // Opens the edit dialog and updates the selected goal.
    private void openEditGoalDialog(Goal originalGoal) {

        Goal updatedGoal = showGoalDialog(originalGoal);

        if (updatedGoal != null) {

            originalGoal.setTitle(updatedGoal.getTitle());

            originalGoal.setDescription(updatedGoal.getDescription());

            originalGoal.setTarget(updatedGoal.getTarget());

            originalGoal.setUnit(updatedGoal.getUnit());

            originalGoal.setCurrent(updatedGoal.getCurrent());

            originalGoal.setDeadline(updatedGoal.getDeadline());

            store.save();

            refreshPage();
        }
    }

    // Displays the shared dialog in add or edit mode.
    private Goal showGoalDialog(Goal existingGoal) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/herspace/view/GoalDialog.fxml"
                    )
            );

            Parent root = loader.load();

            GoalDialogController controller =
                    loader.getController();

            controller.setExistingGoals(store.getGoals());

            Stage stage = new Stage();

            if (existingGoal == null) {

                stage.setTitle("Add Goal");

            } else {

                controller.setEditMode(existingGoal);

                stage.setTitle("Edit Goal");
            }

            stage.setScene(new Scene(root));

            stage.initOwner(
                    addGoalButton.getScene().getWindow()
            );

            stage.initModality(Modality.WINDOW_MODAL);

            stage.setResizable(false);

            stage.showAndWait();

            return controller.getResultGoal();

            } catch (IOException e) {

            e.printStackTrace();   // أضف هذا السطر

            showErrorAlert("Unable to open the goal dialog.");

            return null;
        }

    // Opens the dialog used to add progress to a goal.
    private void openLogProgressDialog(Goal goal) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/herspace/view/LogGoalProgress.fxml"
                    )
            );

            Parent root = loader.load();

            LogGoalProgressController controller =
                    loader.getController();

            controller.setGoal(goal);

            Stage stage = new Stage();

            stage.setTitle("Log Progress");

            stage.setScene(new Scene(root));

            stage.initOwner(
                    addGoalButton.getScene().getWindow()
            );

            stage.initModality(Modality.WINDOW_MODAL);

            stage.setResizable(false);

            stage.showAndWait();

            Double amount = controller.getResultAmount();

            if (amount != null) {

                boolean wasCompleted = goal.isCompleted();

                goal.addProgress(amount);

                store.save();

                refreshPage();

                // Let the user know when the target has been reached
                if (!wasCompleted && goal.isCompleted()) {
                    showGoalCompletedAlert(goal);
                }
            }

        } catch (IOException e) {

            showErrorAlert("Unable to open the progress dialog.");
        }
    }

    private void showGoalCompletedAlert(Goal goal) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Goal Completed");

        alert.setHeaderText("Congratulations!");

        alert.setContentText(
                "You completed the goal \"" + goal.getTitle() + "\"."
        );

        alert.initOwner(goalTable.getScene().getWindow());

        alert.showAndWait();
    }

    private void showErrorAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // Removes a goal after the user confirms the deletion.
    private void deleteGoal(Goal goal) {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Delete Goal");

        alert.setHeaderText("Delete this goal?");

        alert.setContentText(
                "Are you sure you want to delete "
                + goal.getTitle() + "?"
        );

        alert.getButtonTypes().setAll(
                ButtonType.YES,
                ButtonType.CANCEL
        );

        alert.initOwner(
                goalTable.getScene().getWindow()
        );

        if (alert.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.YES) {

            store.remove(goal);

            refreshPage();
        }
    }
}
