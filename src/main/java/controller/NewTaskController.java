package controller;

import entite.Field;
import entite.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import service.TaskService;

public class NewTaskController {

    private final TaskService taskService = new TaskService();
    private Field currentField;

    @FXML
    private TextField taskNameField;

    @FXML
    private TextField taskDeadlineField;

    @FXML
    private Button saveTaskBtn;

    @FXML
    public void initialize() {
        saveTaskBtn.setOnAction(event -> handleSaveTask());
    }

    public void setField(Field field) {
        currentField = field;
    }

    private void handleSaveTask() {
       
    }

    private void returnToMainView() {
        // Navigate back to the main view
        taskNameField.getScene().getRoot().lookup("#mainContainer").setVisible(true);
    }

    private void showError(String title, String message) {
        // Display an error dialog
    }

    private void showSuccess(String title, String message) {
        // Display a success dialog
    }
}