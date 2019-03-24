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
package eu.jangos.extractorfx.gui.viewers;

import eu.jangos.extractor.file.ModelRenderer;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Warkdev
 */
public class Viewer3DController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Viewer3DController.class);

    @FXML
    private TitledPane pane;

    private ModelRenderer model;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing 3D Viewer");
        
        this.pane.setFocusTraversable(false);
        
        logger.info("3D Viewer initialized!");
    }    
    
    public void displayModel(ModelRenderer model) {
        logger.info("Rendering model..");
        if (model == null) {
            logger.error("Model is null, nothing to render.");
            return;
        }

        this.model = model;                            

        
        
        logger.info("Model rendered!");
    }
}
