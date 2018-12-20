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

import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WDT;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.WMOGroup;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.obj.ADTConverter;
import eu.jangos.extractorfx.obj.M2Converter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMOConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
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
    private static final ADTConverter adtConverter = new ADTConverter(adt);
    private static final WMO wmo = new WMO();
    private static final WMOConverter wmoConverter = new WMOConverter(wmo);
    private static final M2 model = new M2();
    private static final ModelConverter m2Converter = new M2Converter(model);
    private static final Map<String, M2Converter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final int MAX_HEIGHT = Integer.MAX_VALUE;

    private static final int VIEWPORT_W = 1920;
    private static final int VIEWPORT_H = 1080;
    private static final double SCALE = 1;
    private static final double SCALE_STEP = 0.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 3;
    private static final double TRANSLATE_STEP = 5;
    private static Camera camera;

    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));                

        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            logger.error(ex.getMessage());
        }

        camera = new ParallelCamera();
        camera.setScaleX(SCALE);
        camera.setScaleY(SCALE);
        Line xLine = new Line(VIEWPORT_W / 2, 0, VIEWPORT_W / 2, VIEWPORT_H);
        xLine.setStroke(Color.RED);
        Line yLine = new Line(0, VIEWPORT_H / 2, VIEWPORT_W, VIEWPORT_H / 2);
        yLine.setStroke(Color.GREEN);
        Group root = new Group();

        /**root.getChildren().addAll(xLine, yLine);
        extractWmo(ironforge, root, false, true);   
        root.getChildren().clear();
        extractWmo(undercity, root, false, true);        
        root.getChildren().clear();
        extractWmo(stormwind, root, false, true);        
        root.getChildren().clear();
        extractWmo(cathedral, root, false, true);   */
        extractAllWMO(false, root, true);
        
        Scene scene = new Scene(root);
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

    private static void extractAllWMO(boolean addModels, Group root, boolean saveToFile) {
        int total = manager.getListWMO().size();
        int done = 0;
        for (String path : manager.getListWMO()) {
            done++;
            logger.info("Extracting WMO... " + path);
            extractWmo(path, root, addModels, saveToFile);
            logger.info("Done: " + done + " / Total: " + total);
            root.getChildren().clear();
        }
    }

    private static void extractWmo(String path, Group root, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + "_liquid.png";
            wmo.init(manager, path);
            Group liquid = new Group();
            wmoConverter.convert(manager, cache, path, addModels, MAX_HEIGHT);
            Rectangle rect = new Rectangle();
            double heightRoot = wmo.getBoundingBox().getMax().y - wmo.getBoundingBox().getMin().y;
            double widthRoot = wmo.getBoundingBox().getMax().x - wmo.getBoundingBox().getMin().x;
            rect.setX(wmo.getBoundingBox().getMin().x);
            rect.setY(-wmo.getBoundingBox().getMin().y - heightRoot);
            rect.setWidth(widthRoot);
            rect.setHeight(heightRoot);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.RED);
            liquid.getChildren().add(rect);

            for (WMOGroup group : wmo.getWmoGroupReadersList()) {
                StackPane pane = new StackPane();
                Rectangle r = new Rectangle();
                String coord;
                double height = group.getGroup().getBoundingBox().getMax().y - group.getGroup().getBoundingBox().getMin().y;
                double width = group.getGroup().getBoundingBox().getMax().x - group.getGroup().getBoundingBox().getMin().x;

                r.setX(group.getGroup().getBoundingBox().getMin().x);
                r.setY(-group.getGroup().getBoundingBox().getMin().y - height);
                r.setWidth(width);
                r.setHeight(height);
                r.setFill(Color.TRANSPARENT);

                if (group.hasLiquid()) {
                    coord = "[" + group.getLiquid().getxTiles() + "," + group.getLiquid().getyTiles() + "]";
                    double tileWidth = 4.1666666666666666666666666;
                    double tileHeight = 4.166666666666666666666666;
                    for (int x = 0; x < group.getLiquid().getxTiles(); x++) {
                        for (int y = 0; y < group.getLiquid().getyTiles(); y++) {
                            Rectangle tile = new Rectangle(x * tileWidth + group.getLiquid().getBaseCoordinates().x, -(y * tileHeight + group.getLiquid().getBaseCoordinates().y), tileWidth, tileHeight);
                            tile.setFill(Color.TRANSPARENT);

                            int flag = group.getLiquid().getFlags().get(group.getLiquid().getFlagPosition(x, y));
                            boolean render = true;
                            if (group.getLiquid().hasNoLiquid(x, y)) {                                                            
                                render = false;                                
                            } else {                                                                
                                if(group.getLiquid().isOverlap(x, y)) {
                                    tile.setFill(Color.RED);
                                } else {                                                        
                                    switch((flag & 0x08) >> 3) {
                                        case 0:
                                            tile.setFill(Color.BLUE);
                                            break;
                                        case 1:                                            
                                            tile.setFill(Color.YELLOW);
                                            break;
                                        case 2:
                                            tile.setFill(Color.PINK);
                                            break;
                                        case 3:
                                            tile.setFill(Color.PURPLE);
                                            break;
                                        default:
                                            tile.setFill(Color.DARKGOLDENROD);
                                            break;
                                    }                                        
                                }                               
                            }
                            if(render)
                                liquid.getChildren().add(tile);
                        }
                    }
                } else {
                    coord = "[0,0]";
                }
                String[] temp = FilenameUtils.getName(group.getFilename()).split("\\.")[0].split("_");
                Text label = new Text(temp[temp.length - 1]);
                //Text label = new Text();
                Text coordinates = new Text(coord);

                if (group.hasLiquid()) {
                    r.setStroke(Color.BLUE);
                    label.setStroke(Color.RED);
                    coordinates.setStroke(Color.BLUE);
                } else {
                    r.setStroke(Color.BLACK);
                }
                pane.setLayoutX(r.getX());
                pane.setLayoutY(r.getY());
                pane.getChildren().addAll(r, label);
                //pane.getChildren().addAll(r, label, coordinates);
                StackPane.setAlignment(label, Pos.TOP_LEFT);
                StackPane.setAlignment(coordinates, Pos.BOTTOM_RIGHT);
                root.getChildren().add(pane);
                //pane.getTransforms().addAll(new Rotate(180, VIEWPORT_W/2, VIEWPORT_H/2, 0, Rotate.X_AXIS), new Translate(VIEWPORT_W/2, VIEWPORT_H/2));                                
                pane.getTransforms().addAll(new Translate(VIEWPORT_W / 2, VIEWPORT_H / 2));
            }
            liquid.getTransforms().addAll(new Translate(VIEWPORT_W / 2, VIEWPORT_H / 2));
            root.getChildren().add(liquid);
            //wmoReader.getLiquidMap(false);
            if (saveToFile) {
                WritableImage image = new WritableImage(1920, 1080);
                SnapshotParameters params = new SnapshotParameters();
                params.setCamera(camera);
                root.snapshot(params, image);                                
                File file = new File(outputLiquidFile);
                if(!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                //wmoConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException ex) {
            logger.error("Error IO Exception");
        } catch (ConverterException ex) {
            logger.error("Error IO Exception");
        } catch (FileReaderException ex) {
            logger.error("Error IO Exception");
        } catch (MPQException ex) {
            logger.error("Error IO Exception");
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        int total = manager.getListM2().size();
        int done = 0;
        for (String path : manager.getListM2()) {
            logger.info("Extracting M2... " + path);
            done++;
            extractM2(path);

            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractM2(String path) {
        try {
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            model.init(manager, path);
            ((M2Converter) m2Converter).convert(1, 17);
            m2Converter.saveToFile(outputFile, false, false);
        } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, int modelMaxHeight, boolean addModelsInWMO, int wmoModelMaxHeight, boolean saveToFile) {
        for (String path : manager.getListADT()) {
            if (path.endsWith(".adt")) {
                extractMap(path, yUp, addWMO, addModels, modelMaxHeight, addModelsInWMO, wmoModelMaxHeight, saveToFile);
            }
        }
    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, int modelMaxHeight, boolean addModelsInWMO, int wmoModelMaxHeight, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map.png";
            String outputDetailedLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map_details.png";
            String outputHoleMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_hole_map.png";
            adt.init(manager, path);
            //adt.saveLiquidMap(outputDetailedLiquidMap, true);
            adtConverter.convert(manager, cache, path, yUp, addWMO, addModels, modelMaxHeight, addModelsInWMO, wmoModelMaxHeight);
            if (saveToFile) {
                adtConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
            logger.error(ex.getMessage());
        }
    }
}
