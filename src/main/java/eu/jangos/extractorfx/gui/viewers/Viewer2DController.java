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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Warkdev
 */
public class Viewer2DController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Viewer2DController.class);
    
    //@FXML    
    //private ParallelCamera camera;
    
    //@FXML
    //private Group group;
    
    @FXML
    private ScrollPane pane;
    
    private ModelRenderer model;
    
    private double minScale = 0.5;
    private double maxScale = 10;
    private double stepScale = 0.1;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing 2D Viewer.");
                
        pane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {    
                if(pane.getContent() == null) {
                    return;
                }
                
                if(event.getDeltaY() < 0) {
                    // Scroll down
                    if(pane.getContent().getScaleX() > minScale) {
                        pane.getContent().setScaleX(pane.getContent().getScaleX() - stepScale);
                        pane.getContent().setScaleY(pane.getContent().getScaleY() - stepScale);                
                    }
                } else {
                    // Scroll up
                    if(pane.getContent().getScaleX() < maxScale) {
                        pane.getContent().setScaleX(pane.getContent().getScaleX() + stepScale);
                        pane.getContent().setScaleY(pane.getContent().getScaleY() + stepScale);                
                    }
                }                                
            }            
        });
        
        logger.info("2D Viewer initialized!");
    }    

    public void displayModel(ModelRenderer model, Pane renderedModel) {
        logger.info("Rendering model..");
        if(model == null) {
            logger.error("Model is null, nothing to render.");
            return;
        }
                
        this.model = model;        
        this.pane.setContent(renderedModel);                       
        
        logger.info("Model rendered!");
    }        
    
}
