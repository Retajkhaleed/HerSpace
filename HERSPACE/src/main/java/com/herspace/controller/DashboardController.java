package com.herspace.controller;

import com.herspace.model.DailyRecord;
import com.herspace.model.DashboardData;
import com.herspace.model.PlannedWorkout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller of the Dashboard page (Dashboard.fxml).
 *
 * Shows today's statistics, today's workout, goal progress,
 * quick actions and a daily tip. All numbers come from the
 * model (DashboardData) and are connected to the labels with
 * bindings, so the page updates by itself when data changes.
 */
public class DashboardController {

    // ===== FXML components =====
    @FXML private ScrollPane rootPane;

    @FXML private Label dateLabel;
    @FXML private Label weeklySummaryLabel;

    @FXML private Label caloriesLabel;
    @FXML private Label workoutsLabel;
    @FXML private Label waterLabel;
    @FXML private Label goalPercentLabel;
    @FXML private VBox waterCard;

    @FXML private Label workoutNameLabel;
    @FXML private Label workoutDurationLabel;
    @FXML private Button startButton;

    @FXML private Label goalTitleLabel;
    @FXML private Label goalCountLabel;
    @FXML private ProgressBar goalProgressBar;

    @FXML private HBox quickActionsBox;

    @FXML private VBox tipCard;
    @FXML private Label tipLabel;

    @FXML private Label statusLabel;

    // Always show numbers with English digits (1,450 not ١٬٤٥٠),
    // even when Windows is set to Arabic
    private static final Locale NUMBERS_LOCALE = Locale.US;

    // ===== Model =====
    private final DashboardData data = DashboardData.getInstance();

    // Styles for the quick action tiles (normal and mouse-over)
    private static final String TILE_STYLE =
        "-fx-background-color: -hover-bg; -fx-background-radius: 10; "
      + "-fx-text-fill: -text-primary; -fx-text-alignment: center; "
      + "-fx-padding: 12 8 12 8; -fx-cursor: hand; -fx-graphic-text-gap: 6;";

    private static final String TILE_HOVER_STYLE =
        "-fx-background-color: -sidebar-bottom; -fx-background-radius: 10; "
      + "-fx-text-fill: -text-primary; -fx-text-alignment: center; "
      + "-fx-padding: 12 8 12 8; -fx-cursor: hand; -fx-graphic-text-gap: 6;";

    // Called automatically after Dashboard.fxml is loaded
    @FXML
    private void initialize() {
        showDate();
        bindStatistics();
        bindTodayWorkout();
        bindGoal();
        setupQuickActions();
        setupTip();
        setupMouseEvents();
        setupKeyboardShortcuts();
    }

    // ================================================================
    //  Setup
    // ================================================================

    private void showDate() {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        dateLabel.setText(LocalDate.now().format(formatter));
    }

    // Connects the four statistic cards to the model using bindings
    private void bindStatistics() {
        DailyRecord today = data.getToday();

        caloriesLabel.textProperty().bind(
            Bindings.format(NUMBERS_LOCALE, "%,d", today.caloriesProperty()));

        waterLabel.textProperty().bind(
            Bindings.format(NUMBERS_LOCALE, "%.1f L", today.waterLitersProperty()));

        workoutsLabel.textProperty().bind(
            Bindings.convert(weeklyWorkoutsBinding()));

        goalPercentLabel.textProperty().bind(
            Bindings.format(NUMBERS_LOCALE, "%.0f%%", goalProgressBinding().multiply(100)));

        // Weekly summary line under the page title (uses streams + Collectors)
        weeklySummaryLabel.textProperty().bind(
            Bindings.createStringBinding(this::buildWeeklySummary,
                data.getToday().workoutsCompletedProperty(),
                data.getToday().waterLitersProperty(),
                data.getToday().caloriesProperty()));
    }

    private void bindTodayWorkout() {
        PlannedWorkout workout = data.getTodayWorkout();

        workoutNameLabel.setText(workout.getName());
        workoutDurationLabel.setText(workout.getDurationMinutes() + " minutes");

        // Button text and state follow the "completed" property
        startButton.textProperty().bind(
            Bindings.when(workout.completedProperty())
                    .then("Completed")
                    .otherwise("Start"));

        startButton.disableProperty().bind(workout.completedProperty());
    }

    private void bindGoal() {
        goalTitleLabel.setText(data.getGoalTitle());

        goalCountLabel.textProperty().bind(
            Bindings.format(NUMBERS_LOCALE, "%d / %d", weeklyWorkoutsBinding(), data.getGoalTarget()));

        goalProgressBar.progressProperty().bind(goalProgressBinding());
    }

    private void setupQuickActions() {
        // Give every quick action tile the same look and hover effect
        quickActionsBox.getChildren().forEach(node -> {
            Button tile = (Button) node;
            tile.setStyle(TILE_STYLE);
            tile.setPrefWidth(100);
            tile.setMaxWidth(Double.MAX_VALUE);
            tile.setMaxHeight(Double.MAX_VALUE);
            HBox.setHgrow(tile, javafx.scene.layout.Priority.ALWAYS);

            tile.setOnMouseEntered(event -> tile.setStyle(TILE_HOVER_STYLE));
            tile.setOnMouseExited(event -> tile.setStyle(TILE_STYLE));
        });
    }

    private void setupTip() {
        tipLabel.setText(data.getCurrentTip());
    }

    private void setupMouseEvents() {
        // Clicking the tip card shows the next tip
        tipCard.setOnMouseClicked(event -> showNextTip());

        // Clicking the water card opens the "Log Water" window
        waterCard.setOnMouseClicked(event -> handleLogWater());
    }

    // Keyboard event: shortcuts work while the Dashboard is shown
    private void setupKeyboardShortcuts() {
        rootPane.setOnKeyPressed(event -> handleShortcut(event));

        // Give the page keyboard focus when it opens or is clicked
        javafx.application.Platform.runLater(() -> rootPane.requestFocus());
        rootPane.setOnMouseClicked(event -> rootPane.requestFocus());
    }

    // Ctrl+T = next tip, Ctrl+L = log water
    private void handleShortcut(KeyEvent event) {
        if (!event.isShortcutDown()) {
            return;
        }
        if (event.getCode() == KeyCode.T) {
            showNextTip();
            event.consume();
        } else if (event.getCode() == KeyCode.L) {
            handleLogWater();
            event.consume();
        }
    }

    // ================================================================
    //  Button handlers (linked from Dashboard.fxml with onAction)
    // ================================================================

    @FXML
    private void handleStartWorkout() {
        data.completeTodayWorkout();
        showStatus("Great job! \"" + data.getTodayWorkout().getName()
                   + "\" is marked as completed.", true);
    }

    @FXML
    private void handleBrowseWorkouts() {
        openPage("#workoutsButton", "Workouts");
    }

    @FXML
    private void handleMealPlan() {
        openPage("#nutritionButton", "Nutrition");
    }

    @FXML
    private void handleViewProgress() {
        openPage("#progressButton", "Progress");
    }

    // Opens the "Log Water" window (LogWater.fxml), same way as Log Weight
    @FXML
    private void handleLogWater() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/herspace/view/LogWater.fxml"));

            Parent root = loader.load();

            LogWaterController controller = loader.getController();

            Stage stage = new Stage();

            stage.setTitle("Log Water");
            stage.setScene(new Scene(root));

            stage.initOwner(rootPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            stage.showAndWait();

            Double liters = controller.getAmount();

            if (liters != null) {
                data.addWater(liters);
                showStatus(String.format(NUMBERS_LOCALE, "Added %.2f L of water. Keep it up!", liters), true);
            }

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setContentText("Unable to open Log Water.");

            alert.showAndWait();
        }
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private void showNextTip() {
        tipLabel.setText(data.nextTip());
    }

    // Selects a sidebar button so MainController opens that page
    private void openPage(String buttonId, String pageName) {
        Scene scene = rootPane.getScene();
        Node node = scene == null ? null : scene.lookup(buttonId);

        if (node instanceof ToggleButton) {
            ((ToggleButton) node).fire();
        } else {
            showStatus("Unable to open the " + pageName + " page.", false);
        }
    }

    // Only today's record changes while the app runs (Start button
    // and Log Water), so the bindings listen to today's values
    private IntegerBinding weeklyWorkoutsBinding() {
        return Bindings.createIntegerBinding(data::getWeeklyWorkouts,
                data.getToday().workoutsCompletedProperty());
    }

    private DoubleBinding goalProgressBinding() {
        return Bindings.createDoubleBinding(data::getGoalProgress,
                data.getToday().workoutsCompletedProperty());
    }

    private String buildWeeklySummary() {
        return String.format(NUMBERS_LOCALE,
            "This week: %d workouts  |  avg %,.0f kcal/day  |  %.1f L water  |  water goal reached %d of 7 days",
            data.getWeeklyWorkouts(),
            data.getAverageCalories(),
            data.getWeeklyWater(),
            data.getDaysWaterTargetReached());
    }

    private void showStatus(String message, boolean success) {
        statusLabel.getStyleClass().removeAll("success-message", "error-message");
        statusLabel.getStyleClass().add(success ? "success-message" : "error-message");
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

}
