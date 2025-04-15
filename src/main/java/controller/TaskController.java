package controller;

import entite.Field;
import entite.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.TaskService;

import java.io.IOException;
import java.util.List;

public class TaskController {

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTaskBtn;

    private final TaskService taskService = new TaskService();
    private Field currentField;

    @FXML
    public void initialize() {
        styleColumns();

        if (addTaskBtn != null) {
            addTaskBtn.setOnAction(event -> handleAddTask());
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

        // Set button actions
        if (deleteBtn != null) deleteBtn.setOnAction(e -> handleDeleteTask(task, taskCard));
        if (editBtn != null) editBtn.setOnAction(e -> handleEditTask(task));

        return taskCard;
    }

    private void applyStatusStyle(Label statusLabel, String status) {
        if (status == null || statusLabel == null) return;

        String normalizedStatus = status.trim().toLowerCase();
        String style = "";

        if (normalizedStatus.contains("todo") || normalizedStatus.contains("to do")) {
            style = "-fx-text-fill: #d9534f; -fx-font-weight: bold;"; // Red
        } else if (normalizedStatus.contains("progres") || normalizedStatus.contains("progress")) {
            style = "-fx-text-fill: #f0ad4e; -fx-font-weight: bold;"; // Orange
        } else if (normalizedStatus.contains("done")) {
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

        String status = task.getStatus().trim().toLowerCase();

        if (status.contains("todo") || status.contains("to do")) {
            todoColumn.getChildren().add(taskCard);
        } else if (status.contains("progres") || status.contains("progress")) {
            inProgressColumn.getChildren().add(taskCard);
        } else if (status.contains("done")) {
            doneColumn.getChildren().add(taskCard);
        } else {
            System.out.println("Unknown task status: " + task.getStatus());
            todoColumn.getChildren().add(taskCard);
        }
    }

    private void removeTaskFromAllColumns(Pane taskCard) {
        todoColumn.getChildren().remove(taskCard);
        inProgressColumn.getChildren().remove(taskCard);
        doneColumn.getChildren().remove(taskCard);
    }

    private void clearTaskColumns() {
        if (todoColumn.getChildren().size() > 1) {
            todoColumn.getChildren().remove(1, todoColumn.getChildren().size());
        }
        if (inProgressColumn.getChildren().size() > 1) {
            inProgressColumn.getChildren().remove(1, inProgressColumn.getChildren().size());
        }
        if (doneColumn.getChildren().size() > 1) {
            doneColumn.getChildren().remove(1, doneColumn.getChildren().size());
        }
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

        setupColumnDropHandler(todoColumn, "To-Do", task);
        setupColumnDropHandler(inProgressColumn, "In Progress", task);
        setupColumnDropHandler(doneColumn, "Done", task);
    }

    private void setupColumnDragOver(VBox column) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    private void setupColumnDropHandler(VBox column, String newStatus, Task task) {
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                task.setStatus(newStatus);
                if (taskService.update(task)) {
                    LoadTasks(currentField);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTask.fxml"));
            Parent addTaskPane = loader.load();

            // Get current stage and switch scene
            Stage stage = (Stage) addTaskBtn.getScene().getWindow();
            stage.getScene().setRoot(addTaskPane);
        } catch (IOException e) {
            System.err.println("Error loading AddTask.fxml:");
            e.printStackTrace();
        }
    }

    private void handleDeleteTask(Task task, Pane taskCard) {
        taskService.delete(task);
            removeTaskFromAllColumns(taskCard);
            System.out.println("Task deleted: " + task.getName());
            System.err.println("Failed to delete task: " + task.getName());

    }

    private void handleEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditTask.fxml"));
            Parent editTaskPane = loader.load();

            // Get current stage and switch scene
            Stage stage = (Stage) addTaskBtn.getScene().getWindow();
            stage.getScene().setRoot(editTaskPane);
        } catch (IOException e) {
            System.err.println("Error loading EditTask.fxml:");
            e.printStackTrace();
        }
    }
}