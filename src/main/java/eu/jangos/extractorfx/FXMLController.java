/* 
 * Copyright 2018 Warkdev.
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
package eu.jangos.extractorfx;

import javafx.scene.shape.Rectangle;
import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.JMpqException;
import systems.crigges.jmpq3.MPQOpenOption;

public class FXMLController implements Initializable {

    @FXML
    private Group meshGroup;

    //private MeshView view = new MeshView();
    private PerspectiveCamera camera;
    private double x, y;

    private static final int VIEWPORT_SIZE = 1920;
    private static final double MODEL_SCALE_FACTOR = 80;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE * 21;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String ROOT = "D:\\Downloads\\Test\\";
        String PATH = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";        
        String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
        String obj = "D:\\Downloads\\Test\\Azeroth_32_48.obj";
        String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
        String WMO = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\wmo.MPQ";
        String wmoExample = "world\\wmo\\azeroth\\buildings\\duskwood_humantwostory\\duskwood_humantwostory.wmo";
        ADT adtReader = new ADT();
        ADT2OBJConverter adtConverter = new ADT2OBJConverter(adtReader);
        WMO wmoReader = new WMO();
        ModelConverter wmoConverter = new WMO2OBJConverter(wmoReader);
        Map<String, ModelConverter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        File adtFile = new File(PATH);
        File modelFile = new File(MODEL);
        File wmoFile = new File(WMO);
                
        
         try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            int totalModel = modelEditor.getFileNames().size();
            int doneModel = 0;
            
            
            String outputFile = ROOT + FilenameUtils.removeExtension(wmoExample) + ".obj";
            wmoReader.init(wmoEditor.extractFileAsBytes(wmoExample), wmoExample);
            ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, wmoExample, false);
            //wmoConverter.saveToFile(outputFile);            
            //view.setMesh(wmoConverter.getMesh());                                                                   
             Line line = new Line();
             line.setStartX(150.0f);
             line.setStartY(140.0f);
             line.setEndX(450.0f);
             line.setEndY(140.0f);
             Rectangle rectangle = new Rectangle(50, 20000000, 50, 12);     
             rectangle.setFill(Color.BLUE);             
             this.meshGroup.getChildren().add(line);
             this.meshGroup.getChildren().add(rectangle);

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //initCamera();
        //Platform.runLater(() -> showFigure());        
        //Platform.runLater(() -> show2DPlan());        
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        System.out.println("key pressed !");
    }
    
    private void initCamera() {
        this.camera = new PerspectiveCamera();
        this.camera.setNearClip(0.1);
        this.camera.setFarClip(100.0);
        this.camera.setTranslateZ(-20);
        
        final Rotate cameraRotateX = new Rotate(0, new Point3D(1, 0, 0));
        final Rotate cameraRotateY = new Rotate(0, new Point3D(0, 1, 0));
        final Translate cameraTranslate = new Translate(0, 0, -20);
        camera.getTransforms().addAll(cameraRotateX, cameraRotateY, cameraTranslate);
    }

    private void showFigure() {
        // Add MeshView to Group
        Group meshInGroup = buildScene();
        // Create SubScene
        SubScene subScene = createScene3D(meshInGroup);
        // Add subScene to meshGroup
        this.meshGroup.getChildren().add(subScene);

        RotateTransition rotate = rotate3dGroup(meshInGroup);        
    }

    private void show2DPlan() {
        // Add MeshView to Group
        Group group = buildScene();
        // Create SubScene
        SubScene subScene = createScene2D(group);
        // Add subScene to meshGroup
        this.meshGroup.getChildren().add(subScene);              
    }
    
    private Group buildScene() {
        Group group = new Group();        
        /**view.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
        view.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
        view.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
        view.setScaleX(MODEL_SCALE_FACTOR);
        view.setScaleY(MODEL_SCALE_FACTOR);
        view.setScaleZ(MODEL_SCALE_FACTOR);*/
        
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(VIEWPORT_SIZE*5);
        pointLight.setTranslateY(VIEWPORT_SIZE*5);
        pointLight.setTranslateX(VIEWPORT_SIZE*5);        

        //group.getChildren().addAll(view, pointLight);
        group.getChildren().addAll(pointLight);
        return group;
    }

    private SubScene createScene2D(Group group) {
        SubScene scene2d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene2d.setFill(Color.WHITE);
        scene2d.setCamera(this.camera);
        scene2d.setPickOnBounds(true);
        return scene2d;
    }

    private SubScene createScene3D(Group group) {
        SubScene scene2d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene2d.setFill(Color.WHITE);
        scene2d.setCamera(this.camera);
        scene2d.setPickOnBounds(true);
        return scene2d;
    }
    
    private RotateTransition rotate3dGroup(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }
}
