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
package eu.jangos.extractor.file;

import com.sun.javafx.geom.Vec2f;
import java.awt.Color;
import eu.jangos.extractor.file.adt.chunk.MCIN;
import eu.jangos.extractor.file.adt.chunk.MCLQ;
import static eu.jangos.extractor.file.adt.chunk.MCLQ.LIQUID_FLAG_LENGTH;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final float TILE_SIZE = 533.33333f;
    public static final float CHUNK_SIZE = TILE_SIZE / 16.0f;
    public static final float UNIT_SIZE = CHUNK_SIZE / 8.0f;
    public static final float ZERO_POINT = 32.0f * TILE_SIZE;

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

    public String[][] getLiquidMapAsString(boolean displayLiquidType) throws FileReaderException {
        List<MCNK> mapChunks = getMapChunks();

        String[][] liquids = new String[SIZE_TILE_MAP][SIZE_TILE_MAP];
        int idx = 0;
        int idy = 0;
        for (MCNK chunk : mapChunks) {
            MCLQ liquid = chunk.getListLiquids().get(0);
            if (liquid == null) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                    }
                }
            } else {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        if (liquid.hasNoLiquid(i, j)) {
                            liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                        } else /*if (liquid.hasLiquid(i, j))*/ {                            
                                if (displayLiquidType) {
                                    String letter = "L";
                                    if (chunk.isRiver()) {
                                        letter += "R ";
                                    } else if (chunk.isOcean()) {
                                        letter += "O ";
                                    } else if (chunk.isMagma()) {
                                        letter += "M ";
                                    } else if (chunk.isSlime()) {
                                        letter += "S ";
                                    }
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = letter;
                                } else {
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "L ";
                                }
                            }
                        }
                    }                
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        return liquids;
    }
/**
     * Generate an image corresponding to the liquid light map. !!! This method isn't working properly !!!
     * @return The liquid light map under the form of a buffered image.
     * @throws FileReaderException If there was an issue while reading the map chunk of the ADT file.
     */
    public BufferedImage getLiquidLightMap() throws FileReaderException {
        BufferedImage img = new BufferedImage(SIZE_TILE_HEIGHTMAP, SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);        
        int idx = 0;
        int idy = 0;
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            if (chunk.hasNoLiquid()) {
                for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {
                    for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {
                        img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }                
            } else {                
                for (MCLQ liquid : chunk.getListLiquids()) {                    
                    for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {
                        for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {  
                            int value = (int) liquid.getLightAt(i, j);
                            if(value < 0 || value > 255)
                                System.out.println(this.filename + ";" + value);
                            //img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, new Color(value, value, value).getRGB());                                                            
                        }
                    }                    
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        return img;
    }
    
    public void saveLiquidLightMap(String pngPath) throws FileReaderException, IOException {
        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(getLiquidHeightMap(), "PNG", imgFile);
    }
    
    public BufferedImage getLiquidHeightMap() throws FileReaderException {        
        BufferedImage img = new BufferedImage(SIZE_TILE_HEIGHTMAP, SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);        
        int idx = 0;
        int idy = 0;
        Vec2f heightBounds = getLiquidMapBounds();
        float maxHeight = heightBounds.x;
        float minHeight = heightBounds.y;
        float diffHeight = maxHeight - minHeight;
        int value = 0;
        
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            if (chunk.hasNoLiquid()) {
                for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {
                    for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {
                        img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }                
            } else {                
                for (MCLQ liquid : chunk.getListLiquids()) {                            
                    for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {                          
                        for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {  
                            float height = liquid.getHeightAt(i, j);                            
                            if(height < Float.MAX_VALUE) { 
                                if(diffHeight == 0) {
                                    value = 255;
                                } else {
                                    value = (int) ((height - minHeight) / diffHeight * 255f);                                
                                }                                
                            } else {
                                value = 0;
                            }                                                                   
                            img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, new Color(value, value, value).getRGB());                                                            
                        }                        
                    }                                        
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        return img;
    }
    
    /**
     * Generate an image corresponding to the liquid height map with coloring depending on maximum and minimum liquid height provided in parameter.
     * maximum height being WHITE and minimum height being close to black.
     * @param maxHeight The maximum height value to be considered.
     * @param minHeight The minimum height value to be considered.
     * @return The liquid height map under the form of a buffered image.
     * @throws FileReaderException If there was an issue while reading the map chunk of the ADT file.
     */
    public BufferedImage getLiquidHeightMap(float maxHeight, float minHeight) throws FileReaderException {        
        BufferedImage img = new BufferedImage(SIZE_TILE_HEIGHTMAP, SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);        
        int idx = 0;
        int idy = 0;
        float diffHeight = maxHeight - minHeight;
        int value = 0;
        
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            if (chunk.hasNoLiquid()) {
                for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {
                    for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {
                        img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }                
            } else {                
                for (MCLQ liquid : chunk.getListLiquids()) {                            
                    for (int i = 0; i < CHUNK_TILE_HEIGHTMAP_LENGTH; i++) {                          
                        for (int j = 0; j < CHUNK_TILE_HEIGHTMAP_LENGTH; j++) {  
                            float height = liquid.getHeightAt(i, j);                            
                            if(height < Float.MAX_VALUE) { 
                                // In case there's no height difference between min & max. Render it full white.
                                if(diffHeight == 0) {
                                    value = 255;
                                } else {
                                    value = (int) ((height - minHeight) / diffHeight * 255f);
                                    // Avoid full black case when there's liquid.
                                    if(value < 10) {
                                        value = 10;
                                    }
                                }                                
                            } else {
                                value = 0;
                            }                                                                   
                            img.setRGB(CHUNK_TILE_HEIGHTMAP_LENGTH * idx + i, CHUNK_TILE_HEIGHTMAP_LENGTH * idy + j, new Color(value, value, value).getRGB());                                                            
                        }                        
                    }                                        
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        return img;
    }
    
    public void saveLiquidHeightMap(String pngPath) throws FileReaderException, IOException {
        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(getLiquidHeightMap(), "PNG", imgFile);
    }
    
    public BufferedImage getLiquidMap(boolean displayLiquidType) throws FileReaderException {
        BufferedImage img = new BufferedImage(SIZE_TILE_MAP, SIZE_TILE_MAP, BufferedImage.TYPE_INT_RGB);
        int alpha = 100;
        int idx = 0;
        int idy = 0;
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            if (chunk.hasNoLiquid()) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }                
            } else {
                int layer = 0;                
                for (MCLQ liquid : chunk.getListLiquids()) {
                    for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                        for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {                                                        
                            if (liquid.hasNoLiquid(i, j)) {
                                // Don't render.
                                img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                                if(liquid.isFlagD(i, j)) {
                                    img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.RED.getRGB());
                                }
                            } else {                                
                                    if (displayLiquidType) {
                                        if(liquid.isDark(i, j)) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.YELLOW.getRGB());
                                        }
                                        else if (liquid.isRiver(i, j) && layer < 1) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                        } else if (liquid.isOcean(i, j) && layer < 2) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(112, 48, 160, alpha).getRGB());
                                        } else if (liquid.isMagma(i, j) && layer < 3) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(244, 176, 132, alpha).getRGB());
                                        } else if (liquid.isSlime(i, j) && layer < 4) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(169, 208, 142, alpha).getRGB());
                                        }
                                        if(liquid.isAnimated(i, j)) {
                                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.RED.getRGB());
                                        }
                                    } else {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                    }                                                              
                            }                                                                                    
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
        
        return img;
    }    
    
    public void saveLiquidMap(String pngPath, boolean displayLiquidType) throws FileReaderException, IOException {
        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(getLiquidMap(displayLiquidType), "PNG", imgFile);
    }
    
    public boolean[][] getHoleMap() throws FileReaderException {
        List<MCNK> mapChunks = getMapChunks();
        boolean[][] holeMap = new boolean[SIZE_TILE_MAP][SIZE_TILE_MAP];

        int idx = 0;
        int idy = 0;
        for (MCNK chunk : mapChunks) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    holeMap[CHUNK_TILE_MAP_LENGTH * idx + i][CHUNK_TILE_MAP_LENGTH * idy + j] = chunk.isHole(i, j);
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        return holeMap;
    }

    public void saveHoleMap(String pngPath) throws IOException, FileReaderException {
        List<MCNK> mapChunks = getMapChunks();
        BufferedImage img = new BufferedImage(SIZE_TILE_MAP, SIZE_TILE_MAP, BufferedImage.TYPE_INT_RGB);
        int idx = 0;
        int idy = 0;

        for (MCNK chunk : mapChunks) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    img.setRGB(CHUNK_TILE_MAP_LENGTH * idx + i, CHUNK_TILE_MAP_LENGTH * idy + j, (chunk.isHole(i, j) ? Color.WHITE.getRGB() : Color.BLACK.getRGB()));
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", imgFile);
    }

    public Vec2f getLiquidMapBounds() throws FileReaderException {
        Vec2f heightBounds = new Vec2f(-Float.MAX_VALUE, Float.MAX_VALUE);
        Vec2f heightChunkBounds;
        
        for(MCNK chunk : getMapChunks()) {
            if(chunk.hasLiquid()) {
                heightChunkBounds = chunk.getLiquidHeightBounds();
                if(heightChunkBounds.x > heightBounds.x) {
                    heightBounds.x = heightChunkBounds.x;
                }
                if(heightChunkBounds.y < heightBounds.y) {
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

}
