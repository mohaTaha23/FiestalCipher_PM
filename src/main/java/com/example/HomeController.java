package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class HomeController {

    @FXML
    private ListView<String> passwordListView;

    private PasswordManagerAuth passwordManagerAuth;
    private String username;

    public void initialize(PasswordManagerAuth passwordManagerAuth, String username) {
        this.passwordManagerAuth = passwordManagerAuth;
        this.username = username;
        // Load passwords for the user
        loadPasswords();
    }

    private void loadPasswords() {
        // Load passwords from the user's storage file and display them in the ListView
        passwordListView.getItems().clear();
        passwordListView.getItems().addAll(passwordManagerAuth.getPasswords(username));
    }

    @FXML
    private void handleAddPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Password");
        dialog.setHeaderText("Add a new password");
        dialog.setContentText("Enter account and password (format: account,password):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                String account = parts[0];
                String password = parts[1];
                passwordManagerAuth.addPassword(username, account, password);
                loadPasswords();
            } else {
                showAlert("Invalid format", "Please enter the account and password in the format: account,password");
            }
        });
    }

    @FXML
    private void handleUpdatePassword() {
        String selectedItem = passwordListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No selection", "Please select a password to update.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Update Password");
        dialog.setHeaderText("Update the password");
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            String account = selectedItem.split(":")[0];
            passwordManagerAuth.updatePassword(username, account, newPassword);
            loadPasswords();
        });
    }

    @FXML
    private void handleDeletePassword() {
        String selectedItem = passwordListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No selection", "Please select a password to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Password");
        alert.setHeaderText("Delete the password");
        alert.setContentText("Are you sure you want to delete this password?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String account = selectedItem.split(":")[0];
            passwordManagerAuth.deletePassword(username, account);
            loadPasswords();
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) passwordListView.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}