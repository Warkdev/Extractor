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
package eu.jangos.extractor.file.impl;

import com.sun.javafx.geom.Vec2f;
import eu.jangos.extractor.file.FileReader;
import eu.jangos.extractor.file.adt.chunk.MCIN;
import eu.jangos.extractor.file.adt.chunk.MCLQ;
import static eu.jangos.extractor.file.adt.chunk.MCLQ.LIQUID_FLAG_LENGTH;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.common.MapUnit;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import eu.jangos.extractorfx.rendering.FileType2D;
import eu.jangos.extractorfx.rendering.FileType3D;
import eu.jangos.extractorfx.rendering.PolygonMesh;
import eu.jangos.extractorfx.rendering.PolygonMeshView;
import eu.jangos.extractorfx.rendering.Render2DType;
import eu.jangos.extractorfx.rendering.Render3DType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.crigges.jmpq3.JMpqException;

/**
 *
 * @author Warkdev
 */
public class ADT extends FileReader {

    private static final Logger logger = LoggerFactory.getLogger(ADT.class);

    private static final String HEADER_VERSION = "MVER";
    private static final String HEADER_MHDR = "MHDR";
    private static final String HEADER_MCIN = "MCIN";
    private static final String HEADER_MTEX = "MTEX";
    private static final String HEADER_MMDX = "MMDX";
    private static final String HEADER_MMID = "MMID";
    private static final String HEADER_MWMO = "MWMO";
    private static final String HEADER_MWID = "MWID";
    private static final String HEADER_MDDF = "MDDF";
    private static final String HEADER_MODF = "MODF";
    private static final String HEADER_MCNK = "MCNK";

    // Size as from which the offset calculation is made.
    private static final int GLOBAL_OFFSET = 0x14;

    public static final int SIZE_TILE_MAP = 128;
    public static final int SIZE_TILE_HEIGHTMAP = 144;
    private static final int CHUNK_TILE_MAP_LENGTH = 8;
    private static final int CHUNK_TILE_HEIGHTMAP_LENGTH = 9;

    private int version;
    private int mfboEnum;
    private int headerFlags;
    private int offsetMCIN;
    private int offsetMTEX;
    private int offsetMMDX;
    private int offsetMMID;
    private int offsetMWMO;
    private int offsetMWID;
    private int offsetMDDF;
    private int offsetMODF;
    private int offsetMFBO;
    private int offsetMH2O;
    private int offsetMTFX;

    // Conditions & parameters for rendering.
    private boolean addModels = false;
    private boolean addWMO = false;
    private boolean yUp = false;

    private float maxHeight;
    private float minHeight;

    public void init(MPQManager manager, String filename) throws IOException, FileReaderException, MPQException {
        init(manager, filename, false);
    }

    @Override
    public void init(MPQManager manager, String filename, boolean loadChildren) throws IOException, FileReaderException, MPQException {
        init = false;

        super.data = ByteBuffer.wrap(manager.getMPQForFile(filename).extractFileAsBytes(filename));

        if (data.remaining() == 0) {
            logger.error("Data array for ADT " + filename + " is empty.");
            throw new ADTException("Data array is empty.");
        }

        super.data.order(ByteOrder.LITTLE_ENDIAN);
        super.filename = filename;

        // This is all what we need to read our file. Initialize the offset and check the version.
        readVersion(super.data);
        readHeader(super.data);
        init = true;
    }

    /**
     * Reading the ADT version. The ADT version is expected to be located at the
     * beginning of the ADT File under this form: 4 character indicating the
     * MVER chunk in reverse order. 4 bytes indicating the length of the MVER
     * chunk. 4 bytes indicating the version of the ADT File (18 in 1.12.x).
     * e.g.: REVM 04 00 00 00 12 00 00 00
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readVersion(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_VERSION);

        // We skip the size as we know it's 4.
        in.getInt();
        this.version = in.getInt();
    }

    /**
     * Reading the MHDR chunk of the ADT file. The MHDR chunk is expected to be
     * right after the MVER chunk. It contains offset towards the various chunks
     * hold in this ADT. e.g. : RDHM
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readHeader(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_MHDR);

        this.mfboEnum = in.getInt();
        this.headerFlags = in.getInt();
        this.offsetMCIN = in.getInt();
        this.offsetMTEX = in.getInt();
        this.offsetMMDX = in.getInt();
        this.offsetMMID = in.getInt();
        this.offsetMWMO = in.getInt();
        this.offsetMWID = in.getInt();
        this.offsetMDDF = in.getInt();
        this.offsetMODF = in.getInt();
        this.offsetMFBO = in.getInt();
        this.offsetMH2O = in.getInt();
        this.offsetMTFX = in.getInt();
    }

    private MCIN[] readMCIN() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        super.data.position(GLOBAL_OFFSET + this.offsetMCIN);

        checkHeader(HEADER_MCIN);

        int size = super.data.getInt();
        MCIN index;
        MCIN[] chunkIndex = new MCIN[size / MCIN.getOBJECT_SIZE()];
        for (int i = 0; i < chunkIndex.length; i++) {
            index = new MCIN();
            index.read(super.data);
            chunkIndex[i] = index;
        }

        return chunkIndex;
    }

    public List<String> getTextures() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMTEX + GLOBAL_OFFSET, HEADER_MTEX);
    }

    public List<String> getModels() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMMDX + GLOBAL_OFFSET, HEADER_MMDX);
    }

    public List<Integer> getModelOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMMID + GLOBAL_OFFSET, HEADER_MMID);
    }

    public List<String> getWorldObjects() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMWMO + GLOBAL_OFFSET, HEADER_MWMO);
    }

    public List<Integer> getWorldObjectsOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMWID + GLOBAL_OFFSET, HEADER_MWID);
    }

    public List<MDDF> getDoodadPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MDDF> listPlacement = new ArrayList<>();

        super.data.position(this.offsetMDDF + GLOBAL_OFFSET);

        checkHeader(HEADER_MDDF);

        int size = super.data.getInt();
        int start = super.data.position();
        MDDF placement;
        while (super.data.position() - start < size) {
            placement = new MDDF();
            placement.read(super.data);
            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MODF> getWorldObjectsPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MODF> listPlacement = new ArrayList<>();
        super.data.position(this.offsetMODF + GLOBAL_OFFSET);

        checkHeader(HEADER_MODF);

        int size = super.data.getInt();
        int start = super.data.position();
        MODF placement;
        while (super.data.position() - start < size) {
            placement = new MODF();
            placement.read(super.data);
            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MCNK> getMapChunks() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MCNK> listMapChunks = new ArrayList<>();

        MCIN[] chunks = readMCIN();

        MCNK chunk;
        for (int i = 0; i < chunks.length; i++) {
            super.data.position(chunks[i].getOffsetMCNK());

            checkHeader(HEADER_MCNK);

            // We ignore size.
            super.data.getInt();
            chunk = new MCNK();
            chunk.read(super.data);
            listMapChunks.add(chunk);
        }

        return listMapChunks;
    }

    public boolean isAddModels() {
        return addModels;
    }

    public void setAddModels(boolean addModels) {
        this.addModels = addModels;
    }

    public boolean isAddWMO() {
        return addWMO;
    }

    public void setAddWMO(boolean addWMO) {
        this.addWMO = addWMO;
    }

    public boolean isyUp() {
        return yUp;
    }

    public void setyUp(boolean yUp) {
        this.yUp = yUp;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }    
    
    public Vec2f getLiquidMapBounds() throws FileReaderException {
        Vec2f heightBounds = new Vec2f(-Float.MAX_VALUE, Float.MAX_VALUE);
        Vec2f heightChunkBounds;

        for (MCNK chunk : getMapChunks()) {
            if (chunk.hasLiquid()) {
                heightChunkBounds = chunk.getLiquidHeightBounds();
                if (heightChunkBounds.x > heightBounds.x) {
                    heightBounds.x = heightChunkBounds.x;
                }
                if (heightChunkBounds.y < heightBounds.y) {
                    heightBounds.y = heightChunkBounds.y;
                }
            }
        }

        return heightBounds;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public Pane render2D(Render2DType renderType, int width, int height) throws ConverterException, FileReaderException {
        if (this.init == false) {
            logger.error("This ADT has not been initialized !");
            throw new ConverterException("This ADT has not been initialized !");
        }

        switch (renderType) {
            case RENDER_TILEMAP_LIQUID_TYPE:
            case RENDER_TILEMAP_LIQUID_FISHABLE:
            case RENDER_TILEMAP_LIQUID_ANIMATED:
            case RENDER_TILEMAP_LIQUID_HEIGHTMAP:
                return renderLiquidTileMap(renderType);
            case RENDER_TILEMAP_TERRAIN_HOLEMAP:
                return renderHoleTileMap();
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public PolygonMesh render3D(Render3DType renderType, Map<String, M2> cache) throws ConverterException, MPQException, FileReaderException {
        if (this.init == false) {
            logger.error("This ADT has not been initialized !");
            throw new ConverterException("This ADT has not been initialized !");
        }

        switch (renderType) {
            case LIQUID:
                return renderLiquid();
            case TERRAIN:
                return renderTerrain(cache);
            default:
                throw new UnsupportedOperationException("The type method is not supported on this object.");
        }
    }

    private PolygonMesh renderLiquid() {
        return liquidMesh;
    }

    private PolygonMesh renderTerrain(Map<String, M2> cache) throws FileReaderException, ConverterException, MPQException {
        clearMesh();

        List<MCNK> mapChunks;
        try {
            mapChunks = getMapChunks();
        } catch (ADTException exception) {
            throw new ConverterException(exception.getMessage());
        }

        float initialChunkX = mapChunks.get(0).getPosition().x;
        float initialChunkY = mapChunks.get(0).getPosition().y;

        for (MCNK chunk : mapChunks) {
            int offset = shapeMesh.getPoints().size() / 3;

            for (int i = 0, idx = 0; i < 17; i++) {
                for (int j = 0; j < (((i % 2) != 0) ? 8 : 9); j++, idx++) {
                    // Calculating Normals.                    
                    //this.mesh.getNormals().addAll(chunk.getNormals().getPoints()[idx].getX() / 127.0f, chunk.getNormals().getPoints()[idx].getY() / 127.0f, chunk.getNormals().getPoints()[idx].getZ() / 127.0f);
                    float x, y, z;
                    // Calculating Positions.                    
                    x = chunk.getPosition().x - (i * MapUnit.UNIT_SIZE * 0.5f);
                    y = chunk.getPosition().y - (j * MapUnit.UNIT_SIZE);
                    z = chunk.getVertices().getPoints()[idx] + chunk.getPosition().z;
                    if ((i % 2) != 0) {
                        y -= 0.5f * MapUnit.UNIT_SIZE;
                    }
                    this.shapeMesh.getPoints().addAll(x, y, z);
                    // Calculating TexCoord in high resolution.
                    x = ((chunk.getPosition().x - initialChunkX) * (-1)) / MapUnit.CHUNK_SIZE;
                    y = (chunk.getPosition().y - initialChunkY) * (-1) / MapUnit.CHUNK_SIZE;
                    this.shapeMesh.getTexCoords().addAll(x, y);
                }
            }

            // We allocate the maximum amount of faces.
            int[][] faces = new int[544][6];
            int idx = 0;
            for (int j = 9, x = 0, y = 0; j < 145; j++, x++) {
                if (x >= 8) {
                    x = 0;
                    y++;
                }

                if (!chunk.isHole(x, y)) {
                    // Adding triangles
                    // Face 1.
                    faces[idx][0] = offset + j + 8;
                    faces[idx][1] = offset + j + 8;
                    faces[idx][2] = offset + j - 9;
                    faces[idx][3] = offset + j - 9;
                    faces[idx][4] = offset + j;
                    faces[idx][5] = offset + j;

                    idx++;
                    /**
                     * this.mesh.getFaces().addAll(offset + j + 8, offset + j +
                     * 8, offset + j + 8); this.mesh.getFaces().addAll(offset +
                     * j - 9, offset + j - 9, offset + j - 9);
                     * this.mesh.getFaces().addAll(offset + j, offset + j,
                     * offset + j);
                     */

                    // Face 2.
                    faces[idx][0] = offset + j - 9;
                    faces[idx][1] = offset + j - 9;
                    faces[idx][2] = offset + j - 8;
                    faces[idx][3] = offset + j - 8;
                    faces[idx][4] = offset + j;
                    faces[idx][5] = offset + j;

                    idx++;
                    /**
                     * this.mesh.getFaces().addAll(offset + j - 9, offset + j -
                     * 9, offset + j - 9); this.mesh.getFaces().addAll(offset +
                     * j - 8, offset + j - 8, offset + j - 8);
                     * this.mesh.getFaces().addAll(offset + j, offset + j,
                     * offset + j);
                     */

                    // Face 3.                    
                    faces[idx][0] = offset + j - 8;
                    faces[idx][1] = offset + j - 8;
                    faces[idx][2] = offset + j + 9;
                    faces[idx][3] = offset + j + 9;
                    faces[idx][4] = offset + j;
                    faces[idx][5] = offset + j;

                    idx++;
                    /**
                     * this.mesh.getFaces().addAll(offset + j - 8, offset + j -
                     * 8, offset + j - 8); this.mesh.getFaces().addAll(offset +
                     * j + 9, offset + j + 9, offset + j + 9);
                     * this.mesh.getFaces().addAll(offset + j, offset + j,
                     * offset + j);
                     */

                    //Face 4.
                    faces[idx][0] = offset + j + 9;
                    faces[idx][1] = offset + j + 9;
                    faces[idx][2] = offset + j + 8;
                    faces[idx][3] = offset + j + 8;
                    faces[idx][4] = offset + j;
                    faces[idx][5] = offset + j;

                    idx++;
                    /**
                     * this.mesh.getFaces().addAll(offset + j + 9, offset + j +
                     * 9, offset + j + 9); this.mesh.getFaces().addAll(offset +
                     * j + 8, offset + j + 8, offset + j + 8);
                     * this.mesh.getFaces().addAll(offset + j, offset + j,
                     * offset + j);
                     */
                    
                    shapeMesh.getFaceSmoothingGroups().addAll(0);
                }

                if ((j + 1) % (9 + 8) == 0) {
                    j += 9;
                }
            }
        }

        if (addModels) {
            M2 model;            
            // Now we add models.                    
            for (MDDF modelPlacement : this.getDoodadPlacement()) {
                // MDX model files are stored as M2 in the MPQ. God knows why.
                String modelFile = FilenameUtils.removeExtension(getModels().get(modelPlacement.getMmidEntry())) + ".M2";
                if (!manager.getMPQForFile(modelFile).hasFile(modelFile)) {
                    logger.debug("Oooops, no MPQ found for this file: " + modelFile);
                    continue;
                }

                try {
                    // First, check if the M2 is in cache. Must be much faster than parsing it again and again.
                    if (cache.containsKey(modelFile)) {
                        model = cache.get(modelFile);
                    } else {
                        model = new M2();                        
                        model.init(manager, modelFile);
                        model.render3D(Render3DType.MODEL, null);
                        cache.put(modelFile, model);
                    }

                    // Now, we have the vertices of this M2, we need to scale, rotate & position.                                                                                
                    // First, we create a view to apply these transformations.
                    PolygonMeshView view = new PolygonMeshView(shapeMesh);

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
                    PolygonMesh temp = new PolygonMesh();
                    for (int i = 0; i < model.getShapeMesh().getPoints().size(); i += 3) {
                        Point3D point = new Point3D(model.getShapeMesh().getPoints().get(i), model.getShapeMesh().getPoints().get(i + 1), model.getShapeMesh().getPoints().get(i + 2));
                        point = concat.transform(point);
                        temp.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
                    }

                    int offset = shapeMesh.getPoints().size() / 3;

                    // Then, we add the converted model mesh to the WMO mesh.
                    shapeMesh.getPoints().addAll(temp.getPoints());
                    //this.mesh.getNormals().addAll(converter.mesh.getNormals());
                    shapeMesh.getTexCoords().addAll(model.getShapeMesh().getTexCoords());

                    // And we recalculate the faces of the model mesh.
                    int[][] faces = new int[model.getShapeMesh().faces.length][model.getShapeMesh().faces[0].length];
                    for (int i = 0; i < model.getShapeMesh().faces.length; i++) {
                        for (int j = 0; j < model.getShapeMesh().faces[i].length; j++) {
                            faces[i][j] += offset;
                        }
                    }
                    shapeMesh.faces = ArrayUtils.addAll(shapeMesh.faces, faces);
                } catch (JMpqException | FileReaderException ex) {
                    logger.error("An error occured while reading the MPQ when importing Models.");
                } catch (IOException ex) {
                    logger.error("An error occured while reading the MPQ when importing Models.");
                }
            }
        }
        if (addWMO) {
            WMO wmo;
            // Now we add wmo.        

            for (MODF modelPlacement : getWorldObjectsPlacement()) {
                String wmoFile = FilenameUtils.removeExtension(getWorldObjects().get(modelPlacement.getMwidEntry())) + ".WMO";
                if (!manager.getMPQForFile(wmoFile).hasFile(wmoFile)) {
                    logger.debug("Oooops, no MPQ found for this file: " + wmoFile);
                    continue;
                }

                try {
                    wmo = new WMO();
                    wmo.init(manager, wmoFile);
                    wmo.render3D(Render3DType.MODEL, cache);

                    // Now, we have the vertices of this WMO, we need to rotate & position.                                                                                
                    // First, we create a view to apply these transformations.
                    PolygonMeshView view = new PolygonMeshView(wmo.getShapeMesh());

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
                    for (int i = 0; i < wmo.getShapeMesh().getPoints().size(); i += 3) {
                        Point3D point = new Point3D(wmo.getShapeMesh().getPoints().get(i), wmo.getShapeMesh().getPoints().get(i + 1), wmo.getShapeMesh().getPoints().get(i + 2));
                        point = concat.transform(point);
                        temp.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
                    }

                    int offset = shapeMesh.getPoints().size() / 3;

                    // Then, we add the converted model mesh to the WMO mesh.
                    shapeMesh.getPoints().addAll(temp.getPoints());
                    //this.mesh.getNormals().addAll(converter.mesh.getNormals());
                    shapeMesh.getTexCoords().addAll(wmo.getShapeMesh().getTexCoords());

                    // And we recalculate the faces of the model mesh.
                    int[][] faces = new int[wmo.getShapeMesh().faces.length][wmo.getShapeMesh().faces[0].length];
                    for (int i = 0; i < wmo.getShapeMesh().faces.length; i++) {
                        for (int j = 0; j < wmo.getShapeMesh().faces[i].length; j++) {
                            faces[i][j] += offset;
                        }
                    }
                    shapeMesh.faces = ArrayUtils.addAll(shapeMesh.faces, faces);

                } catch (JMpqException | FileReaderException ex) {
                    logger.error("Error while reading MPQ to add WMO");
                }
            }
        }

        // Finally, we rotate if that's the desired output.
        if (yUp) {
            Rotate rx = new Rotate(-90, Rotate.X_AXIS);

            PolygonMeshView view = new PolygonMeshView(shapeMesh);
            view.getTransforms().addAll(rx);
            Transform concat = view.getLocalToSceneTransform();

            for (int i = 0; i < shapeMesh.getPoints().size(); i += 3) {
                Point3D point = new Point3D(shapeMesh.getPoints().get(i), shapeMesh.getPoints().get(i + 1), shapeMesh.getPoints().get(i + 2));
                point = concat.transform(point);
                shapeMesh.getPoints().set(i, (float) point.getX());
                shapeMesh.getPoints().set(i + 1, (float) point.getY());
                shapeMesh.getPoints().set(i + 2, (float) point.getZ());
            }
        }

        return shapeMesh;
    }

    private Pane renderLiquidTileMap(Render2DType renderType) throws FileReaderException {
        Pane pane = new Pane();

        int idx = 0;
        int idy = 0;
        Group liquidGroup = new Group();
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            if (chunk.hasNoLiquid()) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        Rectangle tile = new Rectangle((i * MapUnit.UNIT_SIZE) + (idx * MapUnit.CHUNK_SIZE), (j * MapUnit.UNIT_SIZE) + (idy * MapUnit.CHUNK_SIZE), MapUnit.UNIT_SIZE, MapUnit.UNIT_SIZE);
                        tile.setFill(Color.BLACK);
                        liquidGroup.getChildren().add(tile);
                    }
                }
            } else {
                int layer = 0;
                for (MCLQ liquid : chunk.getListLiquids()) {
                    for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                        for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                            Rectangle tile = new Rectangle((i * MapUnit.UNIT_SIZE) + (idx * MapUnit.CHUNK_SIZE), (j * MapUnit.UNIT_SIZE) + (idy * MapUnit.CHUNK_SIZE), MapUnit.UNIT_SIZE, MapUnit.UNIT_SIZE);
                            if (liquid.hasNoLiquid(i, j)) {
                                // Don't render.                                
                                tile.setFill(Color.BLACK);
                            } else {
                                tile.setFill(getColorForLiquid(renderType, chunk, layer, i, j));
                            }
                            liquidGroup.getChildren().add(tile);
                        }
                    }

                    layer++;
                }

            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        pane.getChildren().add(liquidGroup);

        return pane;
    }

    private Pane renderHoleTileMap() throws FileReaderException {
        Pane pane = new Pane();                        
        Group tileGroup = new Group();
        
        int idx = 0;
        int idy = 0;
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {            
            for (int i = 0; i < CHUNK_TILE_MAP_LENGTH; i++) {
                for (int j = 0; j < CHUNK_TILE_MAP_LENGTH; j++) {
                    Rectangle tile = new Rectangle((i * MapUnit.UNIT_SIZE) + (idx * MapUnit.CHUNK_SIZE), (j * MapUnit.UNIT_SIZE) + (idy * MapUnit.CHUNK_SIZE), MapUnit.UNIT_SIZE, MapUnit.UNIT_SIZE);
                    if(chunk.isHole(i, j)) {
                        tile.setFill(Color.WHITE);
                    } else {
                        tile.setFill(Color.BLACK);
                    }                 
                    tileGroup.getChildren().add(tile);
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        pane.getChildren().add(tileGroup);
        return pane;
    }

    private Color getColorForLiquid(Render2DType renderType, MCNK chunk, int layer, int x, int y) {
        MCLQ liquid = chunk.getListLiquids().get(layer);
        switch (renderType) {
            case RENDER_TILEMAP_LIQUID_TYPE:
                if (liquid.isDark(x, y)) {
                    return Color.YELLOW;
                } else if (liquid.isRiver(x, y)) {
                    return Color.BLUE;
                } else if (liquid.isOcean(x, y)) {
                    return Color.PURPLE;
                } else if (liquid.isMagma(x, y)) {
                    return Color.ORANGE;
                } else if (liquid.isSlime(x, y)) {
                    return Color.GREEN;
                }
                break;
            case RENDER_TILEMAP_LIQUID_FISHABLE:
                if (liquid.isFishable(x, y)) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            case RENDER_TILEMAP_LIQUID_ANIMATED:
                if (liquid.isAnimated(x, y)) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            case RENDER_TILEMAP_LIQUID_HEIGHTMAP:
                return getColorForHeight(chunk.getListLiquids().get(layer).getHeightAt(x, y));
        }
        return Color.BLACK;
    }

    private Color getColorForHeight(float height) {
        float diffHeight = maxHeight - minHeight;
        int value = 0;

        if (height < Float.MAX_VALUE) {
            // In case there's no height difference between min & max. Render it full white.
            if (diffHeight == 0) {
                value = 255;
            } else {
                value = (int) ((height - minHeight) / diffHeight * 255f);
                // Avoid full black case when there's liquid.
                if (value < 10) {
                    value = 10;
                }
            }
        } else {
            value = 0;
        }
        return new Color(value, value, value, 100);
    }
} 
