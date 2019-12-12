package edus2.adapter.ui;/*
 * Copyright 2016 Paul Kulyk, Paul Olszynski, Cameron Auser
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import edus2.adapter.repository.file.FileScanImportExportRepository;
import edus2.application.AuthenticationFacade;
import edus2.application.EDUS2View;
import edus2.application.ScanFacade;
import edus2.application.exception.EmptyScanIdException;
import edus2.application.exception.ScanAlreadyExistsException;
import edus2.domain.EDUS2Configuration;
import edus2.domain.MannequinScanEnum;
import edus2.domain.Scan;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose: Display a settings window for EDUS2.
 *
 * @author Cameron Auser
 * @version 1.0
 */
public class ScanSettingsWindow extends VBox {
    private Stage stage;
    private ScansWindow scanList;
    private ScanFacade scanFacade;
    private FileScanImportExportRepository importExportRepository;

    public ScanSettingsWindow(ScanFacade scanFacade, AuthenticationFacade authenticationFacade, EDUS2Configuration configuration) {
        // Just set up a settings window, which is then shown on-screen
        super(10);
        this.scanFacade = scanFacade;
        this.importExportRepository = new FileScanImportExportRepository(scanFacade);
        scanList = new ScansWindow(scanFacade);
        HBox scanSettingButtonsBox = new HBox();
        HBox configurationButtonBox = new HBox();

        Button btnAdd = new Button("Add");
        Button btnBulkAdd = new Button("Bulk Add");
        Button btnDelete = new Button("Delete");
        Button btnDeleteAll = new Button("Delete All");
        Button btnImport = new Button("Import from File");
        Button btnExport = new Button("Export to File");

        // When add is clicked, run through the process of adding a new scan
        btnAdd.setOnAction(event -> {
            if (scanFacade.getUnusedScanEnums().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR, "All mannequin scan locations have been linked to scans already!");
                alert.showAndWait();
            } else {
                FileChooser browser = new FileChooser();
                File selected = browser.showOpenDialog(stage);
                if (selected != null) {
                    promptForScanIdAndSaveScan(selected);
                }
            }
        });

        // When bulk add is clicked, the user can select multiple files to add.
        // Then we run through each selected file and set ID's for all of them.
        btnBulkAdd.setOnAction(event -> {
            if (scanFacade.getUnusedScanEnums().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR, "All mannequin scan locations have been linked to scans already!");
                alert.showAndWait();
                return;
            }
            
            FileChooser browser = new FileChooser();
            List<File> selected = browser.showOpenMultipleDialog(stage);
            if (selected == null) {
                return;
            }

            if (selected.size() > scanFacade.getUnusedScanEnums().size()) {
                Set<String> unusedScanEnumNames = scanFacade.getUnusedScanEnums()
                        .stream()
                        .map(MannequinScanEnum::getName)
                        .collect(Collectors.toSet());
                String unusedLocations = String.join(",", unusedScanEnumNames);
                Alert alert = new Alert(AlertType.ERROR, String.format("You've selected too many videos: you can only link videos to the following locations: %s", unusedLocations));
                alert.showAndWait();
            } else {
                for (File current : selected) {
                    promptForScanIdAndSaveScan(current);
                }
            }
        });

        // Delete the selected scan when the delete button is clicked.
        btnDelete.setOnAction(event -> {
            Scan selected = scanList.getSelectedItem();
            scanFacade.removeScan(selected);
            scanList.refreshTableItems();
        });

        // Delete ALL the scans when the delete all button is clicked.
        btnDeleteAll.setOnAction(event -> {
            // Show the user a warning, to let them know they're going
            // to permanently remove all scans from the program by doing this
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText("It is suggested to export all scans before doing this.\nAre you sure you want to remove ALL scans?\n");
            alert.setHeaderText("Proceed with removing all scans?");
            alert.setTitle("Proceed with removing all scans?");
            alert.showAndWait();
            if (alert.getResult().getText().equals("OK")) {
                // If OK was clicked, we'll nuke all the scans
                scanFacade.removeAllScans();
                scanList.refreshTableItems();
            }
        });

        // Import scans from a file
        btnImport.setOnAction(event -> importScans());

        // Export all the current scans to a file
        btnExport.setOnAction(event -> exportScans());

        scanSettingButtonsBox.setAlignment(Pos.CENTER);
        scanSettingButtonsBox.getChildren().addAll(btnAdd, btnBulkAdd, btnDelete, btnDeleteAll, btnImport, btnExport);

        Button btnConfigSettings = new Button("Configuration Settings");
        btnConfigSettings.setOnAction(e -> {
            ConfigurationWindow configurationWindow = new ConfigurationWindow(configuration, authenticationFacade);
            Stage configurationStage = new Stage();
            Scene configurationScene = new Scene(configurationWindow);
            configurationStage.setScene(configurationScene);
            configurationWindow.setStage(configurationStage);
            configurationStage.showAndWait();
            // needed to keep scan list up-to-date if scan file is changed
            scanList.refreshTableItems();
        });

        configurationButtonBox.setAlignment(Pos.CENTER);
        configurationButtonBox.getChildren().add(btnConfigSettings);
        this.getChildren().addAll(scanList, scanSettingButtonsBox, configurationButtonBox);

    }

    private void promptForScanIdAndSaveScan(File file) {
        boolean added = false;
        while (!added) {
            String convertedFilePath = EDUS2View.convertFilePath(file.getPath());
            Optional<MannequinScanEnum> scanLocationOptional = promptForScanLocation("Filename: " + file.getName() + "\nWhat location would you like to link the video to?");
            if (!scanLocationOptional.isPresent()) {
                break;
            }

            added = addScan(scanLocationOptional.get(), convertedFilePath);
        }
    }

    private Optional<MannequinScanEnum> promptForScanLocation(String prompt) {
        Set<MannequinScanEnum> unusedScanEnums = scanFacade.getUnusedScanEnums();
        ChoiceDialog<String> scanLocationDialog = new ChoiceDialog<>(unusedScanEnums.iterator().next().getName(), unusedScanEnums.stream().map(MannequinScanEnum::getName).collect(Collectors.toSet()));
        scanLocationDialog.setHeaderText("Choose Scan Location");
        scanLocationDialog.setContentText(prompt);
        return scanLocationDialog.showAndWait().map(MannequinScanEnum::findByName);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.getIcons().add(EDUS2View.getThumbnailImage());
    }

    private void importScans() {
        FileChooser browser = new FileChooser();
        File scanFile = browser.showOpenDialog(stage);
        if (scanFile != null) {
            try {
                importExportRepository.importScansFromFile(scanFile);
                scanList.refreshTableItems();
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR, String.format("Encountered error while importing scans: %s", e.getMessage()));
                alert.showAndWait();
            }
        }
    }

    /**
     * Purpose: Export all scans to a file of the user's choice.
     */
    private void exportScans() {
        // Show a file browser, so the user can select a file to save to.
        FileChooser browser = new FileChooser();
        File selected = browser.showSaveDialog(stage);
        if (selected != null) {
            try {
                importExportRepository.exportScansToFile(selected);
                Alert alert = new Alert(AlertType.INFORMATION, "Scans exported successfully!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR, String.format("Encountered error while exporting scans: %s", e.getMessage()));
                alert.showAndWait();
            }
        }
    }

    private boolean addScan(MannequinScanEnum scanEnum, String path) {
        Scan toAdd = new Scan(scanEnum, path);
        try {
            scanFacade.addScan(toAdd);
            scanList.refreshTableItems();
            return true;
        } catch (EmptyScanIdException | ScanAlreadyExistsException e) {
            Alert alert = new Alert(AlertType.ERROR, e.getMessage());
            alert.showAndWait();
            return false;
        }
    }
}