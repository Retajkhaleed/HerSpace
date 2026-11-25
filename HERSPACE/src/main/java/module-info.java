module com.herspace {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.herspace to javafx.fxml;
    exports com.herspace;
    // Allow FXMLLoader to access application controllers
    opens com.herspace.controller to javafx.fxml;

    // Allow TableView (PropertyValueFactory) to read WeightRecord
    opens com.herspace.model to javafx.base;
}
