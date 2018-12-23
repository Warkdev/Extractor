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

import eu.jangos.extractor.file.impl.ADT;
import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractor.file.impl.WDT;
import eu.jangos.extractor.file.impl.WMO;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractor.file.ModelRenderer;
import eu.jangos.extractor.file.exception.ModelRendererException;
import eu.jangos.extractorfx.rendering.FileType3D;
import eu.jangos.extractorfx.rendering.PolygonMeshView;
import eu.jangos.extractorfx.rendering.Render3DType;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String DATA = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\";
    private static final String wdt1 = "world\\maps\\azeroth\\azeroth.wdt";
    private static final String wdt2 = "world\\maps\\kalimdor\\kalimdor.wdt";
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
    private static Camera camera;
    private static PolygonMeshView view;

    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));                

        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            logger.error(ex.getMessage());
        }

        camera = new PerspectiveCamera(false);
        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);
        camera.getTransforms().addAll(new Translate(-1000, -300, 0));
        //camera = new ParallelCamera();
        camera.setScaleX(SCALE);
        camera.setScaleY(SCALE);
        Line xLine = new Line(VIEWPORT_W / 2, 0, VIEWPORT_W / 2, VIEWPORT_H);
        xLine.setStroke(Color.RED);
        Line yLine = new Line(0, VIEWPORT_H / 2, VIEWPORT_W, VIEWPORT_H / 2);
        yLine.setStroke(Color.GREEN);
        Group root = new Group();
        view = new PolygonMeshView();
        view.setCullFace(CullFace.BACK);
        view.setDrawMode(DrawMode.FILL);
        view.setMaterial(new PhongMaterial(Color.RED));        
        
        //root.getChildren().addAll(xLine, yLine);
        root.getChildren().add(view);
        extractWmo(ironforge, false, false);                   
        //extractAllWMO(false, root, true);
        
        Scene scene = new Scene(root, VIEWPORT_W, VIEWPORT_H, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        scene.getStylesheets().add("/styles/Styles.css");
        scene.getRoot().requestFocus();

        root.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
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
        });

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

    private static void extractAllWMO(boolean addModels, boolean saveToFile) {
        int total = manager.getListWMO().size();
        int done = 0;
        for (String path : manager.getListWMO()) {
            done++;
            logger.info("Extracting WMO... " + path);
            extractWmo(path, addModels, saveToFile);
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractWmo(String path, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmo.init(manager, path);
            if (saveToFile) {
                if (wmo.save3D(outputFile, FileType3D.OBJ, Render3DType.MODEL, addModels)) {
                    logger.info("WMO file saved succesfully !");
                } else {
                    logger.error("WMO file not saved !");
                }
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        int total = manager.getListM2().size();
        int done = 0;
        for (String path : manager.getListM2()) {
            done++;
            logger.info("Extracting M2... " + path);
            extractM2(path, saveToFile);
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractM2(String path, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            model.init(manager, path);
            if (saveToFile) {
                model.save3D(outputFile, FileType3D.OBJ, Render3DType.MODEL, saveToFile);
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllTerrains() {

        int total = manager.getListWDT().size();
        int done = 0;
        for (String wdtFile : manager.getListWDT()) {
            done++;
            if (wdtFile.endsWith(".wdt")) {
                try {
                    logger.info("Extracting WDT... " + wdtFile);
                    String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
                    String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + ".png";
                    wdt.init(manager, wdtFile, true);
                    for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                        for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                            if (wdt.hasTerrain(x, y)) {
                                extractMap(base + "_" + y + "_" + x + ".adt", false, false, false, MAX_HEIGHT, false, MAX_HEIGHT, false);
                            }
                        }
                    }
                } catch (IOException | FileReaderException | MPQException ex) {
                    logger.error(ex.getMessage());
                }
                logger.info("Done: " + done + " / Total: " + total);
            }
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO, boolean saveToFile) {
        for (String path : manager.getListADT()) {
            extractMap(path, yUp, addWMO, addModels, MAX_HEIGHT, addModelsInWMO, MAX_HEIGHT, saveToFile);
        }

    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, int modelMaxHeight, boolean addModelsInWMO, int wmoModelMaxHeight, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            adt.init(manager, path);
            if (saveToFile) {
                adt.save3D(outputFile, FileType3D.OBJ, Render3DType.TERRAIN, false);
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllWdt() {
        for (String path : manager.getListWDT()) {
            extractWdt(path);
        }

    }

    private static void extractWdt(String wdtFile) {

        try {
            logger.info("Extracting WDT... " + wdtFile);
            String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + ".png";
            wdt.init(manager, wdtFile, true);
            for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                    if (wdt.hasTerrain(x, y)) {
                        extractMap(base + "_" + y + "_" + x + ".adt", false, false, false, MAX_HEIGHT, false, MAX_HEIGHT, false);
                    }
                }
            }
        } catch (IOException | FileReaderException | MPQException ex) {
            logger.error(ex.getMessage());
        }

    }
}
