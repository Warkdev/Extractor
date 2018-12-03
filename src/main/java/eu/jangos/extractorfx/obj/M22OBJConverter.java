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
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Warkdev
 */
public class M22OBJConverter {

    private M2FileReader reader;

    List<RenderBatch> renderBatches;
    List<Vertex> verticeList;
    List<Short> indiceList;
    Map<Integer, String> materials;

    public M22OBJConverter(M2FileReader reader) {
        this.reader = reader;
        this.renderBatches = new ArrayList<>();
        this.verticeList = new ArrayList<>();
        this.indiceList = new ArrayList<>();
        this.materials = new HashMap<>();
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @param view There are 4 views in the M2 file of the same model. Use
     * number 1 to 4 to extract the view you want.
     * @throws ConverterException
     */
    public void convert(int view) throws ConverterException {
        if (this.reader == null) {
            throw new ConverterException("M2FileReader is null");
        }

        if (view < 1 || view > 4) {
            throw new ConverterException("View number must be between 1 and 4");
        }
        view--;

        this.renderBatches.clear();
        this.verticeList.clear();
        this.indiceList.clear();
        this.materials.clear();

        RenderBatch batch = new RenderBatch();
        try {
            // Converting M2Vertex to Vertex.
            for (M2Vertex v : reader.getVertices()) {
                Vertex vertex = new Vertex();
                vertex.setPosition(new Vec3f(v.getPosition().x, v.getPosition().z, v.getPosition().y * (-1)));
                vertex.setNormal(new Vec3f(v.getNormal().x, v.getNormal().z, v.getNormal().y));
                vertex.setTextCoord(new Vec2f(v.getTexCoords()[0].x, v.getTexCoords()[0].y));

                this.verticeList.add(vertex);
            }

            this.indiceList = reader.getSkins().get(view).getIndices();

            for (int i = 0; i < reader.getSkins().get(view).getSubMeshes().size(); i++) {
                batch.setFirstFace(reader.getSkins().get(view).getSubMeshes().get(i).getIndexStart());
                batch.setNumFaces(reader.getSkins().get(view).getSubMeshes().get(i).getIndexCount());
                batch.setGroupID(i);
                for (int j = 0; j < reader.getSkins().get(view).getTextureUnit().size(); j++) {
                    if (reader.getSkins().get(view).getTextureUnit().get(j).getSkinSectionIndex() == i) {
                        batch.setBlendType(reader.getSkins().get(view).getTextureUnit().get(j).getMaterialIndex());
                        batch.setMaterialID(reader.getTextureLookup().get(reader.getSkins().get(view).getTextureUnit().get(j).getTextureComboIndex()));
                    }
                }

                renderBatches.add(batch);
            }
        } catch (M2Exception ex) {
            throw new ConverterException("Error while eading the M2 content " + ex.getMessage());
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

    public M2FileReader getReader() {
        return reader;
    }

    public void setReader(M2FileReader reader) {
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

    public List<Short> getIndiceList() {
        return indiceList;
    }

    public void setIndiceList(List<Short> indiceList) {
        this.indiceList = indiceList;
    }

    public Map<Integer, String> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<Integer, String> materials) {
        this.materials = materials;
    }

}
