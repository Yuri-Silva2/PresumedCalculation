package org.yuri.service;

import javafx.scene.control.Alert;

public class AlertService {

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setHeight(150.0);
        alert.setWidth(250.0);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
