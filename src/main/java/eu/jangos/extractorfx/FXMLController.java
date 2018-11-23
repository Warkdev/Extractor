package eu.jangos.extractorfx;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.ADTFileReader;
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.mangos.shared.flags.FlagUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableFloatArray;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

public class FXMLController implements Initializable {

    @FXML
    private Group meshGroup;

    private MeshView terrain = new MeshView();
    private PerspectiveCamera camera;
    private double x, y;

    private static final int VIEWPORT_SIZE = 1920;
    private static final double MODEL_SCALE_FACTOR = 15;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE * 21;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String PATH = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
        String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
        String obj = "D:\\Downloads\\Test\\Azeroth_32_48.obj";
        String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
        ADTFileReader adt = new ADTFileReader();
        M2FileReader m2 = new M2FileReader();
        File mpq = new File(PATH);
        File modelFile = new File(MODEL);
        File objFile = new File(obj);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            adt.init(editor.extractFileAsBytes(map));

            // Now, exporting data to OBJ. Hard work starts.
            List<RenderBatch> renderBatches = new ArrayList<>();
            List<Vertex> verticeList = new ArrayList<>();
            List<Integer> indiceList = new ArrayList<>();
            Map<Integer, String> materials = new HashMap<>();

            float initialChunkX = adt.getListMapChunks().get(0).getPosX();
            float initialChunkY = adt.getListMapChunks().get(0).getPosY();
            int index = 0;

            for (MCNK chunk : adt.getListMapChunks()) {
                int offset = verticeList.size();
                RenderBatch batch = new RenderBatch();

                for (int i = 0, idx = 0; i < 17; i++) {
                    for (int j = 0; j < (((i % 2) != 0) ? 8 : 9); j++) {
                        Vertex vertex = new Vertex();
                        // Calculating Normals.
                        vertex.setNormal(new Vec3f(chunk.getNormals().getPoints()[idx].getX() / 127.0f, chunk.getNormals().getPoints()[idx].getY() / 127.0f, chunk.getNormals().getPoints()[idx].getZ() / 127.0f));
                        float x, y, z;
                        // Calculating Positions.
                        x = chunk.getPosY() - (j * ADTFileReader.UNIT_SIZE);
                        y = chunk.getVertices().getPoints()[idx++] + chunk.getPosZ();
                        z = chunk.getPosX() - (i * ADTFileReader.UNIT_SIZE * 0.5f);
                        if ((i % 2) != 0) {
                            x -= 0.5f * ADTFileReader.UNIT_SIZE;
                        }
                        vertex.setPosition(new Vec3f(x, y, z));
                        // Calculating TexCoord in high resolution.
                        x = ((chunk.getPosX() - initialChunkX) * (-1)) / ADTFileReader.CHUNK_SIZE;
                        y = (chunk.getPosZ() - initialChunkY) * (-1) / ADTFileReader.CHUNK_SIZE;
                        vertex.setTextCoord(new Vec2f(x, y));
                        verticeList.add(vertex);
                    }
                }                
                
                batch.setFirstFace(indiceList.size());
                boolean isHole;
                for (int j = 9, xx = 0, yy = 0; j < 145; j++, xx++) {
                    if (xx >= 8) {
                        xx = 0;
                        ++yy;
                    }
                    isHole = true;

                    // Low res hole ?!                    
                    if (!FlagUtils.hasFlag(chunk.getFlags(), 0x10000)) {
                        // Calculate current hole number.
                        int currentHole = (int) Math.pow(2, Math.floor(xx / 2f) * 1f + Math.floor(yy / 2f) * 4f);
                        if (!FlagUtils.hasFlag(chunk.getHoles(), currentHole)) {
                            isHole = false;
                        }
                    } else {
                        // Should never be used in Vanilla.
                        System.out.println("Hello world");
                    }

                    if (!isHole) {
                        // Adding triangles
                        indiceList.add(offset + j + 8);
                        indiceList.add(offset + j - 9);
                        indiceList.add(offset + j);

                        indiceList.add(offset + j - 9);
                        indiceList.add(offset + j - 8);
                        indiceList.add(offset + j);

                        indiceList.add(offset + j - 8);
                        indiceList.add(offset + j + 9);
                        indiceList.add(offset + j);

                        indiceList.add(offset + j + 9);
                        indiceList.add(offset + j + 8);
                        indiceList.add(offset + j);
                    }

                    if ((j + 1) % (9 + 8) == 0) {
                        j += 9;
                    }
                }

                materials.put(index + 1, "test_" + index);
                batch.setMaterialID(index + 1);

                batch.setNumFaces(indiceList.size() - batch.getFirstFace());

                renderBatches.add(batch);
                index++;
            }

            // Now, the truth.
            if (objFile.exists()) {
                objFile.delete();
            }
            OutputStreamWriter writer = new FileWriter(objFile);            
            TriangleMesh mesh = new TriangleMesh();
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
            terrain.setMesh(mesh);

            for (Vertex v : verticeList) {
                mesh.getPoints().addAll(v.getPosition().x, (-1) * v.getPosition().y, v.getPosition().z * (-1));
                mesh.getTexCoords().addAll(v.getTextCoord().x, (-1) * v.getTextCoord().y);
                mesh.getNormals().addAll(v.getNormal().x, v.getNormal().y, v.getNormal().z);
                writer.write("v " + v.getPosition().x + " " + v.getPosition().y + " " + v.getPosition().z + "\n");
                writer.write("vt " + v.getTextCoord().x + " " + (-1) * v.getTextCoord().y + "\n");
                writer.write("vn " + v.getNormal().x + " " + v.getNormal().y + " " + v.getNormal().z + "\n");
            }

            for (RenderBatch batch : renderBatches) {
                int i = batch.getFirstFace();
                if (materials.containsKey(batch.getMaterialID())) {                    
                    writer.write("usemtl " + materials.get(batch.getMaterialID()) + "\n");
                    writer.write("s 1" + "\n");
                }

                // There's one iteration too much.
                while (i < batch.getFirstFace() + batch.getNumFaces()) {         
                    mesh.getFaceSmoothingGroups().addAll(1);
                    mesh.getFaces().addAll(
                            (indiceList.get(i + 2)), (indiceList.get(i + 2)), (indiceList.get(i + 2)),
                            (indiceList.get(i + 1)),  (indiceList.get(i + 1)), (indiceList.get(i + 1)),
                            (indiceList.get(i)), (indiceList.get(i)), (indiceList.get(i)));
                    writer.write("f " + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "/"
                            + (indiceList.get(i + 2) + 1) + " " + (indiceList.get(i + 1) + 1) + "/"
                            + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) + " "
                            + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "\n");
                    i += 3;
                }
            }

            validateFaces(mesh);

            // Exporting M2.
            for (MDDF doodad : adt.getListMDDF()) {
                String mdx = FilenameUtils.removeExtension(adt.getListDoodad().get(doodad.getMmidEntry())) + ".m2";
                File mdxFile = new File("D:\\Downloads\\Test\\" + mdx);
                if (!mdxFile.exists() && modelEditor.hasFile(mdx)) {
                    m2.init(modelEditor.extractFileAsBytes(mdx));
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (M2Exception ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        initCamera();
        Platform.runLater(() -> showFigure());
    }

    @FXML
    private void onMouseDragged(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Detected: "+ event.getX());      
        terrain.startFullDrag();
        this.x = event.getX();
        this.y = event.getY();
    }
    
    @FXML
    private void onDragDone(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Done: "+event.getX());        
    }
    
    @FXML
    private void onDragDropped(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Dropped: "+event.getX());
    }
    
    @FXML
    private void onDragEntered(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Entered: "+event.getX());
    }
    
    @FXML
    private void onDragExited(Event e) {
        MouseEvent event = (MouseEvent) e;
        //System.out.println("Drag Exited: "+event.getX());
    }
    
    @FXML
    private void onDragOver(Event e) {
        MouseEvent event = (MouseEvent) e;
        // Moving camera.        
        this.terrain.setRotationAxis(Rotate.Z_AXIS);
        this.terrain.setRotate(event.getY());        
        this.terrain.setRotationAxis(Rotate.X_AXIS);
        
        this.terrain.setRotate(event.getX());
    }
    
    private void initCamera() {
        this.camera = new PerspectiveCamera(false);
        this.camera.setNearClip(0.1);
        this.camera.setFarClip(10000.0);
        this.camera.setTranslateZ(-1000);
        
        final Rotate cameraRotateX = new Rotate(0, new Point3D(1, 0, 0));
        final Rotate cameraRotateY = new Rotate(0, new Point3D(0, 1, 0));
        final Translate cameraTranslate = new Translate(0, 0, -1000);
        camera.getTransforms().addAll(cameraRotateX, cameraRotateY, cameraTranslate);
    }

    private void showFigure() {
        // Add MeshView to Group
        Group meshInGroup = buildScene();
        // Create SubScene
        SubScene subScene = createScene3D(meshInGroup);
        // Add subScene to meshGroup
        this.meshGroup.getChildren().add(subScene);

        //RotateTransition rotate = rotate3dGroup(meshInGroup);        
    }

    private Group buildScene() {
        Group group = new Group();        
        //terrain.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
        //terrain.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
        //terrain.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
        terrain.setScaleX(MODEL_SCALE_FACTOR);
        terrain.setScaleY(MODEL_SCALE_FACTOR);
        terrain.setScaleZ(MODEL_SCALE_FACTOR);
        
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(VIEWPORT_SIZE*5);
        pointLight.setTranslateY(VIEWPORT_SIZE*5);
        pointLight.setTranslateX(VIEWPORT_SIZE*5);
        //pointLight.setTrasetTranslateZ(VIEWPORT_SIZE);
        //pointLight.setTranslateY(VIEWPORT_SIZE);

        group.getChildren().addAll(terrain, pointLight);
        return group;
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene3d.setFill(Color.WHITE);
        scene3d.setCamera(this.camera);
        scene3d.setPickOnBounds(true);
        return scene3d;
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

    private void validateFaces(TriangleMesh mesh) {
        ObservableFaceArray faces = mesh.getFaces();
        ObservableFloatArray points = mesh.getPoints();
        ObservableFloatArray normals = mesh.getNormals();
        ObservableFloatArray texCoords = mesh.getTexCoords();

        int nVerts = points.size() / mesh.getPointElementSize();
        int nNVerts = normals.size() / mesh.getNormalElementSize();
        int nTVerts = texCoords.size() / mesh.getTexCoordElementSize();
        for (int i = 0; i < faces.size(); i += 3) {
            if ((faces.get(i) >= nVerts || faces.get(i) < 0)) {
                System.out.println("error with nVerts "+nVerts+", "+faces.get(i));
            }
            if (faces.get(i + 1) >= nNVerts || faces.get(i + 1) < 0) {
                System.out.println("error with nNVerts "+nNVerts+", "+faces.get(i+1));
            }
            if (faces.get(i + 2) >= nTVerts || faces.get(i + 2) < 0) {
                System.out.println("error with nTVerts "+ nTVerts+", "+faces.get(i+2));
            }            
        }
    }
}
