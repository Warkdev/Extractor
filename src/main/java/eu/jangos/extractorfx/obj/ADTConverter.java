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

import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.common.MapUnit;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.IOException;
import java.util.List;
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
 * ADT2OBJConverter will take an ADT file in input and create all necessary
 * assets to generate a standard an valid OBJ Wavefront fileformat. It includes
 * vertices, indices, materials and faces. After having generated a new
 * ADT2OBJConverter, please use the method "convert()". Without this call, all
 * list will remain empty.
 *
 * @see https://en.wikipedia.org/wiki/Wavefront_.obj_file
 * @author Warkdev
 */
public class ADTConverter extends ModelConverter {

    private static final Logger logger = LoggerFactory.getLogger(ADTConverter.class);
    
    // The reader will hold ADT information.
    private ADT reader;

    /**
     * Creates an ADT2OBJConverter file.
     *
     * @param reader The ADTFileReader from which you're expecting conversion.
     */
    public ADTConverter(ADT reader) {
        super();
        this.reader = reader;
    }

    /**
     * Convert the actual ADT File to expected OBJ informations (vertices,
     * batches, indices & materials).
     *
     * @throws ConverterException If there's any validation error while
     * converting the ADT to OBJ.
     */
    public void convert(MPQManager manager, Map<String, M2Converter> cache, String adtPath, boolean yUp, boolean addWMO, boolean addModels, int modelMaxHeight, boolean addModelsInWMO, int modelInWMOMaxHeight) throws ConverterException, FileReaderException, MPQException {
        if (this.reader == null) {
            throw new ConverterException("ADTFileReader is null");
        }

        clearMesh();

        List<MCNK> mapChunks;
        try {
            mapChunks = reader.getMapChunks();
        } catch (ADTException exception) {
            throw new ConverterException(exception.getMessage());
        }

        float initialChunkX = mapChunks.get(0).getPosition().x;
        float initialChunkY = mapChunks.get(0).getPosition().y;

        for (MCNK chunk : mapChunks) {
            int offset = this.mesh.getPoints().size() / 3;

            for (int i = 0, idx = 0; i < 17; i++) {
                for (int j = 0; j < (((i % 2) != 0) ? 8 : 9); j++, idx++) {
                    // Calculating Normals.                    
                    this.mesh.getNormals().addAll(chunk.getNormals().getPoints()[idx].getX() / 127.0f, chunk.getNormals().getPoints()[idx].getY() / 127.0f, chunk.getNormals().getPoints()[idx].getZ() / 127.0f);
                    float x, y, z;
                    // Calculating Positions.                    
                    x = chunk.getPosition().x - (i * MapUnit.UNIT_SIZE * 0.5f);
                    y = chunk.getPosition().y - (j * MapUnit.UNIT_SIZE);
                    z = chunk.getVertices().getPoints()[idx] + chunk.getPosition().z;                    
                    if ((i % 2) != 0) {
                        y -= 0.5f * MapUnit.UNIT_SIZE;
                    }
                    this.mesh.getPoints().addAll(x, y, z);
                    // Calculating TexCoord in high resolution.
                    x = ((chunk.getPosition().x - initialChunkX) * (-1)) / MapUnit.CHUNK_SIZE;
                    y = (chunk.getPosition().y - initialChunkY) * (-1) / MapUnit.CHUNK_SIZE;
                    this.mesh.getTexCoords().addAll(x, y);
                }
            }

            for (int j = 9, x = 0, y = 0; j < 145; j++, x++) {
                if (x >= 8) {
                    x = 0;
                    y++;
                }
                
                if (!chunk.isHole(x, y)) {
                    // Adding triangles
                    this.mesh.getFaces().addAll(offset + j + 8, offset + j + 8, offset + j + 8);
                    this.mesh.getFaces().addAll(offset + j - 9, offset + j - 9, offset + j - 9);
                    this.mesh.getFaces().addAll(offset + j, offset + j, offset + j);
                    this.mesh.getFaces().addAll(offset + j - 9, offset + j - 9, offset + j - 9);
                    this.mesh.getFaces().addAll(offset + j - 8, offset + j - 8, offset + j - 8);
                    this.mesh.getFaces().addAll(offset + j, offset + j, offset + j);
                    this.mesh.getFaces().addAll(offset + j - 8, offset + j - 8, offset + j - 8);
                    this.mesh.getFaces().addAll(offset + j + 9, offset + j + 9, offset + j + 9);
                    this.mesh.getFaces().addAll(offset + j, offset + j, offset + j);
                    this.mesh.getFaces().addAll(offset + j + 9, offset + j + 9, offset + j + 9);
                    this.mesh.getFaces().addAll(offset + j + 8, offset + j + 8, offset + j + 8);
                    this.mesh.getFaces().addAll(offset + j, offset + j, offset + j);
                }

                if ((j + 1) % (9 + 8) == 0) {
                    j += 9;
                }
            }
        }

        if (addModels) {
            M2 model;
            M2Converter converter;
            // Now we add models.        
            int idx = 0;
            for (MDDF modelPlacement : reader.getDoodadPlacement()) {
                // MDX model files are stored as M2 in the MPQ. God knows why.
                String modelFile = FilenameUtils.removeExtension(reader.getModels().get(modelPlacement.getMmidEntry())) + ".M2";
                if (!manager.getMPQForFile(modelFile).hasFile(modelFile)) {
                    logger.debug("Oooops, no MPQ found for this file: " + modelFile);
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
                        converter.convert(1, modelMaxHeight);
                        cache.put(modelFile, converter);
                    }

                    // Now, we have the vertices of this M2, we need to scale, rotate & position.                                                                                
                    // First, we create a view to apply these transformations.
                    MeshView view = new MeshView(converter.getMesh());

                    // We translate the object location.                
                    Translate translate = new Translate(17066 - modelPlacement.getPosition().z, 17066 - modelPlacement.getPosition().x, modelPlacement.getPosition().y);

                    // We convert the euler angles to a Rotate object with angle (in degrees) & pivot point.                
                    Rotate rx = new Rotate(modelPlacement.getOrientation().y, Rotate.Z_AXIS);
                    Rotate ry = new Rotate(modelPlacement.getOrientation().z, Rotate.X_AXIS);
                    Rotate rz = new Rotate(modelPlacement.getOrientation().x - 180, Rotate.Z_AXIS);

                    // We scale.
                    double scaleFactor = modelPlacement.getScale() / 1024d;
                    Scale scale = new Scale(scaleFactor, scaleFactor, scaleFactor);

                    // We add all transformations to the view and we get back the transformation matrix.
                    view.getTransforms().addAll(translate, rx, ry, rz, scale);
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

                } catch (JMpqException | FileReaderException ex) {
                    logger.error("An error occured while reading the MPQ when importing Models.");
                } catch (IOException ex) {
                    logger.error("An error occured while reading the MPQ when importing Models.");
                }
            }
        }
        if (addWMO) {
            WMO wmo;
            WMOConverter converter;
            // Now we add wmo.        

            for (MODF modelPlacement : reader.getWorldObjectsPlacement()) {
                String wmoFile = FilenameUtils.removeExtension(reader.getWorldObjects().get(modelPlacement.getMwidEntry())) + ".WMO";
                if (!manager.getMPQForFile(wmoFile).hasFile(wmoFile)) {
                    logger.debug("Oooops, no MPQ found for this file: " + wmoFile);
                    continue;
                }

                try {
                    wmo = new WMO();
                    converter = new WMOConverter(wmo);
                    wmo.init(manager, wmoFile);
                    converter.convert(manager, cache, wmoFile, addModelsInWMO, modelInWMOMaxHeight);

                    // Now, we have the vertices of this WMO, we need to rotate & position.                                                                                
                    // First, we create a view to apply these transformations.
                    MeshView view = new MeshView(converter.getMesh());

                    // We translate the object location.                
                    Translate translate = new Translate(17066 - modelPlacement.getPosition().z, 17066 - modelPlacement.getPosition().x, modelPlacement.getPosition().y);

                    // We convert the euler angles to a Rotate object with euler angle and rotation ZXZ.                
                    Rotate rx = new Rotate(modelPlacement.getOrientation().y, Rotate.Z_AXIS);
                    Rotate ry = new Rotate(modelPlacement.getOrientation().z, Rotate.X_AXIS);
                    Rotate rz = new Rotate(modelPlacement.getOrientation().x - 180, Rotate.Z_AXIS);

                    // We add all transformations to the view and we get back the transformation matrix.
                    view.getTransforms().addAll(translate, rx, ry, rz);
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

                } catch (JMpqException | FileReaderException ex) {
                    logger.error("Error while reading MPQ to add WMO");
                }
            }
        }

        // Finally, we rotate if that's the desired output.
        if (yUp) {
            Rotate rx = new Rotate(-90, Rotate.X_AXIS);

            MeshView view = new MeshView(this.mesh);
            view.getTransforms().addAll(rx);
            Transform concat = view.getLocalToSceneTransform();

            for (int i = 0; i < this.mesh.getPoints().size(); i += 3) {
                Point3D point = new Point3D(this.mesh.getPoints().get(i), this.mesh.getPoints().get(i + 1), this.mesh.getPoints().get(i + 2));
                point = concat.transform(point);
                this.mesh.getPoints().set(i, (float) point.getX());
                this.mesh.getPoints().set(i + 1, (float) point.getY());
                this.mesh.getPoints().set(i + 2, (float) point.getZ());
            }
        }

    }

    private void printBounds(TriangleMesh mesh) {
        for (int j = 0; j < 3; j++) {
            double min = mesh.getPoints().get(j);
            double max = mesh.getPoints().get(j);
            for (int i = j; i < mesh.getPoints().size(); i += 3) {
                if (mesh.getPoints().get(i) < min) {
                    min = mesh.getPoints().get(i);
                } else if (mesh.getPoints().get(i) > max) {
                    max = mesh.getPoints().get(i);
                }
            }
            System.out.print("Min" + (j == 0 ? "X" : j == 1 ? "Y" : "Z") + " : " + min + " - ");
            System.out.println("Max" + (j == 0 ? "X" : j == 1 ? "Y" : "Z") + " : " + max);
        }
    }

    public ADT getReader() {
        return reader;
    }

    public void setReader(ADT reader) {
        this.reader = reader;
    }

}
