package controller;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;

import java.util.Optional;
import entite.Farm;
import entite.Field;
import entite.Task;
import javafx.scene.image.ImageView;
import service.GeminiTest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.FarmService;
import service.TaskService;

import java.io.IOException;
import java.util.List;
import java.time.LocalDate;
import service.WeatherService;
import service.WeatherService.Weather;
import javafx.scene.image.Image;

public class TaskController {

    public ScrollPane mainContainer;
    public Button birButton;
    public Button cabinButton;
    public Button irrigationButton;
    public Button fenceButton;
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTaskBtn;
    @FXML private Button deleteBtn;
    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private DatePicker deadlinePicker;
    @FXML private Button filterButton;
    @FXML private VBox photovoltaicButtonContainer;
    @FXML private Button photovoltaicButton;
    private final TaskService taskService = new TaskService();
    private final FarmService farmService = new FarmService();
    private GeminiTest geminiTest = new GeminiTest();
    private Field currentField;
    private Farm currentFarm;
    @FXML private Button generateTasksBtn;
    private WeatherService.Weather weather;
    @FXML private Label weatherTemp;
    @FXML private Label weatherDesc;
    @FXML private ImageView weatherIcon;

    public void setFirst(WeatherService.Weather first) {
        this.weather= first;
    }
    @FXML
    public void initialize() {
        styleColumns();

        if (addTaskBtn != null) {
            addTaskBtn.setOnAction(event -> handleAddTask(currentField));
        }
        if (backButton != null) {
            backButton.setOnAction(event -> handleBack(currentFarm));
        }
        if (generateTasksBtn != null) {
            generateTasksBtn.setOnAction(event -> {
                if (currentField != null) {
                    showAIPromptDialog();
                } else {
                    showAlert("Error", "No field selected.");
                }
            });
        } else {
            System.err.println("generateTasksBtn is null!");
        }
        setupSearch();
        setupFilters();
    }

    private void styleColumns() {
        todoColumn.setStyle("-fx-background-color: #ffeeee; -fx-padding: 10; -fx-border-color: #ffdddd;");
        inProgressColumn.setStyle("-fx-background-color: #ffffe5; -fx-padding: 10; -fx-border-color: #eeeecc;");
        doneColumn.setStyle("-fx-background-color: #eeffee; -fx-padding: 10; -fx-border-color: #ddffdd;");
    }

    public void LoadTasks(Field field) {
        this.currentField = field;
        this.currentFarm = farmService.getFarmByFieldId(field.getId());
        clearTaskColumns();

        // Check if this is the main field and has photovoltaic enabled
        if (currentField.getName().equals("Main field") && currentFarm.isPhotovoltaic()) {
            System.out.println("yes yes yes");
            photovoltaicButton.setVisible(true);
            photovoltaicButton.setOnAction(event -> handleAddPhotovoltaicTask(currentField));
        } else {
            photovoltaicButton.setVisible(false);
        }
        if (currentField.getName().equals("Main field") && !currentFarm.isBir()) {
            System.out.println("yes yes yes");
            birButton.setVisible(true);
            birButton.setOnAction(event -> handleAddbirTask(currentField));
        } else {
            birButton.setVisible(false);
        }
        if (currentField.getName().equals("Main field") && !currentFarm.isCabin()) {
            System.out.println("yes yes yes");
            cabinButton.setVisible(true);
            cabinButton.setOnAction(event -> handleAddcabinTask(currentField));
        } else {
            cabinButton.setVisible(false);
        }
        if (currentField.getName().equals("Main field") && !currentFarm.isFence()) {
            System.out.println("yes yes yes");
            fenceButton.setVisible(true);
            fenceButton.setOnAction(event -> handleAddfenceTask(currentField));
        } else {
            fenceButton.setVisible(false);
        }
        if (currentField.getName().equals("Main field") && !currentFarm.isIrrigation()) {
            System.out.println("yes yes yes");
            irrigationButton.setVisible(true);
            irrigationButton.setOnAction(event -> handleAddirrigationTask(currentField));
        } else {
            irrigationButton.setVisible(false);
        }

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

    private void handleAddirrigationTask(Field currentField) {
    }

    private void handleAddfenceTask(Field currentField) {
    }

    private void handleAddcabinTask(Field currentField) {
        try {
            // Get current date
            java.util.Date currentDate = new java.util.Date();
            
            // Create foundation task (Day 1)
            Task foundationTask = new Task(
                currentField,
                "Foundation",
                "Prepare and pour concrete foundation",
                "to do",
                new java.sql.Date(currentDate.getTime()),
                "Concrete",
                "Construction Team",
                "High",
                "1 day",
                new java.sql.Date(currentDate.getTime() + (1 * 24 * 60 * 60 * 1000)),
                4,
                new java.sql.Timestamp(System.currentTimeMillis()),
                150.0,
                600.0
            );
            taskService.create(foundationTask);

            // Create framing task (Day 3)
            Task framingTask = new Task(
                currentField,
                "Framing",
                "Construct wooden frame structure",
                "to do",
                new java.sql.Date(currentDate.getTime() + (2 * 24 * 60 * 60 * 1000)),
                "Wood",
                "Carpentry Team",
                "High",
                "2 days",
                new java.sql.Date(currentDate.getTime() + (3 * 24 * 60 * 60 * 1000)),
                3,
                new java.sql.Timestamp(System.currentTimeMillis()),
                120.0,
                360.0
            );
            taskService.create(framingTask);

            // Create roofing task (Day 5)
            Task roofingTask = new Task(
                currentField,
                "Roofing",
                "Install roof structure and shingles",
                "to do",
                new java.sql.Date(currentDate.getTime() + (4 * 24 * 60 * 60 * 1000)),
                "Roofing Materials",
                "Roofing Team",
                "High",
                "2 days",
                new java.sql.Date(currentDate.getTime() + (5 * 24 * 60 * 60 * 1000)),
                3,
                new java.sql.Timestamp(System.currentTimeMillis()),
                130.0,
                390.0
            );
            taskService.create(roofingTask);

            // Create interior task (Day 7)
            Task interiorTask = new Task(
                currentField,
                "Interior",
                "Install insulation and fixtures",
                "to do",
                new java.sql.Date(currentDate.getTime() + (6 * 24 * 60 * 60 * 1000)),
                "Interior Materials",
                "Interior Team",
                "Medium",
                "2 days",
                new java.sql.Date(currentDate.getTime() + (7 * 24 * 60 * 60 * 1000)),
                2,
                new java.sql.Timestamp(System.currentTimeMillis()),
                110.0,
                220.0
            );
            taskService.create(interiorTask);

            // Create final inspection task (Day 9)
            Task inspectionTask = new Task(
                currentField,
                "Inspection",
                "Final safety inspection",
                "to do",
                new java.sql.Date(currentDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                "Inspection Tools",
                "Safety Inspector",
                "High",
                "1 day",
                new java.sql.Date(currentDate.getTime() + (9 * 24 * 60 * 60 * 1000)),
                1,
                new java.sql.Timestamp(System.currentTimeMillis()),
                200.0,
                200.0
            );
            taskService.create(inspectionTask);

            // Refresh the task list
            LoadTasks(currentField);
            
            showAlert("Success", "Cabin construction tasks have been created successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to create cabin tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddbirTask(Field currentField) {
    }

    private Pane createTaskCard(Task task) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TaskCard.fxml"));
        Pane taskCard = loader.load();

        VBox cardRoot = (VBox) taskCard.lookup("#cardRoot");
        if (cardRoot != null) {
            cardRoot.getStyleClass().add("task-card");
            if (task.getPriority() != null) {
                String priority = task.getPriority().trim().toLowerCase();
                if (priority.equals("high")) cardRoot.getStyleClass().add("priority-high");
                else if (priority.equals("medium")) cardRoot.getStyleClass().add("priority-medium");
                else if (priority.equals("low")) cardRoot.getStyleClass().add("priority-low");
            }
        }

        // Set task data
        Label taskNameLabel = (Label) taskCard.lookup("#taskName");
        Label taskDeadlineLabel = (Label) taskCard.lookup("#deadlineLabel");
        Label taskStatusLabel = (Label) taskCard.lookup("#taskStatus");
        Label responsibleLabel = (Label) taskCard.lookup("#responsibleLabel");
        Label workersLabel = (Label) taskCard.lookup("#workers");
        Label paymentworker = (Label) taskCard.lookup("#paymentworker");
        Label taskDateLabel = (Label) taskCard.lookup("#taskDateLabel");
        Label durationLabel = (Label) taskCard.lookup("#durationLabel");

        if (taskNameLabel != null) taskNameLabel.setText(task.getName() != null ? task.getName() : "-");
        if (taskStatusLabel != null) {
            taskStatusLabel.setText(task.getStatus() != null ? task.getStatus() : "-");
            applyStatusStyle(taskStatusLabel, task.getStatus());
        }
        if (responsibleLabel != null) responsibleLabel.setText(task.getResponsable() != null ? task.getResponsable() : "-");
        if (workersLabel != null) workersLabel.setText(String.format("%d", task.getWorkers()));
        if (paymentworker != null) paymentworker.setText(String.format("%.2f", task.getPaymentWorker()));
        if (taskDateLabel != null) taskDateLabel.setText(task.getDate() != null ? task.getDate().toString() : "-");
        if (durationLabel != null) durationLabel.setText(task.getEstimatedDuration() != null ? task.getEstimatedDuration() : "-");
        if (taskDeadlineLabel != null) {
            if (task.getDeadline() != null) {
                taskDeadlineLabel.setText(task.getDeadline().toString());
            } else {
                taskDeadlineLabel.setText("-");
            }
        }

        Button deleteBtn = (Button) taskCard.lookup("#deleteBtn");
        Button editBtn = (Button) taskCard.lookup("#editBtn");

        if (deleteBtn != null) deleteBtn.setOnAction(e -> handleDeleteTask(task, taskCard));
        if (editBtn != null) editBtn.setOnAction(e -> handleEditTask(task, taskCard));

        return taskCard;
    }

    private void applyStatusStyle(Label statusLabel, String status) {
        if (status == null || statusLabel == null) return;

        String normalizedStatus = status.trim();
        String style = "";

        if (normalizedStatus.equalsIgnoreCase("Low")) {
            style = "-fx-text-fill: #d9534f; -fx-font-weight: bold;"; // Red
        } else if (normalizedStatus.equalsIgnoreCase("Medium")) {
            style = "-fx-text-fill: #f0ad4e; -fx-font-weight: bold;"; // Orange
        } else if (normalizedStatus.equalsIgnoreCase("High")) {
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

        String status = task.getStatus().trim();

        if (status.equalsIgnoreCase("to do")) {
            todoColumn.getChildren().add(taskCard);
        } else if (status.equalsIgnoreCase("In progres")) {
            inProgressColumn.getChildren().add(taskCard);
        } else if (status.equalsIgnoreCase("done")) {
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

        setupColumnDropHandler(todoColumn, "to do");
        setupColumnDropHandler(inProgressColumn, "In progres");
        setupColumnDropHandler(doneColumn, "done");
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

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    if (currentField != null) LoadTasks(currentField);
                } else {
                    searchTasks(newValue);
                }
            });
        }
        if (searchButton != null) {
            searchButton.setOnAction(e -> searchTasks(searchField.getText()));
        }
    }

    private void searchTasks(String searchText) {
        clearTaskColumns();
        if (currentField == null) return;
        List<Task> tasks = taskService.getTasksByField(currentField.getId());
        for (Task task : tasks) {
            if (matchesSearch(task, searchText)) {
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
    }

    private boolean matchesSearch(Task task, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;
        String searchLower = searchText.toLowerCase();
        return task.getName().toLowerCase().contains(searchLower)
            || (task.getStatus() != null && task.getStatus().toLowerCase().contains(searchLower))
            || (task.getDeadline() != null && task.getDeadline().toString().contains(searchText))
            || (task.getDate() != null && String.valueOf(task.getDate()).contains(searchText));
    }

    @FXML
    private void handleSearch() {
        searchTasks(searchField.getText());
    }

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

    private void setupFilters() {
        if (priorityCombo != null) {
            priorityCombo.getItems().clear();
            priorityCombo.getItems().addAll("All", "Low", "Medium", "High");
            priorityCombo.getSelectionModel().selectFirst();
        }
        if (filterButton != null) {
            filterButton.setOnAction(e -> applyFilters());
        }
    }

    private void applyFilters() {
        String priority = priorityCombo != null ? priorityCombo.getValue() : null;
        if (priority == null) priority = "All";
        java.time.LocalDate deadline = deadlinePicker != null ? deadlinePicker.getValue() : null;

        System.out.println("Filtering: priority='" + priority + "', deadline=" + deadline);

        clearTaskColumns();
        if (currentField == null) return;
        List<Task> tasks = taskService.getTasksByField(currentField.getId());
        for (Task task : tasks) {
            // --- Apply Filters ---
            // Filter by priority (assuming Task has getPriority())
            if (!"All".equals(priority)) {
                String taskPriority = (task.getPriority() != null) ? task.getPriority() : "";
                if (!taskPriority.equalsIgnoreCase(priority)) {
                    continue;
                }
            }
            // Filter by deadline (assuming Task has getDeadline() returning LocalDate)
            if (deadline != null && task.getDeadline() != null && !task.getDeadline().equals(deadline)) {
                continue;
            }
            // --- If passed all filters, display the task ---
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

    private void handleAddPhotovoltaicTask(Field field) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addtask.fxml"));
            Parent root = loader.load();

            AddTaskController controller = loader.getController();
            controller.setField(field);
            // You can set additional photovoltaic-specific properties here

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Photovoltaic Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh task list after closing the add task window
            LoadTasks(currentField);

        } catch (IOException e) {
            showAlert("Error", "Failed to load photovoltaic task form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAIPromptDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Generate Tasks with AI");
        dialog.setHeaderText("Enter your task description and start date");

        // Set the button types
        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        // Create the prompt input and date picker
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextArea promptInput = new TextArea();
        promptInput.setPromptText("Describe the tasks you want to generate (e.g., 'I want to build a fence in my farm')");
        promptInput.setPrefRowCount(3);
        promptInput.setWrapText(true);
        
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPromptText("Select start date");
        
        Label dateLabel = new Label("Start Date:");
        dateLabel.setStyle("-fx-font-weight: bold;");
        
        content.getChildren().addAll(
            new Label("Task Description:"),
            promptInput,
            dateLabel,
            startDatePicker
        );

        // Add the content to the dialog
        dialog.getDialogPane().setContent(content);

        // Show loading indicator when generating
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        dialog.getDialogPane().setGraphic(progressIndicator);

        // Convert the result to a string when the generate button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                if (startDatePicker.getValue() == null) {
                    showAlert("Error", "Please select a start date.");
                    return null;
                }
                return promptInput.getText();
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(prompt -> {
            if (prompt.trim().isEmpty()) {
                showAlert("Error", "Please enter a task description.");
                return;
            }

            progressIndicator.setVisible(true);
            dialog.getDialogPane().setDisable(true);
            
            try {
                // Convert LocalDate to java.util.Date
                LocalDate selectedDate = startDatePicker.getValue();
                java.util.Date startDate = java.sql.Date.valueOf(selectedDate);
                
                // Generate tasks using Gemini
                geminiTest.generator(currentField, prompt, startDate);
                
                // Refresh the task list to show the new tasks
                LoadTasks(currentField);

                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Tasks have been generated successfully!").showAndWait());
            } catch (Exception e) {
                showAlert("Error", "An error occurred while generating tasks: " + e.getMessage());
                e.printStackTrace();
            } finally {
                progressIndicator.setVisible(false);
                dialog.getDialogPane().setDisable(false);
            }
        });
    }

    private void showGeneratedTasksDialog(List<Task> tasks) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Generated Tasks");
        dialog.setHeaderText("Review and Save Generated Tasks");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save All", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create a table to display the tasks
        TableView<Task> taskTable = new TableView<>();
        
        // Add columns
        TableColumn<Task, String> nameColumn = new TableColumn<>("Task Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        
        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        
        TableColumn<Task, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        
        TableColumn<Task, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstimatedDuration()));
        
        TableColumn<Task, Integer> workersColumn = new TableColumn<>("Workers");
        workersColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getWorkers()).asObject());
        
        TableColumn<Task, String> resourcesColumn = new TableColumn<>("Resources");
        resourcesColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRessource()));

        taskTable.getColumns().addAll(nameColumn, descriptionColumn, priorityColumn, 
                                    durationColumn, workersColumn, resourcesColumn);
        
        // Add the tasks to the table
        taskTable.getItems().addAll(tasks);

        // Add the table to the dialog
        dialog.getDialogPane().setContent(taskTable);

        // Handle the save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Save all tasks
                for (Task task : tasks) {
                    task.setField(currentField);
                    taskService.create(task);
                }
                // Refresh the task list
                LoadTasks(currentField);
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }
    private void handleBack(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fielddisplay.fxml"));
            Pane fieldDisplay = loader.load();

            // Get the FieldController and pass the farm
            FieldController fieldController = loader.getController();
            if (fieldController != null) {
                fieldController.loadField(farm, weather);
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

    public void setWeather(WeatherService.Weather weather) {
        this.weather = weather;
        updateWeatherCard();
    }

    private void updateWeatherCard() {
        if (weather != null) {
            weatherTemp.setText(String.format("%.1f°C", weather.getTemperature()));
            weatherDesc.setText(weather.getDescription());
            weatherIcon.setImage(new Image("http://openweathermap.org/img/wn/" + weather.getIcon() + "@2x.png"));
        }
    }
}
