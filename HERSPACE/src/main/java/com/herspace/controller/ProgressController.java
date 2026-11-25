package com.herspace.controller;

import com.herspace.model.WeightRecord;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressController {

@FXML
private TableView<WeightRecord> weightTable;

@FXML
private TableColumn<WeightRecord, LocalDate> dateColumn;

@FXML
private TableColumn<WeightRecord, Double> weightColumn;

@FXML
private Label currentWeightLabel;

@FXML
private Label changeLabel;

@FXML
private Button addWeightButton;

private static final ObservableList<WeightRecord> records =
        FXCollections.observableArrayList();

@FXML
public void initialize() {

    dateColumn.setCellValueFactory(
            new PropertyValueFactory<>("date"));

    weightColumn.setCellValueFactory(
            new PropertyValueFactory<>("weight"));

    weightTable.setItems(records);

    weightTable.setPlaceholder(
            new Label("No weight records yet."));

    // Action Event
    addWeightButton.setOnAction(e -> openLogWeight());

    // Keyboard Event
    weightTable.setOnKeyPressed(e -> {

        if (e.getCode() == KeyCode.ENTER) {
            showRecord();
        }
    });

    updateSummary();
}

private void openLogWeight() {

    try {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/LogWeight.fxml"));

        Parent root = loader.load();

        LogWeightController controller =
                loader.getController();

        controller.setRecords(records);

        Stage stage = new Stage();

        stage.setTitle("Log Weight");
        stage.setScene(new Scene(root));

        stage.initOwner(
                addWeightButton.getScene().getWindow());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);

        stage.showAndWait();

        WeightRecord record = controller.getNewRecord();

        if (record != null) {

            records.add(record);

            // Sort records by date
            List<WeightRecord> sortedRecords = records.stream()
                    .sorted((a, b) ->
                            a.getDate().compareTo(b.getDate()))
                    .collect(Collectors.toList());

            records.setAll(sortedRecords);

            updateSummary();
        }

    } catch (IOException e) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setContentText("Unable to open Log Weight.");

        alert.showAndWait();
    }
}

private void updateSummary() {

    // Stream: filter and map
    List<Double> weights = records.stream()
            .filter(r -> r.getWeight() > 0)
            .map(WeightRecord::getWeight)
            .collect(Collectors.toList());

    // Statistical calculation
    long totalRecords = weights.stream()
            .collect(Collectors.counting());

    if (totalRecords == 0) {

        currentWeightLabel.setText("-- kg");
        changeLabel.setText("-- kg");

        return;
    }

    double firstWeight = weights.get(0);

    double currentWeight =
            weights.get(weights.size() - 1);

    double change = currentWeight - firstWeight;

    currentWeightLabel.setText(
            String.format("%.1f kg", currentWeight));

    changeLabel.setText(
            String.format("%+.1f kg", change));

    if (change < 0) {

        changeLabel.setStyle(
                "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #789273;");

    } else {

        changeLabel.setStyle(
                "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #92576D;");
    }
}

private void showRecord() {
    WeightRecord selected = weightTable
            .getSelectionModel().getSelectedItem();

    if (selected != null) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle("Weight Record");
        alert.setHeaderText(null);

        alert.setContentText(
                "Date: " + selected.getDate()
                + "\nWeight: "
                + selected.getWeight() + " kg");

        alert.showAndWait();
    }
}
}
