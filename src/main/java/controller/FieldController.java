package controller;

import entite.Farm;
import entite.Field;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import service.FieldService;

import java.io.IOException;
import java.util.List;

public class FieldController {

    private final FieldService fieldService = new FieldService();

    @FXML
    private GridPane fieldgrid;
    private Farm currentFarm;


    @FXML
    private Button addFieldBtn;


    @FXML
    public void initialize() {
        fieldgrid.setHgap(30);
        fieldgrid.setVgap(200);
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
        // Debug: Afficher l'ID de la ferme
        System.out.println("Debug - Farm ID: " + farm.getId());

        // Clear existing content in the grid before refreshing
        fieldgrid.getChildren().clear();
        setFarm(farm);
        addFieldBtn.setOnAction(e -> handleAddField(currentFarm));

        List<Field> fields = fieldService.getFieldsByFarm(farm.getId());
        int col = 0, row = 0;

        try {
            for (Field field : fields) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                Pane card = loader.load();

                // Set field data
                ((Label) card.lookup("#Namef")).setText(field.getName());
                ((Label) card.lookup("#Surfacef")).setText(String.format("%.2f ha", field.getSurface()));
                ((Label) card.lookup("#incomef")).setText(String.format("$%.2f", field.getIncome()));
                ((Label) card.lookup("#outcomef")).setText(String.format("$%.2f", field.getOutcome()));

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
}