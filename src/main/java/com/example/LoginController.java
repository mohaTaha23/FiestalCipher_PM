package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private PasswordManagerAuth passwordManagerAuth;

    public LoginController() {
        passwordManagerAuth = new PasswordManagerAuth();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            boolean loginSuccess = passwordManagerAuth.login(username, password);
            if (loginSuccess) {
                System.out.println("Login successful.");
                // Switch to the home page
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("home.fxml"));
                Parent root = fxmlLoader.load();
                HomeController homeController = fxmlLoader.getController();

                homeController.initialize(passwordManagerAuth, username);

                Stage primaryStage = App.getPrimaryStage();
                Scene scene = new Scene(root, 640, 480);
                scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                System.out.println("Login failed. Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}