package controller;

import controller.CropController;
import entite.Crop;
import entite.Field;
import entite.Farm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.CropCRUD;
import service.FarmService;
import service.FieldService;

import java.io.IOException;
import java.util.List;

public class AddFieldController {
    private final FieldService fieldService = new FieldService();
    private final FarmService farmService = new FarmService();

    private final CropCRUD cropService = new CropCRUD();


    @FXML private TextField nameField;
    @FXML private TextField surfaceField;
    @FXML private TextField budgetField;
    @FXML private TextField incomeField;
    @FXML private TextField outcomeField;
    @FXML private TextArea descriptionField;

    private Farm currentFarm; // La ferme associée au champ
    private Field currentField; // Le champ en cours de modification

    @FXML
    public void initialize() {

    }

    @FXML
    private void handleUpdate() {
        try {
            if (currentField != null) {
                currentField.setName(nameField.getText());
                currentField.setSurface(Double.parseDouble(surfaceField.getText()));
                currentField.setBudget(Double.parseDouble(budgetField.getText()));
                currentField.setIncome(Double.parseDouble(incomeField.getText()));
                currentField.setOutcome(Double.parseDouble(outcomeField.getText()));
                currentField.setDescription(descriptionField.getText());

                fieldService.update(currentField);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Champ modifié avec succès !");
                returnToGrid();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le champ : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        returnToGrid();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void returnToGrid() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fielddisplay.fxml"));
            BorderPane fieldGrid = loader.load();

            // Récupérer le contrôleur et charger les champs
            FieldController fieldController = loader.getController();
            fieldController.loadField(currentFarm);

            // Remplacer uniquement le contenu central
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(fieldGrid);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find main container");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load field grid: " + e.getMessage());
        }
    }

    private BorderPane getMainContainer() {
        Node current = nameField;
        while (current != null && !(current instanceof BorderPane)) {
            current = current.getParent();
        }
        return (BorderPane) current;
    }

    public void setField(Field field) {
        this.currentField = field;

        nameField.setText(field.getName());
        surfaceField.setText(String.valueOf(field.getSurface()));
        budgetField.setText(String.valueOf(field.getBudget()));
        incomeField.setText(String.valueOf(field.getIncome()));
        outcomeField.setText(String.valueOf(field.getOutcome()));
        descriptionField.setText(field.getDescription());
        // Associer le champ à la culture (exemple)
    }

    public void setFarm(Farm farm) {
        this.currentFarm = farm;
    }

    @FXML

    private void handleSave() {
        try {
            // Validate required fields
            if (nameField.getText().isEmpty() || surfaceField.getText().isEmpty() || budgetField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // Get the last added crop
            Crop lastCrop = cropService.getLastAddedCrop();

            if (lastCrop == null) {
                showAlert(Alert.AlertType.WARNING,
                        "No Crops Found",
                        "No crops exist in the system. Please create a crop first.");
                return;
            }

            List<Integer> allCropIds = cropService.getAllCrops()
                    .stream()
                    .map(Crop::getId)
                    .toList();

            // Check if last crop exists in the system
            if (!allCropIds.contains(lastCrop.getId())) {
                showAlert(Alert.AlertType.ERROR,
                        "Invalid Crop",
                        "The last crop no longer exists in the system.");
                return;
            }



            // Get list of crops already assigned to fields
            List<Integer> assignedCropIds = fieldService.getCropIds();

            // Check if last crop is already assigned to a field
            if (assignedCropIds.contains(lastCrop.getId())) {
                showAlert(Alert.AlertType.WARNING,
                        "Crop Already Assigned",
                        "This crop is already assigned to another field.");
                return;
            }

            // Create and save the field with the valid crop
            Field field = new Field();
            field.setName(nameField.getText());
            field.setSurface(Double.parseDouble(surfaceField.getText()));
            field.setBudget(Double.parseDouble(budgetField.getText()));
            field.setIncome(Double.parseDouble(incomeField.getText()));
            field.setOutcome(Double.parseDouble(outcomeField.getText()));
            field.setDescription(descriptionField.getText());
            field.setFarm(currentFarm);
            field.setCrop(lastCrop);

            fieldService.create(field);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Champ ajouté avec succès !");
            returnToGrid();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez entrer des nombres valides pour les champs numériques.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le champ : " + e.getMessage());
        }
    }


    @FXML
    private void handleupdate() {
        try {
            if (currentField != null) {
                if (currentFarm == null) {
                    currentFarm = farmService.getFarmByFieldId(currentField.getId());
                    if (currentFarm == null) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune ferme associée au champ.");
                        return;
                    }
                }

                currentField.setFarm(currentFarm);
                currentField.setName(nameField.getText());
                currentField.setSurface(Double.parseDouble(surfaceField.getText()));
                currentField.setBudget(Double.parseDouble(budgetField.getText()));
                currentField.setIncome(Double.parseDouble(incomeField.getText()));
                currentField.setOutcome(Double.parseDouble(outcomeField.getText()));
                currentField.setDescription(descriptionField.getText());

                fieldService.update(currentField);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Champ modifié avec succès !");
                returnToGrid();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun champ sélectionné pour la mise à jour.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides pour les champs numériques.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le champ : " + e.getMessage());
        }
    }

    public void showAddCropForm(ActionEvent actionEvent) {
        CropController controller = new CropController();
        CropController controller1 = controller;
        controller1.showAddCropForm();
    }


}
