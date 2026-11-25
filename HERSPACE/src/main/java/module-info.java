module com.herspace {

requires javafx.controls;
requires javafx.fxml;
    requires java.base;

opens com.herspace.controller to javafx.fxml;
opens com.herspace.model to javafx.base;

exports com.herspace;
}