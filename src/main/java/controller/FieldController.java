package controller;

import entite.Farm;
import entite.Field;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.FieldService;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.IOException;
import java.io.InputStream;

// QR Code imports
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.stage.Modality;

public class FieldController {
    private final FieldService fieldService = new FieldService();
    private Field selectedField;
    private final Map<Integer, String> fieldAccessCodes = new HashMap<>();

    @FXML
    private GridPane fieldgrid;
    private Farm currentFarm;

    @FXML
    private Button addFieldBtn;
    
    // FXML elements for dynamically loaded cards
    @FXML
    private HBox buttonBox;

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
                System.out.println("Debug - Loading field: " + field.getName() + " (ID: " + field.getId() + ")");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                // We're not using a controller for the field card, so we load it directly
                Pane card = loader.load();

                // Set field data with null checking
                setLabelTextSafely(card, "#Namef", field.getName());
                setLabelTextSafely(card, "#Surfacef", String.format("%.2f ha", field.getSurface()));
                
                // Fix the income/outcome labels which are in the same grid cell in the FXML
                Label incomeLabel = (Label) card.lookup("#incomef");
                Label outcomeLabel = (Label) card.lookup("#outcomef");
                
                try {
                    if (incomeLabel != null && outcomeLabel != null) {
                        // Find the GridPane correctly - search for it using traversal
                        System.out.println("Debug - Attempting to find GridPane for field: " + field.getName());
                        GridPane gridPane = findGridPaneInCard(card);
                        System.out.println("Debug - GridPane lookup result: " + (gridPane != null ? "found" : "not found"));
                        
                        if (gridPane != null) {
                            // Log the current structure of the GridPane
                            // Log the current structure of the GridPane
                            Integer incomeRow = GridPane.getRowIndex(incomeLabel);
                            Integer outcomeRow = GridPane.getRowIndex(outcomeLabel);
                            Integer incomeCol = GridPane.getColumnIndex(incomeLabel);
                            Integer outcomeCol = GridPane.getColumnIndex(outcomeLabel);
                            
                            System.out.println("Debug - GridPane structure before changes:");
                            System.out.println("  - incomeLabel: row=" + (incomeRow != null ? incomeRow : "default(0)") + 
                                              ", col=" + (incomeCol != null ? incomeCol : "default(0)"));
                            System.out.println("  - outcomeLabel: row=" + (outcomeRow != null ? outcomeRow : "default(0)") + 
                                              ", col=" + (outcomeCol != null ? outcomeCol : "default(0)"));
                            
                            // Log all children of GridPane
                            // Log all children of GridPane
                            System.out.println("Debug - GridPane children count: " + gridPane.getChildren().size());
                            for (Node child : gridPane.getChildren()) {
                                Integer gridRowIndex = GridPane.getRowIndex(child);
                                Integer gridColIndex = GridPane.getColumnIndex(child);
                                System.out.println("  - Child: " + child.getClass().getSimpleName() + 
                                                  " at row=" + (gridRowIndex != null ? gridRowIndex : "default(0)") + 
                                                  ", col=" + (gridColIndex != null ? gridColIndex : "default(0)"));
                            }
                            try {
                                GridPane.setRowIndex(outcomeLabel, 2);
                                // Also set a column index to ensure it's in the right position
                                if (GridPane.getColumnIndex(outcomeLabel) == null) {
                                    GridPane.setColumnIndex(outcomeLabel, 1);
                                }
                                System.out.println("Debug - Repositioned outcomeLabel to row 2");
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to reposition outcomeLabel: " + e.getMessage());
                            }
                            
                            // Add a new label for "Outcome:" in the left column
                            try {
                                Label outcomeTextLabel = new Label("Outcome:");
                                GridPane.setRowIndex(outcomeTextLabel, 2);
                                GridPane.setColumnIndex(outcomeTextLabel, 0);
                                gridPane.getChildren().add(outcomeTextLabel);
                                System.out.println("Debug - Added 'Outcome:' label to GridPane");
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to add outcome text label: " + e.getMessage());
                            }
                        } else {
                            System.err.println("Warning: GridPane not found in field card for field: " + field.getName());
                        }
                        
                        // Set the text for the labels even if GridPane adjustments failed
                        incomeLabel.setText(String.format("$%.2f", field.getIncome()));
                        outcomeLabel.setText(String.format("$%.2f", field.getOutcome()));
                    } else {
                        System.err.println("Warning: Income or outcome labels not found for field: " + field.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error handling income/outcome labels: " + e.getMessage());
                    e.printStackTrace();
                }

                // Debug logging for card structure
                System.out.println("Debug - Card structure loaded for field: " + field.getName());

                // Get buttons and add event handlers with null checking
                Button deleteBtn = (Button) card.lookup("#deleteBtn");
                Button modifyBtn = (Button) card.lookup("#modifyBtn");
                Button detailsBtn = (Button) card.lookup("#detailsBtn");

                // Log button discovery status
                System.out.println("Debug - Button discovery: deleteBtn=" + (deleteBtn != null) + 
                                  ", modifyBtn=" + (modifyBtn != null) + 
                                  ", detailsBtn=" + (detailsBtn != null));

                // Setup delete button
                if (deleteBtn != null) {
                    deleteBtn.setOnAction(e -> handleDelete(field, card));
                } else {
                    System.err.println("Warning: deleteBtn not found in field card for field: " + field.getName());
                }

                // Setup modify button
                if (modifyBtn != null) {
                    modifyBtn.setOnAction(e -> handleModify(field));
                } else {
                    System.err.println("Warning: modifyBtn not found in field card for field: " + field.getName());
                }

                // Setup details button
                if (detailsBtn != null) {
                    detailsBtn.setOnAction(e -> handleDetails(field));
                } else {
                    System.err.println("Warning: detailsBtn not found in field card for field: " + field.getName());
                }

                // Add QR code and verify buttons
                Button qrCodeButton = new Button("QR Code");
                qrCodeButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
                qrCodeButton.setOnAction(e -> showQRCode(field));

                Button verifyButton = new Button("Verify");
                verifyButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                verifyButton.setOnAction(e -> verifyAccessCode(field));

                // Add QR code and verify buttons to the buttonBox
                setupButtonBox(card, field, qrCodeButton, verifyButton);

                // Add to grid
                fieldgrid.add(card, col % 3, row);
                col++;
                if (col % 3 == 0) row++;
            }
        } catch (Exception e) {
            showError("Error Loading Fields", "Failed to load fields: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for better debugging
        }
    }

    /**
     * Sets up the buttonBox in the card, adding the QR code and verify buttons.
     * If the buttonBox doesn't exist in the FXML, creates one programmatically.
     * 
     * @param card The field card pane
     * @param field The field data
     * @param qrCodeButton The QR code button to add
     * @param verifyButton The verify button to add
     */
    private void setupButtonBox(Pane card, Field field, Button qrCodeButton, Button verifyButton) {
        try {
            // Try to find existing buttonBox
            HBox buttonBox = (HBox) card.lookup("#buttonBox");
            System.out.println("Debug - ButtonBox lookup result for field " + field.getId() + ": " + 
                              (buttonBox != null ? "found" : "not found"));
            
            if (buttonBox != null) {
                // If buttonBox exists in FXML, add buttons to it
                buttonBox.getChildren().addAll(qrCodeButton, verifyButton);
                System.out.println("Debug - Added buttons to existing buttonBox for field: " + field.getName());
            } else {
                // Log error for debugging
                System.err.println("Warning: buttonBox not found in field.fxml for field: " + field.getName() + 
                                  " (ID: " + field.getId() + "). Creating one programmatically.");
                
                // Create buttonBox programmatically if not found in FXML
                buttonBox = new HBox(10); // 10 is the spacing
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.setId("buttonBox"); // Set the ID for future reference
                buttonBox.getStyleClass().add("button-container"); // Add CSS class
                buttonBox.getChildren().addAll(qrCodeButton, verifyButton);
                
                // Find the VBox with the farm-details class as defined in the FXML
                VBox fieldDetails = (VBox) card.lookup(".farm-details");
                System.out.println("Debug - VBox farm-details lookup result for field " + field.getId() + ": " + 
                                  (fieldDetails != null ? "found" : "not found"));
                
                if (fieldDetails != null) {
                    // Add the buttonBox to the VBox with farm-details class
                    fieldDetails.getChildren().add(buttonBox);
                    System.out.println("Debug - Added buttonBox to VBox.farm-details for field: " + field.getName());
                } else {
                    System.err.println("Warning: Could not find VBox with farm-details class for field: " + field.getName());
                    
                    // Try alternative lookup strategies
                    if (card instanceof VBox) {
                        boolean added = false;
                        ObservableList<Node> children = ((VBox) card).getChildren();
                        for (Node child : children) {
                            if (child instanceof VBox) {
                                VBox vbox = (VBox) child;
                                vbox.getChildren().add(buttonBox);
                                System.out.println("Debug - Added buttonBox to child VBox for field: " + field.getName());
                                added = true;
                                break;
                            }
                        }
                        
                        if (!added) {
                            // If we couldn't find a child VBox, add directly to the main VBox
                            ((VBox) card).getChildren().add(buttonBox);
                            System.out.println("Debug - Added buttonBox directly to main VBox for field: " + field.getName());
                        }
                    } else {
                        // Last resort - add directly to whatever pane we have
                        ((Pane) card).getChildren().add(buttonBox);
                        System.out.println("Debug - Added buttonBox directly to card Pane for field: " + field.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up buttonBox for field " + field.getName() + ": " + e.getMessage());
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

            // Pass the field to TaskController
            TaskController taskController = loader.getController();
            if (taskController != null) {
                taskController.LoadTasks(field);
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
    
    /**
     * Sets text for a Label safely, checking for null values
     */
    private void setLabelTextSafely(Pane container, String selector, String text) {
        Label label = (Label) container.lookup(selector);
        if (label != null) {
            label.setText(text);
        } else {
            System.err.println("Warning: Label with selector '" + selector + "' not found");
        }
    }
    
    /**
     * Finds the first child of a specific type in the node hierarchy
     */
    private Node findFirstChildOfType(Pane container, Class<?> type) {
        for (Node node : container.getChildren()) {
            if (type.isInstance(node)) {
                return node;
            } else if (node instanceof Pane) {
                Node result = findFirstChildOfType((Pane) node, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the GridPane inside a field card using proper traversal
     * @param card The field card pane
     * @return The GridPane if found, null otherwise
     */
    private GridPane findGridPaneInCard(Pane card) {
        System.out.println("Debug - Searching for GridPane in field card");
        
        try {
            // Try looking up GridPane within .farm-details VBox first
            VBox farmDetails = (VBox) card.lookup(".farm-details");
            if (farmDetails != null) {
                System.out.println("Debug - Found .farm-details VBox, searching for GridPane inside");
                
                // Log the structure of farmDetails for debugging
                System.out.println("Debug - .farm-details children count: " + farmDetails.getChildren().size());
                int childIndex = 0;
                
                for (Node child : farmDetails.getChildren()) {
                    System.out.println("Debug - Child " + childIndex + " type: " + child.getClass().getSimpleName());
                    childIndex++;
                    
                    if (child instanceof GridPane) {
                        System.out.println("Debug - Found GridPane in .farm-details children");
                        return (GridPane) child;
                    }
                }
            } else {
                System.out.println("Debug - .farm-details VBox not found");
            }
            
            // Try with CSS selectors
            for (Node node : card.lookupAll("GridPane")) {
                if (node instanceof GridPane) {
                    System.out.println("Debug - Found GridPane using CSS selector");
                    return (GridPane) node;
                }
            }
            
            // If not found, try general DOM traversal
            System.out.println("Debug - Performing recursive search for GridPane");
            Node result = findFirstChildOfType(card, GridPane.class);
            if (result != null) {
                System.out.println("Debug - Found GridPane using recursive search");
                return (GridPane) result;
            }
            
            // Fallback - no GridPane found
            System.out.println("Debug - Failed to find GridPane, returning null");
            return null;
        } catch (Exception e) {
            System.err.println("Error finding GridPane: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void showQRCode(Field field) {
        try {
            // Generate new random code
            String accessCode = generateRandomCode();
            fieldAccessCodes.put(field.getId(), accessCode);

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(accessCode, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            
            // Create popup window
            Stage qrStage = new Stage();
            qrStage.setTitle("Field Access Code");
            
            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            closeButton.setOnAction(e -> qrStage.close());
            
            layout.getChildren().addAll(
                new Label("Scan this QR code to get the access code"),
                imageView,
                closeButton
            );
            
            Scene scene = new Scene(layout);
            qrStage.setScene(scene);
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.show();
            
        } catch (WriterException e) {
            showError("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void verifyAccessCode(Field field) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Verify Access Code");
        dialog.setHeaderText("Enter the access code from the QR code");

        ButtonType verifyButtonType = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("Enter access code");
        grid.add(new Label("Access Code:"), 0, 0);
        grid.add(codeField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        Node verifyButton = dialog.getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            verifyButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == verifyButtonType) {
                return codeField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            String storedCode = fieldAccessCodes.get(field.getId());
            if (code.equals(storedCode)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Access code verified successfully!");
                alert.showAndWait();
                handleDetails(field);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid access code. Please try again.");
                alert.showAndWait();
            }
        });
    }
}
