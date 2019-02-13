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

import eu.jangos.extractor.file.FileReader;
import eu.jangos.extractor.file.ModelRenderer;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.ModelRendererException;
import eu.jangos.extractor.file.impl.ADT;
import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractor.file.impl.WDT;
import eu.jangos.extractor.file.impl.WMO;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.gui.assets.AssetTabController;
import eu.jangos.extractorfx.gui.viewers.Viewer2DController;
import eu.jangos.extractorfx.rendering.Render2DType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.StatusBar;
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
    private BorderPane borderPane;
    
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

    @FXML
    private StatusBar statusBar;

    @FXML
    private TitledPane viewer2D;

    @FXML
    private Viewer2DController viewer2DController;

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

        this.wdtTabController.setMediator(this);
        this.wdtTabController.setModel(new WDT());

        this.adtTabController.setMediator(this);
        this.adtTabController.setModel(new ADT());

        this.wmoTabController.setMediator(this);
        this.wmoTabController.setModel(new WMO());

        this.modelTabController.setMediator(this);
        this.modelTabController.setModel(new M2());
                
        BorderPane.setMargin(this.viewer2D, Insets.EMPTY);
        BorderPane.setAlignment(this.viewer2D, Pos.TOP_LEFT);
        this.viewer2D.setVisible(false);
        
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

    public void render2D(Render2DType renderType, String filename, ModelRenderer model) {        
        if (model == null) {
            return;
        }
            
        Task task = new Task<Pane>() {
            @Override
            protected Pane call() throws Exception {
                Pane pane = null;
                logger.info("Rendering 2D model " + renderType + " for file " + FilenameUtils.getBaseName(filename));
                updateMessage("Rendering 2D model " + renderType + " for file " + FilenameUtils.getBaseName(filename));
                try {
                    if (((FileReader) model).getFilename() == null || !((FileReader) model).getFilename().equals(filename)) {
                        // Avoid init twice the same file.
                        ((FileReader) model).init(mpqManager, filename, true);
                    }                   
                    pane = model.render2D(renderType);                                        
                    logger.info("Rendering finished!");
                    updateMessage("Rendering finished!");
                } catch (IOException | MPQException mioe) {
                    logger.error("Error while initializing the file");
                    updateMessage("Error while initializing the file");
                } catch (FileReaderException fre) {
                    logger.error("Error while reading the model");
                    updateMessage("Error while reading the model");
                } catch (ModelRendererException mre) {
                    logger.error("Error while rendering the model");
                    updateMessage("Error while rendering the model");
                }
                return pane;
            }
        };
        
        task.setOnSucceeded(new EventHandler() {
            @Override
            public void handle(Event event) {  
                logger.info("Task succeeded!");
                viewer2DController.displayModel(model, (Pane) task.getValue());
                viewer2D.setVisible(true);
            }
        });
        
        task.setOnFailed(new EventHandler() {
            @Override
            public void handle(Event event) {                
                logger.info("Task failed!");
                task.getException().printStackTrace();
                viewer2D.setVisible(false);
            }
        });
        
        statusBar.textProperty().bind(task.messageProperty());        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();        
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
