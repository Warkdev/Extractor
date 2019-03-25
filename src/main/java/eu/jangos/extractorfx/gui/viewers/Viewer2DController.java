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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Warkdev
 */
public class Viewer2DController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Viewer2DController.class);
    
    @FXML
    private TitledPane pane;

    private ModelRenderer model;

    private DropShadow dropShadow;
    private Glow glow;
    private Delta dragDelta;
    private double minScale = 0.1;
    private double maxScale = 10;
    private double stepScale = 0.05;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing 2D Viewer.");

        this.dropShadow = new DropShadow();
        this.glow = new Glow();
        this.dragDelta = new Delta();
        this.pane.setFocusTraversable(false);

        this.pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Pane renderedModel = (Pane) pane.getContent();
                if (renderedModel == null) {
                    return;
                }
                                
                switch (event.getCode()) {   
                    case D:
                        renderedModel.setTranslateX(renderedModel.getTranslateX() + 5);
                        break;
                    case Q:
                        renderedModel.setTranslateX(renderedModel.getTranslateX() - 5);
                        break;
                    case Z:
                        renderedModel.setTranslateY(renderedModel.getTranslateY() - 5);
                        break;
                    case S:
                        renderedModel.setTranslateY(renderedModel.getTranslateY() + 5);
                        break;
                    case ADD:
                        zoom(false);
                        break;
                    case SUBTRACT:
                        zoom(true);
                        break;
                }
                                
            }

        });

        this.pane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                Pane renderedModel = (Pane) pane.getContent();
                if (renderedModel == null) {
                    return;
                }

                if (event.getDeltaY() < 0) {
                    zoom(true);
                } else {
                    zoom(false);
                }
            }
        });

        this.pane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Pane renderedModel = (Pane) pane.getContent();

                switch (event.getButton()) {
                    case PRIMARY:
                        renderedModel.setTranslateX(event.getSceneX() + dragDelta.x);
                        renderedModel.setTranslateY(event.getSceneY() + dragDelta.y);
                        break;
                }
            }
        });

        this.pane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Pane renderedModel = (Pane) pane.getContent();

                switch (event.getButton()) {
                    case PRIMARY:
                        dragDelta.x = renderedModel.getTranslateX() - event.getSceneX();
                        dragDelta.y = renderedModel.getTranslateY() - event.getSceneY();
                        renderedModel.setCursor(Cursor.MOVE);
                        break;
                }
            }
        });

        this.pane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Pane renderedModel = (Pane) pane.getContent();

                switch (event.getButton()) {
                    case PRIMARY:
                        renderedModel.setCursor(Cursor.HAND);
                        break;
                }
            }
        });

        this.pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Pane renderedModel = (Pane) pane.getContent();
                renderedModel.setCursor(Cursor.HAND);
                dropShadow.setInput(glow);
            }
        });

        this.pane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dropShadow.setInput(null);
            }
        });

        logger.info("2D Viewer initialized!");
    }

    @FXML
    private void zoomIn(ActionEvent event) {
        zoom(false);
    }
    
    @FXML
    private void zoomOut(ActionEvent event) {
        zoom(true);
    }
    
    @FXML
    private void rotate(ActionEvent event) {
        if (this.pane.getContent() == null) {
            return;
        }

        rotateContent(Rotate.Z_AXIS, 90);
    }

    private void rotateContent(Point3D axis, double angle) {
        if (this.pane.getContent() == null) {
            return;
        }

        Pane renderedPane = (Pane) this.pane.getContent();

        double width = renderedPane.getWidth() / 2;
        double height = renderedPane.getHeight() / 2;

        this.pane.getContent().getTransforms().add(new Translate(this.pane.getContent().getTranslateX() + width, this.pane.getContent().getTranslateY() + height));
        this.pane.getContent().getTransforms().add(new Rotate(angle, axis));
        this.pane.getContent().getTransforms().add(new Translate(this.pane.getContent().getTranslateX() - width, this.pane.getContent().getTranslateY() - height));
    }

    private void zoom(boolean zoomOut) {
        Pane model = (Pane) this.pane.getContent();
        if (model == null) {
            return;
        }

        if (zoomOut) {
            // Scroll down                    
            if (model.getScaleX() > minScale) {
                model.setScaleX(model.getScaleX() - stepScale);
                model.setScaleY(model.getScaleY() - stepScale);
            }

        } else {
            // Scroll up
            if (model.getScaleX() < maxScale) {
                model.setScaleX(model.getScaleX() + stepScale);
                model.setScaleY(model.getScaleY() + stepScale);
            }
        }
    }

    public void displayModel(ModelRenderer model, Pane renderedModel, boolean fromCache) {
        logger.info("Rendering model..");
        if (model == null) {
            logger.error("Model is null, nothing to render.");
            return;
        }

        this.model = model;            
        if(!fromCache) {
            Scene testScene = new Scene(renderedModel);
            renderedModel.layout();            
            renderedModel.setEffect(dropShadow);
        }
        
        this.pane.setContent(renderedModel);
        
        if(!fromCache) {
            this.pane.getContent().getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
            this.pane.getContent().getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        }

        logger.info("Model rendered!");
    }

}

class Delta {

    double x, y;
}
