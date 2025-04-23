package controller;

import entite.Farm;
import entite.Field;
import entite.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.TaskService;

import java.io.IOException;
import java.util.List;


public class TaskController {

    public ScrollPane mainContainer;
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTaskBtn;
    @FXML private Button deleteBtn;
    @FXML private Button backButton;
    private final TaskService taskService = new TaskService();
    private Field currentField;
    private Farm currentFarm;

    @FXML
    public void initialize() {
        styleColumns();

        if (addTaskBtn != null) {
            addTaskBtn.setOnAction(event -> handleAddTask(currentField));
        } if (backButton != null) {
            backButton.setOnAction(event -> handleBack(currentFarm));
        }
    }

    private void styleColumns() {
        todoColumn.setStyle("-fx-background-color: #ffeeee; -fx-padding: 10; -fx-border-color: #ffdddd;");
        inProgressColumn.setStyle("-fx-background-color: #ffffe5; -fx-padding: 10; -fx-border-color: #eeeecc;");
        doneColumn.setStyle("-fx-background-color: #eeffee; -fx-padding: 10; -fx-border-color: #ddffdd;");
    }

    public void LoadTasks(Field field) {
        this.currentField = field;
        clearTaskColumns();

        List<Task> tasks = taskService.getTasksByField(field.getId());
        System.out.println("Loading " + tasks.size() + " tasks for field: " + field.getName());

        for (Task task : tasks) {
            try {
                Pane taskCard = createTaskCard(task);
                setupDragAndDrop(taskCard, task);
                addTaskToColumn(task, taskCard);
            } catch (IOException e) {
                System.err.println("Failed to create task card for: " + task.getName());
                e.printStackTrace();
            }
        }
    }

    private Pane createTaskCard(Task task) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TaskCard.fxml"));
        Pane taskCard = loader.load();

        // Set task data
        Label taskNameLabel = (Label) taskCard.lookup("#taskName");
        Label taskDeadlineLabel = (Label) taskCard.lookup("#taskDeadline");
        Label taskStatusLabel = (Label) taskCard.lookup("#taskStatus");
        Button deleteBtn = (Button) taskCard.lookup("#deleteBtn");
        Button editBtn = (Button) taskCard.lookup("#editBtn");

        if (taskNameLabel != null) taskNameLabel.setText(task.getName());
        if (taskDeadlineLabel != null) {
            taskDeadlineLabel.setText(task.getDeadline() != null ?
                    task.getDeadline().toString() : "No deadline");
        }
        if (taskStatusLabel != null) {
            taskStatusLabel.setText(task.getStatus());
            applyStatusStyle(taskStatusLabel, task.getStatus());
        }
        Label durationLabel = (Label) taskCard.lookup("#durationLabel");
        if (durationLabel != null && task.getDate() != null) {
            durationLabel.setText(task.getDate() + " days");
        }
        Label paymentworker=(Label) taskCard.lookup("#paymentworker");
        if (paymentworker != null && task.getPaymentWorker() != 0) {
            paymentworker.setText(task.getTotal() + " DT");
        }

        // Set button actions
        if (deleteBtn != null) deleteBtn.setOnAction(e -> handleDeleteTask(task, taskCard));
        if (editBtn != null) editBtn.setOnAction(e -> handleEditTask(task, taskCard));

        return taskCard;
    }

    private void applyStatusStyle(Label statusLabel, String status) {
        if (status == null || statusLabel == null) return;

        String normalizedStatus = status.trim().toUpperCase();
        String style = "";

        if (normalizedStatus.equals("TODO")) {
            style = "-fx-text-fill: #d9534f; -fx-font-weight: bold;"; // Red
        } else if (normalizedStatus.equals("INPROG")) {
            style = "-fx-text-fill: #f0ad4e; -fx-font-weight: bold;"; // Orange
        } else if (normalizedStatus.equals("DONE")) {
            style = "-fx-text-fill: #5cb85c; -fx-font-weight: bold;"; // Green
        } else {
            style = "-fx-text-fill: #777777;"; // Gray
            System.out.println("Unknown task status: " + status);
        }
        statusLabel.setStyle(style);
    }

    private void addTaskToColumn(Task task, Pane taskCard) {
        removeTaskFromAllColumns(taskCard);

        if (task.getStatus() == null) {
            todoColumn.getChildren().add(taskCard);
            return;
        }

        String status = task.getStatus().trim().toUpperCase();

        if (status.equals("TODO")) {
            todoColumn.getChildren().add(taskCard);
        } else if (status.equals("INPROG")) {
            inProgressColumn.getChildren().add(taskCard);
        } else if (status.equals("DONE")) {
            doneColumn.getChildren().add(taskCard);
        } else {
            System.out.println("Unknown task status: " + task.getStatus());
            todoColumn.getChildren().add(taskCard);
        }
    }

    private void removeTaskFromAllColumns(Node taskCard) {
        todoColumn.getChildren().remove(taskCard);
        inProgressColumn.getChildren().remove(taskCard);
        doneColumn.getChildren().remove(taskCard);
    }

    private void clearTaskColumns() {
        todoColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();
    }

    private void setupDragAndDrop(Pane taskCard, Task task) {
        taskCard.setOnDragDetected(event -> {
            Dragboard db = taskCard.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId()));
            db.setContent(content);
            event.consume();
        });

        setupColumnDragOver(todoColumn);
        setupColumnDragOver(inProgressColumn);
        setupColumnDragOver(doneColumn);

        setupColumnDropHandler(todoColumn, "TODO");
        setupColumnDropHandler(inProgressColumn, "INPROG");
        setupColumnDropHandler(doneColumn, "DONE");
    }

    private void setupColumnDragOver(VBox column) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    private void setupColumnDropHandler(VBox column, String newStatus) {
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                try {
                    int taskId = Integer.parseInt(db.getString());
                    Task task = taskService.getTaskById(taskId);
                    if (task != null) {
                        // Update the task status
                        task.setStatus(newStatus);
                        if (taskService.updateStatus(task, newStatus)) {
                            // Remove the task from all columns first
                            Node draggedNode = (Node) event.getGestureSource();
                            removeTaskFromAllColumns(draggedNode);
                            // Add the task to the new column
                            column.getChildren().add(draggedNode);
                            success = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid task ID in dragboard: " + db.getString());
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

   // Add this if you want to reference the button in code

    @FXML
    private void handleAddTask(Field field) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addtask.fxml"));
            Parent root = loader.load();

            AddTaskController controller = loader.getController();
            controller.setField(field);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add New Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh task list after closing the add task window
            LoadTasks(currentField);

        } catch (IOException e) {
            showAlert("Error", "Failed to load task form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private void handleDeleteTask(Task task, Pane taskCard) {
        taskService.delete(task);
        removeTaskFromAllColumns(taskCard);
        System.out.println("Task deleted: " + task.getName());
        System.err.println("Failed to delete task: " + task.getName());

    }

    private void handleEditTask(Task task, Pane taskCard) {
            

        removeTaskFromAllColumns(taskCard);
        LoadTasks(currentField);


    }

    @FXML
    private void handleBack(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fielddisplay.fxml"));
            Pane fieldDisplay = loader.load();

            // Get the FieldController and pass the farm
            FieldController fieldController = loader.getController();
            if (fieldController != null) {
                fieldController.loadField(farm);
            }

            // Replace main content
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(fieldDisplay);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(title);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    private BorderPane getMainContainer() {
        Node current = mainContainer;
        while (current != null && !(current instanceof BorderPane)) {
            current = current.getParent();
        }
        return (BorderPane) current;
    }



    public void setCurrentFarm(Farm farm) {
        this.currentFarm = farm;
    }
}
