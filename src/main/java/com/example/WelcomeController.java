package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class WelcomeController {

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        // Handle button clicks
        registerButton.setOnAction(event -> handleRegister());
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("register.fxml"));
            Parent root = fxmlLoader.load();
            Stage primaryStage = App.getPrimaryStage();
            Scene scene = new Scene(root, 640, 480);
            scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
            Parent root = fxmlLoader.load();
            Stage primaryStage = App.getPrimaryStage();
            primaryStage.setScene(new Scene(root, 640, 480));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}