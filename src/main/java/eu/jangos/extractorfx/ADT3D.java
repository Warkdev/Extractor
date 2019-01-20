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

import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.ModelRendererException;
import eu.jangos.extractor.file.impl.ADT;
import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractor.file.impl.WDT;
import eu.jangos.extractor.file.impl.WMO;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.rendering.FileType3D;
import eu.jangos.extractorfx.rendering.PolygonMeshView;
import eu.jangos.extractorfx.rendering.Render2DType;
import eu.jangos.extractorfx.rendering.Render3DType;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADT3D extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ADT3D.class);

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String DATA = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\";
    private static final String azeroth = "world\\maps\\azeroth\\azeroth.wdt";
    private static final String kalimdor = "world\\maps\\kalimdor\\kalimdor.wdt";
    private static final String map = "World\\Maps\\emeralddream\\emeralddream_33_27.adt";
    //private static final String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
    //private static final String map = "world\\maps\\kalimdor\\kalimdor_32_18.adt";
    private static MPQManager manager;
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\KL_Diremaul\\KL_Diremaul_Instance.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_ScarletMonestary\\Monestary_Cathedral.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_raid.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_raid.wmo";
    private static final String cathedral = "World\\wmo\\Dungeon\\LD_ScarletMonestary\\Monestary_Cathedral.wmo";
    private static final String undercity = "World\\wmo\\Lorderon\\Undercity\\Undercity.wmo";
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    private static final String ironforge = "World\\wmo\\KhazModan\\Cities\\Ironforge\\ironforge.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Buildings\\Stranglethorn_BootyBay\\BootyBay.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\MD_Crypt\\MD_Crypt_D.wmo";
    private static final String stormwind = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Buildings\\Prison_Camp\\PrisonOublietteLarge.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Collidable Doodads\\Elwynn\\AbbeyGate\\abbeygate01.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\KL_Blackfathom\\Blackfathom_instance.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_B.wmo";
    //private static final String wmoExample = "World\\wmo\\Kalimdor\\Darnassis\\Darnassis.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\AZ_Blackrock\\Blackrock.wmo";
    //private static final String wmoExample = "World\\wmo\\Lorderon\\Buildings\\EasternPlaguelands\\UndeadZiggurat\\UndeadZiggurat.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\AZ_Blackrock\\Blackrock_lower_guild.wmo";
    private static final String m2Example = "world\\azeroth\\elwynn\\passivedoodads\\trees\\elwynntreecanopy04.M2";
    private static final WDT wdt = new WDT();
    private static final ADT adt = new ADT();
    private static final WMO wmo = new WMO();
    private static final M2 model = new M2();
    private static final Map<String, M2> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final int MAX_HEIGHT = Integer.MAX_VALUE;

    private static final int VIEWPORT_W = 1920;
    private static final int VIEWPORT_H = 1080;
    private static final double SCALE = 1;
    private static final double SCALE_STEP = 0.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 3;
    private static final double TRANSLATE_STEP = 5;
    private static PolygonMeshView view;
    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 1500;
    final Xform adtGroup = new Xform();
    final Xform wmoGroup = new Xform();
    private Timeline timeline;
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0/24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
        
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    
    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));                

        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            logger.error(ex.getMessage());
        }

        buildScene();
        buildCamera();
        buildAxes();
        buildBBOX();        
        
        Scene scene = new Scene(root, VIEWPORT_W, VIEWPORT_H, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        scene.getStylesheets().add("/styles/Styles.css");
        scene.getRoot().requestFocus();
        handleMouse(scene, root);
        handleKeyboard(scene, root);
        
        /**root.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            double scale = camera.getScaleX();
            double translateX = camera.getTranslateX();
            double translateY = camera.getTranslateY();
            switch (event.getCode()) {
                case SUBTRACT:
                    scale += SCALE_STEP;
                    if (scale > MAX_SCALE) {
                        scale = MAX_SCALE;
                    }
                    break;
                case ADD:
                    scale -= SCALE_STEP;
                    if (scale < MIN_SCALE) {
                        scale = MIN_SCALE;
                    }
                    break;
                case LEFT:
                    translateX -= TRANSLATE_STEP;
                    break;
                case RIGHT:
                    translateX += TRANSLATE_STEP;
                    break;
                case UP:
                    translateY -= TRANSLATE_STEP;
                    break;
                case DOWN:
                    translateY += TRANSLATE_STEP;
                    break;
            }
            camera.setScaleX(scale);
            camera.setScaleY(scale);
            camera.setTranslateX(translateX);
            camera.setTranslateY(translateY);
        });*/

        // Maximizing.
        stage.setMaximized(true);
        stage.setTitle("WMO Liquid Redering");

        stage.setScene(scene);
        stage.show();
        //System.exit(0);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void buildScene() {
        System.out.println("buildScene");
        root.getChildren().add(world);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);        
        cameraXform.ry.setAngle(320.0);
        cameraXform.rx.setAngle(40);
    }

    private void buildAxes() {
        System.out.println("buildAxes()");
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        int axisSize = 5;
        final Box xAxis = new Box(1000.0, axisSize, axisSize);
        final Box yAxis = new Box(axisSize, 1000.0, axisSize);
        final Box zAxis = new Box(axisSize, axisSize, 1000.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }
    
    private void buildBBOX() {
        System.out.println("buildBBOX");
        
                /**
         * adtBBOX: BoundingBox [minX:-533.3333129882812,
         * minY:61.29011535644531, minZ:-9066.6650390625,
         * width:533.3333129882812, height:215.6719512939453,
         * depth:533.3310546875, maxX:0.0, maxY:276.9620666503906,
         * maxZ:-8533.333984375] 
         */
                
        Translate zero = new Translate(533.3333333, -61.29011535644531, 9066.6650390625);
        //Translate zero = new Translate(0 ,0 ,0);
        Box adtBox = new Box(533.3333333, 215.6719512939453, 533.3310546875);      
        Translate posAdt = new Translate(adtBox.getWidth() / 2, adtBox.getHeight() / 2, adtBox.getDepth() / 2);
        adtBox.setCullFace(CullFace.NONE);        
        adtBox.setMaterial(new PhongMaterial(Color.YELLOW));
        adtBox.setDrawMode(DrawMode.LINE);
        /**
         * wmoBBOX: BoundingBox [minX:-504.27142333984375, minY:-72.3641357421875, minZ:-599.6694946289062, width:1059.9337158203125, height:348.9631042480469, depth:807.6498870849609, maxX:555.6622924804688, maxY:276.5989685058594, maxZ:207.9803924560547]
         */
        // wmoUpdatedBBOX: BoundingBox [minX:-25.04371223805697, minY:29.592880249023438, minZ:-9408.318259093612, width:1332.286647712974, height:348.9631042480469, depth:1291.8976414774643, maxX:1307.242935474917, maxY:378.5559844970703, maxZ:-8116.420617616148]
        //Box wmoBox = new Box(1332.286647712974, 348.9631042480469, 1291.8976414774643);        
        Box wmoBox = new Box(1059.9337158203125, 348.9631042480469, 807.6498870849609);
        wmoBox.setCullFace(CullFace.NONE);
        wmoBox.setMaterial(new PhongMaterial(Color.RED));
        wmoBox.setDrawMode(DrawMode.LINE);        
        adtGroup.getChildren().add(adtBox);        
        adtGroup.getTransforms().addAll(posAdt);
        wmoGroup.getChildren().add(wmoBox);                
        /** 0.0
        -141.5
        0.0*/
        Rotate ry = new Rotate(-141.5, Rotate.Y_AXIS);
        /**
         * 539.29296875
            101.95701599121094
        -8931.634765625
         */
        wmoGroup.getTransforms().addAll(new Translate(539.29296875, 101.95701599121094, -8931.634765625), zero);            
        Bounds adtBounds = adtGroup.localToScene(adtGroup.getBoundsInLocal());
        Bounds wmoBounds = wmoGroup.localToScene(wmoGroup.getBoundsInLocal());
        System.out.println(adtBounds);
        System.out.println(wmoBounds);
        System.out.println(adtBounds.intersects(wmoBounds));
        world.getChildren().addAll(adtGroup, wmoGroup);         
    }
    
    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX); 
                mouseDeltaY = (mousePosY - mouseOldY); 
                
                double modifier = 1.0;
                double modifierFactor = 0.1;
                
                if (me.isControlDown()) {
                    modifier = 0.1;
                } 
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }     
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*modifierFactor*modifier*2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*modifierFactor*modifier*2.0);  // -
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*modifierFactor*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*modifierFactor*modifier*0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*modifierFactor*modifier*0.3);  // -
                }
            }
        });
    }
    
    private void handleKeyboard(Scene scene, final Node root) {
        final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Duration currentTime;
                switch (event.getCode()) {
                    case Z:
                        if (event.isShiftDown()) {
                            cameraXform.ry.setAngle(0.0);
                            cameraXform.rx.setAngle(0.0);
                            camera.setTranslateZ(-300.0);
                        }   
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        break;
                    case X:
                        if (event.isControlDown()) {
                            if (axisGroup.isVisible()) {
                                System.out.println("setVisible(false)");
                                axisGroup.setVisible(false);
                            }
                            else {
                                System.out.println("setVisible(true)");
                                axisGroup.setVisible(true);
                            }
                        }   
                        break;
                    case S:
                        if (event.isControlDown()) {
                            if (adtGroup.isVisible()) {
                                adtGroup.setVisible(false);
                            }
                            else {
                                adtGroup.setVisible(true);
                            }
                        }   
                        break;
                    case SPACE:
                        if (timelinePlaying) {
                            timeline.pause();
                            timelinePlaying = false;
                        }
                        else {
                            timeline.play();
                            timelinePlaying = true;
                        }
                        break;
                    case UP:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0*CONTROL_MULTIPLIER);  
                        }  
                        else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0*ALT_MULTIPLIER);  
                        }
                        else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0*CONTROL_MULTIPLIER);  
                        }
                        else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0*ALT_MULTIPLIER);  
                        }
                        else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z + 5.0*SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case DOWN:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0*CONTROL_MULTIPLIER);  
                        }  
                        else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0*ALT_MULTIPLIER);  
                        }
                        else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0*CONTROL_MULTIPLIER);  
                        }
                        else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0*ALT_MULTIPLIER);  
                        }
                        else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z - 5.0*SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case RIGHT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0*CONTROL_MULTIPLIER);  
                        }  
                        else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0*ALT_MULTIPLIER);  
                        }
                        else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0*CONTROL_MULTIPLIER);  
                        }
                        else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0*ALT_MULTIPLIER);  
                        }
                        break;
                    case LEFT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0*CONTROL_MULTIPLIER);  
                        }  
                        else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0*ALT_MULTIPLIER);  // -
                        }
                        else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0*CONTROL_MULTIPLIER);  
                        }
                        else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0*ALT_MULTIPLIER);  // -
                        }
                        break;
                }
            }
        });
    }
}
