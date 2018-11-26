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
import eu.jangos.extractor.file.ADTFileReader;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import eu.mangos.shared.flags.FlagUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ADT2OBJConverter {

    // The reader will hold ADT information.
    private ADTFileReader reader;

    List<RenderBatch> renderBatches;
    List<Vertex> verticeList;
    List<Integer> indiceList;
    Map<Integer, String> materials;

    /**
     * Creates an ADT2OBJConverter file.
     *
     * @param reader The ADTFileReader from which you're expecting conversion.
     */
    public ADT2OBJConverter(ADTFileReader reader) {
        this.reader = reader;
        this.renderBatches = new ArrayList<>();
        this.verticeList = new ArrayList<>();
        this.indiceList = new ArrayList<>();
        this.materials = new HashMap<>();
    }

    /**
     * Convert the actual ADT File to expected OBJ informations (vertices,
     * batches, indices & materials).
     *
     * @throws ConverterException If there's any validation error while
     * converting the ADT to OBJ.
     */
    public void convert() throws ConverterException {
        if (this.reader == null) {
            throw new ConverterException("ADTFileReader is null");
        }

        this.renderBatches.clear();
        this.verticeList.clear();
        this.indiceList.clear();
        this.materials.clear();
        
        List<MCNK> mapChunks;
        try {
            mapChunks = reader.getMapChunks();
        } catch (ADTException exception) {
            throw new ConverterException(exception.getMessage());
        }

        float initialChunkX = mapChunks.get(0).getPosX();
        float initialChunkY = mapChunks.get(0).getPosY();
        int index = 0;

        for (MCNK chunk : mapChunks) {
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
            throw new ConverterException("Provide file is null or empty.");
        }

        File objFile = new File(file);
        if (objFile.exists()) {
            objFile.delete();
        }

        OutputStreamWriter writer = new FileWriter(objFile);
        writer.write(getOBJasAString());
        writer.close();
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
                sb.append("f " + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "/"
                        + (indiceList.get(i + 2) + 1) + " " + (indiceList.get(i + 1) + 1) + "/"
                        + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) + " "
                        + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "\n");
                i += 3;
            }
        }

        return sb.toString();
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

    public ADTFileReader getReader() {
        return reader;
    }

    public void setReader(ADTFileReader reader) {
        this.reader = reader;
        this.renderBatches.clear();
        this.verticeList.clear();
        this.indiceList.clear();
        this.materials.clear();
    }
}
