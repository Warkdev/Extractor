/*
 * Copyright 2019 Warkdev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.jangos.extractorfx.gui;

import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.gui.assets.AssetTabController;
import eu.jangos.extractorfx.gui.assets.StringCellComparator;
import eu.jangos.extractorfx.gui.assets.StringCellFactory;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Warkdev
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox wdtTab;
    @FXML
    private AssetTabController wdtTabController;

    @FXML
    private VBox adtTab;
    @FXML
    private AssetTabController adtTabController;

    @FXML
    private VBox wmoTab;
    @FXML
    private AssetTabController wmoTabController;

    @FXML
    private VBox modelTab;
    @FXML
    private AssetTabController modelTabController;

    private DirectoryChooser dirChooser;
    private Stage stage;

    private MPQManager mpqManager;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.dirChooser = new DirectoryChooser();
        this.dirChooser.setTitle("Select the directory where MPQ files are stored");
        // Debug
        this.dirChooser.setInitialDirectory(new File("D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK"));
    }

    @FXML
    private void onOpenMPQAction(ActionEvent event) {
        logger.info("Opening MPQ directory.");

        this.wdtTabController.clearListItems();
        this.adtTabController.clearListItems();
        this.wmoTabController.clearListItems();
        this.modelTabController.clearListItems();

        File directory = this.dirChooser.showDialog(stage);
        logger.debug("Selected directory: " + directory.getAbsolutePath());
        if (this.mpqManager != null) {
            this.mpqManager.setDirectory(directory);
        } else {
            try {
                this.mpqManager = new MPQManager(directory.getAbsolutePath());
            } catch (MPQException ex) {
                logger.error(ex.getMessage());
            }
        }

        this.wdtTabController.addItems(this.mpqManager.getListWDT());
        this.adtTabController.addItems(this.mpqManager.getListADT());
        this.wmoTabController.addItems(this.mpqManager.getListWMO());
        this.modelTabController.addItems(this.mpqManager.getListM2());

        logger.info("MPQ directory opened..");
    }

    @FXML
    private void onCloseAction(ActionEvent event) {
        logger.info("Closing application...");
        this.stage.close();
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
