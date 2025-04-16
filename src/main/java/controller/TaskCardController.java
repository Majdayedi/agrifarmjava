package controller;

import entite.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class TaskCardController {

    @FXML
    private Label taskNameLabel;

    @FXML
    private Label taskDeadlineLabel;

    @FXML
    private Button backBtn;

    private Task task;

    @FXML
    public void initialize() {
        backBtn.setOnAction(event -> returnToMainView());
    }

    public void setTask(Task task) {
        this.task = task;
        loadTaskDetails();
    }

    private void loadTaskDetails() {
        taskNameLabel.setText(task.getName());
        taskDeadlineLabel.setText(task.getDeadline().toString());
    }

    private void returnToMainView() {
        // Navigate back to the main view
        taskNameLabel.getScene().getRoot().lookup("#mainContainer").setVisible(true);
    }
}