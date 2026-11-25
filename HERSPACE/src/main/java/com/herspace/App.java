
package com.herspace;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main entry point of the HERSPACE application
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Load the main application layout
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("view/Main.fxml")
        );

        Parent root = loader.load();

        // Create the main application scene
        Scene scene = new Scene(root, 1100, 700);

        // Apply the shared application stylesheet
        scene.getStylesheets().add(
            App.class.getResource("css/style.css").toExternalForm()
        );

        // Set the application window title
        stage.setTitle("HERSPACE");

        // Set the minimum window dimensions
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Display the main application window
        stage.setScene(scene);
        stage.show();
    }

    // Launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
