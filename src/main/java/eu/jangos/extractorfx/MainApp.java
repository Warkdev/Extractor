package eu.jangos.extractorfx;

import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.WMOGroup;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

public class MainApp extends Application {

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String ADT = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
    //private static final String ADT = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\terrain.MPQ";
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
    private static final String WMO = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\wmo.MPQ";
    String wmoExample = "World\\wmo\\Kalimdor\\CollidableDoodads\\Durotar\\StoneGate\\DurotarGate.wmo";
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\MD_Crypt\\MD_Crypt_D.wmo";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Buildings\\Prison_Camp\\PrisonOublietteLarge.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Collidable Doodads\\Elwynn\\AbbeyGate\\abbeygate01.wmo";
    private static final String m2Example = "world\\azeroth\\elwynn\\passivedoodads\\trees\\elwynntreecanopy04.M2";
    private static final String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
    private static final ADT adtReader = new ADT();
    private static final ADT2OBJConverter adtConverter = new ADT2OBJConverter(adtReader);
    private static final WMO wmo = new WMO();
    private static final ModelConverter wmoConverter = new WMO2OBJConverter(wmo);
    private static final M2 m2Reader = new M2();
    private static final ModelConverter m2Converter = new M22OBJConverter(m2Reader);
    private static final Map<String, ModelConverter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final File adtFile = new File(ADT);
    private static final File modelFile = new File(MODEL);
    private static final File wmoFile = new File(WMO);

    private static final int VIEWPORT_W = 1920;
    private static final int VIEWPORT_H = 1080;
    private static final double SCALE = 1;

    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));                

        Camera camera = new ParallelCamera();
        camera.setScaleX(SCALE);
        camera.setScaleY(SCALE);
        Line xLine = new Line(VIEWPORT_W / 2, 0, VIEWPORT_W / 2, VIEWPORT_H);
        xLine.setStroke(Color.RED);
        Line yLine = new Line(0, VIEWPORT_H / 2, VIEWPORT_W, VIEWPORT_H / 2);
        yLine.setStroke(Color.GREEN);
        Group root = new Group();

        root.getChildren().addAll(xLine, yLine);
        extractWmo(wmoExample, root, false, true);

        Scene scene = new Scene(root);
        scene.setCamera(camera);
        scene.getStylesheets().add("/styles/Styles.css");
        scene.getRoot().requestFocus();

        // Maximizing.
        stage.setMaximized(true);
        stage.setTitle("WMO Liquid Redering");

        stage.setScene(scene);
        stage.show();
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
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            int total = wmoEditor.getTotalFileCount();
            int done = 0;
            for (String path : wmoEditor.getFileNames()) {
                done++;
                if (!path.endsWith(".wmo")) {
                    continue;
                }
                byte[] data = wmoEditor.extractFileAsBytes(path);
                if (!wmo.isRootFile(data)) {
                    continue;
                }
                //System.out.println("Extracting WMO... " + path);
                String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
                wmo.init(data, path);
                ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
                if (saveToFile) {
                    wmoConverter.saveToFile(outputFile, false, false);
                }

                //System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractWmo(String path, Group root, boolean addModels, boolean saveToFile) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmo.init(wmoEditor.extractFileAsBytes(path), path);
            Group liquid = new Group();
            Point3D rotation = new Point3D(VIEWPORT_W / 2, VIEWPORT_H / 2, 0);
            ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
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
                    //double tileWidth = width / group.getLiquid().getxTiles();
                    //double tileHeight = height / group.getLiquid().getyTiles();
                    double tileWidth = 4.1666666666666666666666666;
                    double tileHeight = 4.166666666666666666666666;
                    for (int x = 0; x < group.getLiquid().getxTiles(); x++) {
                        for (int y = 0; y < group.getLiquid().getyTiles(); y++) {
                            Rectangle tile = new Rectangle(x * tileWidth + group.getLiquid().getBaseCoordinates().x, -(y * tileHeight + group.getLiquid().getBaseCoordinates().y), tileWidth, tileHeight);
                            tile.setFill(Color.TRANSPARENT);
                            //System.out.println(group.getLiquid().getxTiles() + " " + group.getLiquid().getyTiles() + " " +x+" "+y);
                            
                            if (group.getLiquid().hasNoLiquid(x, y)) {
                                tile.setStroke(Color.BLACK);
                            } else {
                                if (group.getLiquid().isMagma(x, y)) {
                                    tile.setStroke(Color.ORANGE);
                                } else {
                                    tile.setStroke(Color.BLUE);
                                }
                            }
                            liquid.getChildren().add(tile);
                        }
                    }
                } else {
                    coord = "[0,0]";
                }
                Text label = new Text(FilenameUtils.getName(group.getFilename()).split("_")[1].split("\\.")[0]);
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
                wmoConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            int total = modelEditor.getTotalFileCount();
            int done = 0;
            for (String file : modelEditor.getFileNames()) {
                if (file.endsWith(".m2")) {
                    System.out.println("Extracting M2... " + file);
                    String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(file) + ".obj";
                    m2Reader.init(modelEditor.extractFileAsBytes(file), file);
                    ((M22OBJConverter) m2Converter).convert(1, 100000);
                    if (saveToFile) {
                        m2Converter.saveToFile(outputFile, false, false);
                    }
                }
                done++;
                System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractM2(String path) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            m2Reader.init(modelEditor.extractFileAsBytes(path), path);
            ((M22OBJConverter) m2Converter).convert(1, 17);
            m2Converter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO) {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            for (String path : adtEditor.getFileNames()) {
                if (path.endsWith(".adt")) {
                    String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
                    adtReader.init(adtEditor.extractFileAsBytes(path), path);
                    adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
                }
            }
            //adtConverter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO) {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map.png";
            String outputDetailedLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map_details.png";
            adtReader.init(adtEditor.extractFileAsBytes(path), path);
            //adtReader.saveLiquidMap(outputLiquidMap, false);
            //adtReader.saveLiquidMap(outputDetailedLiquidMap, true);
            adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
            adtConverter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
