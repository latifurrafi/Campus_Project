package com.pharmacy.org.pharmacy.Controllers;

import DataBase.DatabaseConnection;
import com.pharmacy.org.pharmacy.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {
    @SuppressWarnings("exports")
    @FXML
    public TextField userNameField;
    @SuppressWarnings("exports")
    @FXML
    public PasswordField passwordField;
    @SuppressWarnings("exports")
    @FXML
    public PasswordField passwordFieldAgain;
    @SuppressWarnings("exports")
    @FXML
    public Label errorLabel;
    @SuppressWarnings("exports")
    @FXML
    public Label signUpMessage;

    @FXML
    public void toLoginPage(@SuppressWarnings("exports") ActionEvent actionEvent) {
        SceneManager.loadScene("main-view.fxml", "Login");
    }

    @FXML
    public void signUp(@SuppressWarnings("exports") ActionEvent actionEvent) {
        String userName = userNameField.getText().trim().toLowerCase(); // Trim to avoid whitespace issues
        String password = passwordField.getText().trim().toLowerCase();
        String passAgain = passwordFieldAgain.getText().trim();

        // Check for empty fields
        if (userName.isEmpty()) {
            errorLabel.setText("Username cannot be empty");
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Password cannot be empty");
            return;
        }
        if (passAgain.isEmpty()) {
            errorLabel.setText("Please confirm your password");
            return;
        }

        // Check if passwords match
        if (!password.equals(passAgain)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        // Clear error label
        errorLabel.setText("");

        // Attempt to insert the user into the database
        String result = DatabaseConnection.insertUser(userName, password);

        // Display result message
        signUpMessage.setText(result);
    }


}
