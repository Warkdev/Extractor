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

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.WMOFileReader;
import eu.jangos.extractor.file.WMOGroupFileReader;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractor.file.wmo.group.MOBA;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.JMpqException;
import systems.crigges.jmpq3.MpqFile;

/**
 *
 * @author Warkdev
 */
public class WMO2OBJConverter {

    private WMOFileReader reader;

    NumberFormat formatter = new DecimalFormat("000");
    
    List<RenderBatch> renderBatches;
    List<Vertex> verticeList;
    List<Integer> indiceList;
    Map<Integer, String> materials;

    public WMO2OBJConverter(WMOFileReader reader) {
        this.reader = reader;
        this.renderBatches = new ArrayList<>();
        this.verticeList = new ArrayList<>();
        this.indiceList = new ArrayList<>();
        this.materials = new HashMap<>();
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @throws ConverterException
     */
    public void convert(JMpqEditor mpqEditor, String wmoPath) throws ConverterException, JMpqException, WMOException {
        if (this.reader == null) {
            throw new ConverterException("M2FileReader is null");
        }

        this.renderBatches.clear();
        this.verticeList.clear();
        this.indiceList.clear();
        this.materials.clear();

        int offsetVertices = 0;

        for (int i = 0; i < this.reader.getnGroups(); i++) {
            String wmoGroupPath = FilenameUtils.removeExtension(wmoPath) + "_" + formatter.format(i) + ".wmo";
            if (mpqEditor.hasFile(wmoGroupPath)) {
                this.reader.initGroup(mpqEditor.extractFileAsBytes(wmoGroupPath));
                WMOGroupFileReader groupReader = this.reader.getWmoGroupReadersList().get(i);
                if (!groupReader.getVertexList().isEmpty()) {
                    // Wmo group file has vertices.

                    for (short index : groupReader.getIndexList()) {
                        indiceList.add(index + offsetVertices);
                    }

                    Vertex vertex;
                    int idx = 0;
                    for (Vec3f v : groupReader.getVertexList()) {
                        vertex = new Vertex();
                        vertex.setPosition(new Vec3f(v.x * -1, v.z, v.y));
                        vertex.setNormal(new Vec3f(groupReader.getNormalList().get(idx).x, groupReader.getNormalList().get(idx).z, groupReader.getNormalList().get(idx).y));
                        vertex.setTextCoord(new Vec2f(groupReader.getTextureVertexList().get(idx).x, groupReader.getTextureVertexList().get(idx).x));
                        verticeList.add(vertex);
                        idx++;
                        offsetVertices++;
                    }

                    for (MOBA moba : groupReader.getBatchList()) {
                        RenderBatch batch = new RenderBatch();
                        batch.setFirstFace(moba.getStartIndex());
                        batch.setNumFaces(moba.getCount());
                        batch.setMaterialID(moba.getPadding());
                        renderBatches.add(batch);
                    }
                }
            } else {
                System.out.println(wmoGroupPath + ": NOK");
            }
        }
    }

    /**
     * This methid returns the OBJ file as a String representation (including
     * carriage return).
     *
     * @return A String object representing the corresponding OBJ file
     * structure.
     */
    public String getOBJasAString() {
        StringBuilder sb = new StringBuilder();

        for (Vertex v : verticeList) {
            sb.append("v " + v.getPosition().x + " " + v.getPosition().y + " " + v.getPosition().z + "\n");
            sb.append("vt " + v.getTextCoord().x + " " + (-1) * v.getTextCoord().y + "\n");
            sb.append("vn " + v.getNormal().x + " " + v.getNormal().y + " " + v.getNormal().z + "\n");
        }

        for (RenderBatch batch : renderBatches) {
            int i = batch.getFirstFace();
            if (materials.containsKey(batch.getMaterialID())) {
                sb.append("usemtl " + materials.get(batch.getMaterialID()) + "\n");
                sb.append("s 1" + "\n");
            }

            while (i < batch.getFirstFace() + batch.getNumFaces()) {
                sb.append("f " + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1)
                        + " " + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1)
                        + " " + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "\n");
                i += 3;
            }
        }

        return sb.toString();
    }

    /**
     * This method is saving the OBJ file structure to the file given in
     * parameters.
     *
     * @param file The OBJ file (including path) where the structure needs to be
     * saved.
     * @throws ConverterException
     */
    public void saveToFile(String file) throws ConverterException, IOException {
        if (file == null || file.isEmpty()) {
            throw new ConverterException("Provided file is null or empty.");
        }

        File objFile = new File(file);
        if (objFile.exists()) {
            objFile.delete();
        } else {
            objFile.getParentFile().mkdirs();
        }

        OutputStreamWriter writer = new FileWriter(objFile);
        writer.write(getOBJasAString());
        writer.close();
    }

    public WMOFileReader getReader() {
        return reader;
    }

    public void setReader(WMOFileReader reader) {
        this.reader = reader;
    }

    public List<RenderBatch> getRenderBatches() {
        return renderBatches;
    }

    public void setRenderBatches(List<RenderBatch> renderBatches) {
        this.renderBatches = renderBatches;
    }

    public List<Vertex> getVerticeList() {
        return verticeList;
    }

    public void setVerticeList(List<Vertex> verticeList) {
        this.verticeList = verticeList;
    }

    public List<Integer> getIndiceList() {
        return indiceList;
    }

    public void setIndiceList(List<Integer> indiceList) {
        this.indiceList = indiceList;
    }

    public Map<Integer, String> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<Integer, String> materials) {
        this.materials = materials;
    }

}
