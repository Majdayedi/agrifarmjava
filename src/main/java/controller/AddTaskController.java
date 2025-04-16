package controller;

import entite.Task;
import entite.Field;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.TaskService;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

import static utils.AlertHelper.showAlert;

public class AddTaskController {
    private final TaskService taskService = new TaskService();

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField ressourceField;
    @FXML private TextField responsableField;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private TextField estimatedDurationField;
    @FXML private DatePicker deadlinePicker;
    @FXML private Spinner<Integer> workersSpinner;
    @FXML private TextField paymentWorkerField;
    @FXML private TextField totalField;

    private Field currentField; // The field this task belongs to
    private Task currentTask; // The task being edited (null if adding new)

    @FXML
    public void initialize() {
        // Initialize form controls
        statusComboBox.getItems().addAll("To Do", "In Progress", "Done");
        priorityComboBox.getItems().addAll("Low", "Medium", "High");

        // Set up workers spinner
        workersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Set current date as default
        datePicker.setValue(LocalDate.now());
    }

    public void setField(Field field) {
        this.currentField = field;
    }

    public void setTask(Task task) {
        this.currentTask = task;
        if (task != null) {
            // Populate form with existing task data
            nameField.setText(task.getName());
            descriptionField.setText(task.getDescription());
            statusComboBox.setValue(task.getStatus());
            datePicker.setValue(task.getDate().toLocalDate());
            ressourceField.setText(task.getRessource());
            responsableField.setText(task.getResponsable());
            priorityComboBox.setValue(task.getPriority());
            estimatedDurationField.setText(task.getEstimatedDuration());
            if (task.getDeadline() != null) {
                deadlinePicker.setValue(task.getDeadline().toLocalDate());
            }
            workersSpinner.getValueFactory().setValue(task.getWorkers());
            paymentWorkerField.setText(String.valueOf(task.getPaymentWorker()));
            totalField.setText(String.valueOf(task.getTotal()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (nameField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                    statusComboBox.getValue() == null || priorityComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all required fields.");
                return;
            }

            Task task;
            if (currentTask == null) {
                // Creating new task
                task = new Task();
                task.setField(currentField);
            } else {
                // Updating existing task
                task = currentTask;
            }

            // Set task properties from form
            task.setName(nameField.getText());
            task.setDescription(descriptionField.getText());
            task.setStatus("to do");
            task.setDate(Date.valueOf(datePicker.getValue()));
            task.setRessource(ressourceField.getText());
            task.setResponsable(responsableField.getText());
            task.setPriority(priorityComboBox.getValue());
            task.setEstimatedDuration(estimatedDurationField.getText());

            if (deadlinePicker.getValue() != null && !deadlinePicker.getValue().toString().isEmpty()) {

                if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
                    // Deadline is before today
                }

                    showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please select a valid deadline date.");

                }
                else{
                    task.setDeadline(Date.valueOf(deadlinePicker.getValue()));
                }


            task.setWorkers(workersSpinner.getValue());
            task.setPaymentWorker(Double.parseDouble(paymentWorkerField.getText()));
            task.setTotal(task.getWorkers() * task.getPaymentWorker());
            task.setLastUpdated(new Timestamp(System.currentTimeMillis()));

            // Save to database
            if (currentTask == null) {
                taskService.create(task);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully!");
            } else {
                taskService.update(task);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully!");
            }

            returnToTaskGrid();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers for numeric fields.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        returnToTaskGrid();
    }

    private void returnToTaskGrid() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskdisplay.fxml"));
            BorderPane taskGrid = loader.load();

            // Get the controller and load tasks for the current field
            TaskController taskController = loader.getController();
            taskController.LoadTasks(currentField);

            // Replace only the center content
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(taskGrid);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find main container");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load task grid: " + e.getMessage());
        }
    }

    private BorderPane getMainContainer() {
        Node current = nameField;
        while (current != null && !(current instanceof BorderPane)) {
            current = current.getParent();
        }
        return (BorderPane) current;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}