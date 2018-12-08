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
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.WMOFileReader;
import eu.jangos.extractor.file.WMOGroupFileReader;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.wmo.WMODoodadDef;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Point3D;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.JMpqException;

/**
 *
 * @author Warkdev
 */
public class WMO2OBJConverter extends ModelConverter {

    private WMOFileReader reader;

    NumberFormat formatter = new DecimalFormat("000");

    public WMO2OBJConverter(WMOFileReader reader) {
        super();
        this.reader = reader;
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @throws ConverterException
     */
    public void convert(JMpqEditor wmoEditor, JMpqEditor m2Editor, Map<String, ModelConverter> cache, String wmoPath, boolean addModels) throws ConverterException, JMpqException, WMOException {
        if (this.reader == null) {
            throw new ConverterException("WMOFileReader is null");
        }

        this.mesh.getPoints().clear();
        this.mesh.getNormals().clear();
        this.mesh.getTexCoords().clear();
        this.mesh.getFaces().clear();

        int offsetVertices = 0;

        for (int i = 0; i < this.reader.getnGroups(); i++) {
            String wmoGroupPath = FilenameUtils.removeExtension(wmoPath) + "_" + formatter.format(i) + ".wmo";
            if (wmoEditor.hasFile(wmoGroupPath)) {
                this.reader.initGroup(wmoEditor.extractFileAsBytes(wmoGroupPath), wmoGroupPath);
                WMOGroupFileReader groupReader = this.reader.getWmoGroupReadersList().get(i);
                if (!groupReader.getVertexList().isEmpty()) {
                    // Wmo group file has vertices.

                    for (int face = 0; face < groupReader.getIndexList().size(); face += 3) {
                        mesh.getFaces().addAll(
                                (groupReader.getIndexList().get(face + 2)) + offsetVertices, (groupReader.getIndexList().get(face + 2)) + offsetVertices, (groupReader.getIndexList().get(face + 2)) + offsetVertices,
                                (groupReader.getIndexList().get(face + 1)) + offsetVertices, (groupReader.getIndexList().get(face + 1)) + offsetVertices, (groupReader.getIndexList().get(face + 1)) + offsetVertices,
                                (groupReader.getIndexList().get(face)) + offsetVertices, (groupReader.getIndexList().get(face)) + offsetVertices, (groupReader.getIndexList().get(face)) + offsetVertices);
                    }

                    int idx = 0;
                    for (Vec3f v : groupReader.getVertexList()) {
                        mesh.getPoints().addAll(v.x, v.y, v.z);
                        mesh.getNormals().addAll(groupReader.getNormalList().get(idx).x, groupReader.getNormalList().get(idx).y, groupReader.getNormalList().get(idx).z);
                        mesh.getTexCoords().addAll(groupReader.getTextureVertexList().get(idx).x, groupReader.getTextureVertexList().get(idx).y);
                        idx++;
                        offsetVertices++;
                    }

                }
            } else {
                System.out.println(wmoGroupPath + ": NOK");
            }
        }

        if (addModels) {
            M2FileReader m2Reader;
            ModelConverter m2Converter;
            // Now we add models.
            for (WMODoodadDef modelInstance : reader.getDoodadDefList()) {

                if (reader.getDoodadNameMap().containsKey(modelInstance.getNameIndex())) {
                    // MDX model files are stored as M2 in the MPQ. God knows why.
                    String modelFile = FilenameUtils.removeExtension(reader.getDoodadNameMap().get(modelInstance.getNameIndex())) + ".M2";
                    if (!m2Editor.hasFile(modelFile)) {
                        System.out.println("Oooops, m2Editor doesn't have the file: " + modelFile);
                        continue;
                    }

                    try {
                        // First, check if the M2 is in cache. Must be much faster than parsing it again and again.
                        if (cache.containsKey(modelFile)) {
                            m2Converter = cache.get(modelFile);
                        } else {
                            m2Reader = new M2FileReader();
                            m2Converter = new M22OBJConverter(m2Reader);
                            m2Reader.init(m2Editor.extractFileAsBytes(modelFile));
                            m2Converter.convert();
                            cache.put(modelFile, m2Converter);
                        }

                        // Now, we have the vertices of this M2, we need to scale, rotate & position.                                                                                
                        // First, we create a view to apply these transformations.
                        MeshView view = new MeshView(m2Converter.getMesh());

                        // We translate the object location.
                        Translate translate = new Translate(modelInstance.getPosition().x, modelInstance.getPosition().y, modelInstance.getPosition().z);

                        // We convert the quaternion to a Rotate object with angle (in degrees) & pivot point.
                        Rotate rotate = getAngleAndPivot(modelInstance.getOrientation());

                        // We scale.
                        Scale scale = new Scale(modelInstance.getScale(), modelInstance.getScale(), modelInstance.getScale());

                        // We add all transformations to the view and we get back the transformation matrix.
                        view.getTransforms().addAll(translate, rotate, scale);
                        Transform concat = view.getLocalToSceneTransform();

                        // We apply the transformation matrix to all points of the mesh.
                        TriangleMesh temp = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
                        for (int i = 0; i < m2Converter.getMesh().getPoints().size(); i += 3) {
                            Point3D point = new Point3D(m2Converter.getMesh().getPoints().get(i), m2Converter.getMesh().getPoints().get(i + 1), m2Converter.getMesh().getPoints().get(i + 2));
                            point = concat.transform(point);
                            temp.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
                        }

                        int offset = this.mesh.getPoints().size() / 3;

                        // Then, we add the converted model mesh to the WMO mesh.
                        this.mesh.getPoints().addAll(temp.getPoints());
                        this.mesh.getNormals().addAll(m2Converter.mesh.getNormals());
                        this.mesh.getTexCoords().addAll(m2Converter.mesh.getTexCoords());

                        // And we recalculate the faces of the model mesh.
                        for (int i = 0; i < m2Converter.mesh.getFaces().size(); i++) {
                            this.mesh.getFaces().addAll(m2Converter.getMesh().getFaces().get(i) + offset);
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(WMO2OBJConverter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (M2Exception ex) {
                        Logger.getLogger(WMO2OBJConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    System.out.println(this.reader.getFileName() + " - Oooops, offset for MDX is not found! " + modelInstance.getNameIndex());
                }
            }
        }
    }

    public WMOFileReader getReader() {
        return reader;
    }

    public void setReader(WMOFileReader reader) {
        this.reader = reader;
    }

    @Override
    public void convert() throws ConverterException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
