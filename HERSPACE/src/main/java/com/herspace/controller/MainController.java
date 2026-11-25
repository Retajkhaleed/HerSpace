
package com.herspace.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MainController {

    // Navigation buttons defined in Main.fxml
    @FXML
    private ToggleButton dashboardButton;

    @FXML
    private ToggleButton workoutsButton;

    @FXML
    private ToggleButton nutritionButton;

    @FXML
    private ToggleButton progressButton;

    @FXML
    private ToggleButton goalsButton;

    // Container used to display application pages
    @FXML
    private StackPane contentArea;

    // Ensures that only one navigation button is selected
    private final ToggleGroup navigationGroup = new ToggleGroup();

    // Called automatically after loading Main.fxml
    @FXML
    private void initialize() {

        // Store all navigation buttons in an array
        ToggleButton[] buttons = {
            dashboardButton,
            workoutsButton,
            nutritionButton,
            progressButton,
            goalsButton
        };

        // Configure each navigation button
        for (ToggleButton button : buttons) {

            // Add the button to the shared toggle group
            button.setToggleGroup(navigationGroup);

            // Update the icon whenever the selection changes
            button.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateIconColor(button);
                }
            );

            // Apply the initial icon color
            updateIconColor(button);
        }

        // Prevent all navigation buttons from being deselected
        navigationGroup.selectedToggleProperty().addListener(
            (observable, oldToggle, newToggle) -> {

                if (newToggle == null && oldToggle != null) {
                    oldToggle.setSelected(true);
                }
            }
        );

        // Configure navigation actions using lambda expressions
        dashboardButton.setOnAction(event -> loadPage("Dashboard"));

        workoutsButton.setOnAction(event -> loadPage("Workouts"));

        nutritionButton.setOnAction(event -> loadPage("Nutrition"));

        progressButton.setOnAction(event -> loadPage("Progress"));

        goalsButton.setOnAction(event -> loadPage("Goals"));

        // Select Dashboard when the application starts
        dashboardButton.setSelected(true);

        // Display Dashboard as the default page
        loadPage("Dashboard");
    }

    // Change the icon color according to the button state
    private void updateIconColor(ToggleButton button) {

        ImageView icon = (ImageView) button.getGraphic();

        if (icon == null) {
            return;
        }

        if (button.isSelected()) {

            // Convert the selected icon to white
            ColorAdjust whiteEffect = new ColorAdjust();
            whiteEffect.setBrightness(1.0);

            icon.setEffect(whiteEffect);

        } else {

            // Restore the original icon color
            icon.setEffect(null);
        }
    }

    // Load the requested FXML page into the main content area
    private void loadPage(String pageName) {

        try {

            // Locate the requested FXML file
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/herspace/view/" + pageName + ".fxml"
                )
            );

            // Load the page layout
            Parent page = loader.load();

            // Replace the current content with the requested page
            contentArea.getChildren().setAll(page);

        } catch (IOException | IllegalStateException e) {

            // Display an error message if the page cannot be loaded
            Label errorLabel = new Label(
                "Unable to load " + pageName + " page."
            );

            errorLabel.getStyleClass().add("error-message");

            contentArea.getChildren().setAll(errorLabel);

            // Print error details for debugging
            e.printStackTrace();
        }
    }
}
