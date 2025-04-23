    package controller;

    import entite.Farm;
    import entite.Field;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.layout.GridPane;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.BorderPane;
    import service.FieldService;

    import java.io.IOException;
    import java.io.InputStream;
    import java.util.List;
    import javafx.stage.Stage;
    import javafx.scene.Parent;
    import javafx.scene.Scene;

    public class FieldController {

        private final FieldService fieldService = new FieldService();

        @FXML
        private GridPane fieldgrid;
        private Farm currentFarm;


        @FXML
        private Button addFieldBtn;

        @FXML
        private Button backButton;

        @FXML
        public void initialize() {
            fieldgrid.setHgap(30);
            fieldgrid.setVgap(200);
            
            if (backButton != null) {
                backButton.setOnAction(e -> handleBackToFarm());
            }
        }

        private void handleAddField(Farm farm) {
            try {
                System.out.println("Debug - Farm ID: " + farm.getId());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/addfield.fxml"));
                Pane addForm = loader.load();

                // Récupérer le contrôleur et passer l'objet Farm
                AddFieldController addFieldController = loader.getController();
                addFieldController.setFarm(farm);

                BorderPane mainContainer = getMainContainer();
                if (mainContainer != null) {
                    mainContainer.setCenter(addForm);
                } else {
                    showError("Error", "Could not find main container");
                }
            } catch (IOException e) {
                showError("Error", "Could not load add field form: " + e.getMessage());
            }
        }
        public void setFarm(Farm farm) {
            this.currentFarm = farm;
        }
        public void loadField(Farm farm) {
            // Debug: Print farm ID
            System.out.println("Debug - Farm ID: " + farm.getId());

            // Clear existing content in the grid before refreshing
            fieldgrid.getChildren().clear();
            setFarm(farm);
            
            // Set up the add field button
            if (addFieldBtn != null) {
                addFieldBtn.setOnAction(e -> handleAddField(currentFarm));
            }

            // Load fields for the farm
            List<Field> fields = fieldService.getFieldsByFarm(farm.getId());
            int col = 0, row = 0;

            try {
                for (Field field : fields) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                    Pane card = loader.load();

                    // Set field data
                    ((Label) card.lookup("#Namef")).setText(field.getName());
                    ((Label) card.lookup("#Surfacef")).setText(String.format("%.2f ha", field.getSurface()));

                    ((Label) card.lookup("#Budgetf")).setText(String.format("$%.2f", field.getBudget()));

                    ((Label) card.lookup("#incomef")).setText(String.format("$%.2f", field.getIncome()));
                    ((Label) card.lookup("#outcomef")).setText(String.format("$%.2f", field.getOutcome()));
                    System.out.println(field.toString());
                    ((Label) card.lookup("#cropf")).setText(field.getCrop() != null ? field.getCrop().getTypeCrop() : "No Crop");

                    ((Label) card.lookup("#descriptionf")).setText(field.getDescription());


                    // Get buttons and add event handlers
                    Button deleteBtn = (Button) card.lookup("#deleteBtn");
                    Button modifyBtn = (Button) card.lookup("#modifyBtn");
                    Button detailsBtn = (Button) card.lookup("#detailsBtn");

                    // Setup delete button
                    deleteBtn.setOnAction(e -> handleDelete(field, card));

                    // Setup modify button
                    modifyBtn.setOnAction(e -> handleModify(field));

                    // Setup details button
                    detailsBtn.setOnAction(e -> handleDetails(field));

                    // Add to grid
                    fieldgrid.add(card, col % 3, row);
                    col++;
                    if (col % 3 == 0) row++;
                }
            } catch (Exception e) {
                showError("Error Loading Fields", "Failed to load fields: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleModify(Field field) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/updatefield.fxml"));
                Pane modifyForm = loader.load();

                // Récupérer le contrôleur et passer l'objet Field
                AddFieldController addFieldController = loader.getController();
                addFieldController.setField(field);

                BorderPane mainContainer = getMainContainer();
                if (mainContainer != null) {
                    mainContainer.setCenter(modifyForm);
                } else {
                    showError("Error", "Could not find main container");
                }
            } catch (IOException e) {
                showError("Error", "Could not load modify field form: " + e.getMessage());
            }
        }

        private void handleDetails(Field field) {
            try {
                // Use getResourceAsStream for more reliable loading
                InputStream fxmlStream = getClass().getResourceAsStream("/TaskDetails.fxml");
                if (fxmlStream == null) {
                    System.err.println("Error: TaskDetails.fxml not found in resources");
                    return;
                }

                FXMLLoader loader = new FXMLLoader();
                Pane details = loader.load(fxmlStream);

                // Pass both the field and farm to TaskController
                TaskController taskController = loader.getController();
                if (taskController != null) {
                    taskController.LoadTasks(field);
                    taskController.setCurrentFarm(currentFarm); // Pass the farm object
                } else {
                    System.err.println("Error: TaskController not initialized");
                }

                // Replace main content
                BorderPane mainContainer = getMainContainer();
                if (mainContainer != null) {
                    mainContainer.setCenter(details);
                } else {
                    showError("Error", "Could not find main container");
                }
            } catch (IOException e) {
                showError("Error", "Could not load task details: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleDelete(Field field, Pane card) {
            fieldService.delete(field);
            fieldgrid.getChildren().remove(card);
        }

        private BorderPane getMainContainer() {
            Node current = fieldgrid;
            while (current != null && !(current instanceof BorderPane)) {
                current = current.getParent();
            }
            return (BorderPane) current;
        }

        private void showError(String title, String message) {
            System.err.println(title + ": " + message);
        }
        @FXML
        private void handleBack(Farm farm) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
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

        private void handleBackToFarm() {
            try {
                // Get the current stage
                Stage stage = (Stage) backButton.getScene().getWindow();
                
                // Load the farm display view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
                Parent root = loader.load();
                
                // Get the controller and load farms
                FarmController controller = loader.getController();
                controller.loadFarms();
                
                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showError("Error", "Could not navigate back to farm: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void setCurrentFarm(Farm farm) {
            this.currentFarm = farm;
        }
    }