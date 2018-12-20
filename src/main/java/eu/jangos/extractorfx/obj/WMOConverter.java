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
package eu.jangos.extractorfx.obj;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.WMOGroup;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractor.file.wmo.WMODoodadDef;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import javafx.geometry.Point3D;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.crigges.jmpq3.JMpqException;

/**
 *
 * @author Warkdev
 */
public class WMOConverter extends ModelConverter {

    private static final Logger logger = LoggerFactory.getLogger(WMOConverter.class);

    private WMO reader;    

    public WMOConverter(WMO reader) {
        super();
        this.reader = reader;
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @throws ConverterException
     */
    public void convert(MPQManager manager, Map<String, M2Converter> cache, String wmoPath, boolean addModels, int modelMaxHeight) throws ConverterException, JMpqException, WMOException, MPQException {
        if (this.reader == null) {
            logger.error("File read is not set.");
            throw new ConverterException("WMOFileReader is null");
        }

        clearMesh();

        int offsetVertices = 0;

        for (int i = 0; i < this.reader.getnGroups(); i++) {

            WMOGroup wmoGroup = this.reader.getWmoGroupReadersList().get(i);
            if (!wmoGroup.getVertexList().isEmpty()) {
                // Wmo group file has vertices.

                for (int face = 0; face < wmoGroup.getIndexList().size(); face += 3) {
                    mesh.getFaces().addAll(
                            (wmoGroup.getIndexList().get(face + 2)) + offsetVertices, (wmoGroup.getIndexList().get(face + 2)) + offsetVertices, (wmoGroup.getIndexList().get(face + 2)) + offsetVertices,
                            (wmoGroup.getIndexList().get(face + 1)) + offsetVertices, (wmoGroup.getIndexList().get(face + 1)) + offsetVertices, (wmoGroup.getIndexList().get(face + 1)) + offsetVertices,
                            (wmoGroup.getIndexList().get(face)) + offsetVertices, (wmoGroup.getIndexList().get(face)) + offsetVertices, (wmoGroup.getIndexList().get(face)) + offsetVertices);
                }

                int idx = 0;
                for (Vec3f v : wmoGroup.getVertexList()) {
                    mesh.getPoints().addAll(v.x, v.y, v.z);
                    mesh.getNormals().addAll(wmoGroup.getNormalList().get(idx).x, wmoGroup.getNormalList().get(idx).y, wmoGroup.getNormalList().get(idx).z);
                    mesh.getTexCoords().addAll(wmoGroup.getTextureVertexList().get(idx).x, wmoGroup.getTextureVertexList().get(idx).y);
                    idx++;
                    offsetVertices++;
                }

            }
        }

        if (addModels) {
            M2 model;
            M2Converter converter;
            // Now we add models.
            for (WMODoodadDef modelInstance : reader.getDoodadDefList()) {

                if (reader.getDoodadNameMap().containsKey(modelInstance.getNameIndex())) {
                    // MDX model files are stored as M2 in the MPQ. God knows why.
                    String modelFile = FilenameUtils.removeExtension(reader.getDoodadNameMap().get(modelInstance.getNameIndex())) + ".M2";
                    if (!manager.getMPQForFile(modelFile).hasFile(modelFile)) {
                        logger.warn("Oooops, m2Editor doesn't have the file: " + modelFile);
                        continue;
                    }

                    try {
                        // First, check if the M2 is in cache. Must be much faster than parsing it again and again.
                        if (cache.containsKey(modelFile)) {
                            converter = cache.get(modelFile);
                        } else {
                            model = new M2();
                            converter = new M2Converter(model);
                            model.init(manager, modelFile);
                            ((M2Converter) converter).convert(1, modelMaxHeight);
                            cache.put(modelFile, converter);
                        }

                        // Now, we have the vertices of this M2, we need to scale, rotate & position.                                                                                
                        // First, we create a view to apply these transformations.
                        MeshView view = new MeshView(converter.getMesh());

                        // We translate the object location.
                        Translate translate = new Translate(modelInstance.getPosition().x, modelInstance.getPosition().y, modelInstance.getPosition().z);

                        // We convert the quaternion to a Rotate object with angle (in degrees) & pivot point.
                        Rotate rotate = getAngleAndAxis(modelInstance.getOrientation());

                        // We scale.
                        Scale scale = new Scale(modelInstance.getScale(), modelInstance.getScale(), modelInstance.getScale());

                        // We add all transformations to the view and we get back the transformation matrix.
                        view.getTransforms().addAll(translate, rotate, scale);
                        Transform concat = view.getLocalToSceneTransform();

                        // We apply the transformation matrix to all points of the mesh.
                        TriangleMesh temp = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
                        for (int i = 0; i < converter.getMesh().getPoints().size(); i += 3) {
                            Point3D point = new Point3D(converter.getMesh().getPoints().get(i), converter.getMesh().getPoints().get(i + 1), converter.getMesh().getPoints().get(i + 2));
                            point = concat.transform(point);
                            temp.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
                        }

                        int offset = this.mesh.getPoints().size() / 3;

                        // Then, we add the converted model mesh to the WMO mesh.
                        this.mesh.getPoints().addAll(temp.getPoints());
                        this.mesh.getNormals().addAll(converter.mesh.getNormals());
                        this.mesh.getTexCoords().addAll(converter.mesh.getTexCoords());

                        // And we recalculate the faces of the model mesh.
                        for (int i = 0; i < converter.mesh.getFaces().size(); i++) {
                            this.mesh.getFaces().addAll(converter.getMesh().getFaces().get(i) + offset);
                        }

                    } catch (IOException ex) {
                        logger.error("Error while adding a new model to this WMO: " + modelFile);
                    } catch (FileReaderException ex) {
                        logger.error("Error while adding a new model to this WMO: " + modelFile);
                    }
                } else {
                    logger.warn(this.reader.getFilename() + " - Oooops, offset for MDX is not found! " + modelInstance.getNameIndex());
                }
            }
        }
    }

    public WMO getReader() {
        return reader;
    }

    public void setReader(WMO reader) {
        this.reader = reader;
    }

}
