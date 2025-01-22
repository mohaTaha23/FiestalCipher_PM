package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ProgressBar passwordStrengthBar;

    private PasswordManagerAuth passwordManagerAuth;

    public RegisterController() {
        passwordManagerAuth = new PasswordManagerAuth();
    }

    @FXML
    private void initialize() {
        // Add listener to password field to check strength on each text change
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength();
        });
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            passwordManagerAuth.register(username, password);
            System.out.println("User registered successfully.");
            // Switch to the home page
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent root = fxmlLoader.load();
            HomeController homeController = fxmlLoader.getController();

            homeController.initialize(passwordManagerAuth, username);

            Stage primaryStage = App.getPrimaryStage();

            Scene scene = new Scene(root, 640, 480);
            scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updatePasswordStrength() {
        String password = passwordField.getText();
        double strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);
    }

    private double calculatePasswordStrength(String password) {
        int length = password.length();
        int strength = 0;
        if (length >= 8)
            strength++;
        if (password.matches(".*[A-Z].*"))
            strength++;
        if (password.matches(".*[a-z].*"))
            strength++;
        if (password.matches(".*[0-9].*"))
            strength++;
        if (password.matches(".*[!@#$%^&*].*"))
            strength++;
        return (double) strength / 5;
    }
}