package org.example.thermalprinterapp;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.nio.charset.Charset;

public class ThermalPrinterApp extends Application {

    private TextArea textArea;
    private ComboBox<PrintService> printerComboBox;
    private ComboBox<String> encodingComboBox;
    private CheckBox boldCheckBox;
    private CheckBox centerCheckBox;
    private CheckBox cutPaperCheckBox;
    private CheckBox doubleHeightCheckBox;
    private CheckBox underlineCheckBox;
    private Slider copiesSlider;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Thermal Printer Control Panel");

        // Initialize status label first
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        // Create UI components
        createComponents();

        // Layout
        GridPane settingsPane = createSettingsPane();
        VBox mainContent = new VBox(20, createHeader(), textArea, settingsPane, createButtonPanel());
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(mainContent, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createComponents() {
        // Text area
        textArea = new TextArea();
        textArea.setPromptText("Enter text to print...");
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: #ffffff;");
        textArea.setPrefHeight(200);

        // Printer selection
        printerComboBox = new ComboBox<>();
        printerComboBox.setPrefWidth(300);
        printerComboBox.setStyle("-fx-font-size: 14px;");
        loadPrinters(printerComboBox);

        // Encoding selection
        encodingComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "CP437", "UTF-8", "ISO-8859-1", "Windows-1252"
        ));
        encodingComboBox.getSelectionModel().selectFirst();
        encodingComboBox.setStyle("-fx-font-size: 14px;");

        // Checkboxes for formatting
        boldCheckBox = new CheckBox("Bold");
        centerCheckBox = new CheckBox("Center Text");
        cutPaperCheckBox = new CheckBox("Cut Paper");
        cutPaperCheckBox.setSelected(true);
        doubleHeightCheckBox = new CheckBox("Double Height");
        underlineCheckBox = new CheckBox("Underline");

        // Copies slider
        copiesSlider = new Slider(1, 10, 1);
        copiesSlider.setMajorTickUnit(1);
        copiesSlider.setMinorTickCount(0);
        copiesSlider.setSnapToTicks(true);
        copiesSlider.setShowTickMarks(true);
        copiesSlider.setShowTickLabels(true);
    }

    private HBox createHeader() {
        Label titleLabel = new Label("Thermal Printer Control");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setTextFill(Color.web("#333"));

        HBox header = new HBox(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private GridPane createSettingsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 5; -fx-border-color: #ddd; -fx-border-width: 1;");

        // Add components to grid
        grid.add(new Label("Printer:"), 0, 0);
        grid.add(printerComboBox, 1, 0, 2, 1);

        grid.add(new Label("Encoding:"), 0, 1);
        grid.add(encodingComboBox, 1, 1);

        grid.add(new Label("Copies:"), 0, 2);
        grid.add(copiesSlider, 1, 2);
        grid.add(new Label(String.valueOf((int)copiesSlider.getValue())), 2, 2);

        copiesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            grid.getChildren().removeIf(node -> GridPane.getColumnIndex(node) == 2 && GridPane.getRowIndex(node) == 2);
            grid.add(new Label(String.valueOf(newVal.intValue())), 2, 2);
        });

        // Formatting checkboxes
        grid.add(new Label("Formatting:"), 0, 3);

        HBox formatBox1 = new HBox(10, boldCheckBox, doubleHeightCheckBox, underlineCheckBox);
        HBox formatBox2 = new HBox(10, centerCheckBox, cutPaperCheckBox);

        grid.add(formatBox1, 1, 3, 2, 1);
        grid.add(formatBox2, 1, 4, 2, 1);

        return grid;
    }

    private VBox createButtonPanel() {
        Button printButton = createStyledButton("Print", "#4CAF50");
        printButton.setOnAction(e -> printText());

        Button refreshButton = createStyledButton("Refresh Printers", "#2196F3");
        refreshButton.setOnAction(e -> loadPrinters(printerComboBox));

        Button previewButton = createStyledButton("Preview", "#FF9800");
        previewButton.setOnAction(e -> showPreview());

        HBox buttonBox = new HBox(15, printButton, refreshButton, previewButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Status bar
        HBox statusBar = new HBox(statusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        VBox panel = new VBox(10, buttonBox, statusBar);
        return panel;
    }



    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-padding: 8 16 8 16; -fx-background-radius: 4;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-padding: 8 16 8 16; -fx-background-radius: 4;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-padding: 8 16 8 16; -fx-background-radius: 4;"));
        return button;
    }

    private void loadPrinters(ComboBox<PrintService> printerComboBox) {
        if (statusLabel != null) {
            statusLabel.setText("Loading printers...");
        }
        new Thread(() -> {
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            javafx.application.Platform.runLater(() -> {
                printerComboBox.getItems().clear();
                if (printServices.length > 0) {
                    printerComboBox.getItems().addAll(printServices);
                    printerComboBox.getSelectionModel().selectFirst();
                    if (statusLabel != null) {
                        statusLabel.setText("Found " + printServices.length + " printer(s)");
                    }
                } else {
                    printerComboBox.setPromptText("No Printers Found");
                    if (statusLabel != null) {
                        statusLabel.setText("No printers found");
                    }
                }
            });
        }).start();
    }

    private void printText() {
        String text = textArea.getText();
        PrintService printService = printerComboBox.getValue();

        if (text.isEmpty()) {
            statusLabel.setText("Error: No text to print");
            return;
        }

        if (printService == null) {
            statusLabel.setText("Error: No printer selected");
            return;
        }

        statusLabel.setText("Printing...");

        new Thread(() -> {
            try {
                String encoding = encodingComboBox.getValue();
                int copies = (int) copiesSlider.getValue();

                for (int i = 0; i < copies; i++) {
                    printWithFormatting(text, printService, encoding);
                    if (i < copies - 1) {
                        Thread.sleep(500); // Small delay between copies
                    }
                }

                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("Printed successfully to " + printService.getName()));
                        textArea.clear();
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void printWithFormatting(String text, PrintService printService, String encoding) throws Exception {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        DocPrintJob job = printService.createPrintJob();

        // Initialize printer
        byte[] commandInit = { 0x1B, 0x40 };

        // Set formatting
        byte[] formattingCommands = getFormattingCommands();

        // Get text data
        byte[] textData = text.getBytes(Charset.forName(encoding));
        byte[] newLine = { 0x0A };

        // Cut paper if selected
        byte[] cutPaper = cutPaperCheckBox.isSelected() ?
                new byte[] { 0x1D, 0x56, 0x41, 0x00 } : new byte[0];

        // Combine all byte arrays
        byte[] finalData = new byte[
                commandInit.length +
                        formattingCommands.length +
                        textData.length +
                        newLine.length +
                        cutPaper.length
                ];

        int pos = 0;
        System.arraycopy(commandInit, 0, finalData, pos, commandInit.length);
        pos += commandInit.length;
        System.arraycopy(formattingCommands, 0, finalData, pos, formattingCommands.length);
        pos += formattingCommands.length;
        System.arraycopy(textData, 0, finalData, pos, textData.length);
        pos += textData.length;
        System.arraycopy(newLine, 0, finalData, pos, newLine.length);
        pos += newLine.length;
        System.arraycopy(cutPaper, 0, finalData, pos, cutPaper.length);

        Doc doc = new SimpleDoc(finalData, flavor, null);
        job.print(doc, pras);
    }

    private byte[] getFormattingCommands() {
        // ESC/POS commands for formatting
        byte[] bold = boldCheckBox.isSelected() ? new byte[] { 0x1B, 0x45, 0x01 } : new byte[] { 0x1B, 0x45, 0x00 };
        byte[] center = centerCheckBox.isSelected() ? new byte[] { 0x1B, 0x61, 0x01 } : new byte[] { 0x1B, 0x61, 0x00 };
        byte[] doubleHeight = doubleHeightCheckBox.isSelected() ? new byte[] { 0x1B, 0x21, 0x10 } : new byte[] { 0x1B, 0x21, 0x00 };
        byte[] underline = underlineCheckBox.isSelected() ? new byte[] { 0x1B, 0x2D, 0x01 } : new byte[] { 0x1B, 0x2D, 0x00 };

        // Combine all formatting commands
        byte[] combined = new byte[bold.length + center.length + doubleHeight.length + underline.length];
        int pos = 0;
        System.arraycopy(bold, 0, combined, pos, bold.length);
        pos += bold.length;
        System.arraycopy(center, 0, combined, pos, center.length);
        pos += center.length;
        System.arraycopy(doubleHeight, 0, combined, pos, doubleHeight.length);
        pos += doubleHeight.length;
        System.arraycopy(underline, 0, combined, pos, underline.length);

        return combined;
    }

    private void showPreview() {
        String text = textArea.getText();
        if (text.isEmpty()) {
            statusLabel.setText("No text to preview");
            return;
        }

        Stage previewStage = new Stage();
        previewStage.setTitle("Print Preview");

        TextArea previewArea = new TextArea(text);
        previewArea.setEditable(false);
        previewArea.setWrapText(true);
        previewArea.setStyle(getPreviewStyle());

        Scene previewScene = new Scene(new StackPane(previewArea), 400, 600);
        previewStage.setScene(previewScene);
        previewStage.show();
    }

    private String getPreviewStyle() {
        StringBuilder style = new StringBuilder();
        style.append("-fx-font-family: 'Courier New'; ");
        style.append("-fx-font-size: 14px; ");

        if (boldCheckBox.isSelected()) {
            style.append("-fx-font-weight: bold; ");
        }

        if (doubleHeightCheckBox.isSelected()) {
            style.append("-fx-font-size: 20px; ");
        }

        if (underlineCheckBox.isSelected()) {
            style.append("-fx-underline: true; ");
        }

        return style.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
