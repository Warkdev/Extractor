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
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.WDTException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractor.file.wdt.AreaInfo;
import eu.mangos.shared.flags.FlagUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.crigges.jmpq3.JMpqException;

/**
 * WDT represents a WDT file from WoW package. This class allows to read its
 * definition.
 *
 * @author Warkdev
 */
public class WDT extends FileReader {

    private static final Logger logger = LoggerFactory.getLogger(WDT.class);

    // Headers expected in the WDT.
    private static final String HEADER_MVER = "MVER";
    private static final String HEADER_MPHD = "MPHD";
    private static final String HEADER_MAIN = "MAIN";
    private static final String HEADER_MWMO = "MWMO";
    private static final String HEADER_MODF = "MODF";

    // Supported version of the WDT
    private static final int SUPPORTED_VERSION = 18;
    private static final int SIZE_AREA_INFO = 8;

    // Flag value to indicate whether it uses global map object or not. In case not, there's ADT (terrain information).
    public static final int FLAG_USE_GLOBAL_MAP_OBJ = 0x0001;

    // Map tile size for terrain information. Only one value as the tile is a square.
    public static final int MAP_TILE_SIZE = 64;

    private int version;

    private int flags;

    private List<AreaInfo> listAreas = new ArrayList<>();
    private String wmo;
    private MODF wmoPlacement = new MODF();
    private ADT[][] adtArray = new ADT[MAP_TILE_SIZE][MAP_TILE_SIZE];
    private float liquidMinHeight;
    private float liquidMaxHeight;    
    
    @Override
    public void init(MPQManager manager, String filename, boolean loadChildren) throws FileReaderException, JMpqException, MPQException, IOException {
        super.init = false;
        listAreas.clear();        
        
        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                adtArray[i][j] = null;
            }
        }

        liquidMinHeight = Float.MAX_VALUE;
        liquidMaxHeight = -Float.MAX_VALUE;
        
        super.filename = filename;
        super.data = ByteBuffer.wrap(manager.getMPQForFile(filename).extractFileAsBytes(filename));
        super.data.order(ByteOrder.LITTLE_ENDIAN);

        int size;

        readVersion(super.data);

        if (version != SUPPORTED_VERSION) {
            throw new WDTException("The WDT file version is not supported (" + this.version + "), supported version: " + SUPPORTED_VERSION);
        }

        checkHeader(HEADER_MPHD);
        size = super.data.getInt();
        this.flags = super.data.getInt();

        // We just skip unused data.
        super.data.position(super.data.position() + (size - 4));

        checkHeader(HEADER_MAIN);
        size = super.data.getInt();

        if (size != MAP_TILE_SIZE * MAP_TILE_SIZE * SIZE_AREA_INFO) {
            throw new WDTException("The size for the ADT Map Tile is not the expected one. This file looks corrupted.");
        }

        AreaInfo info;
        for (int i = 0; i < MAP_TILE_SIZE * MAP_TILE_SIZE; i++) {
            info = new AreaInfo();
            info.read(super.data);
            listAreas.add(info);
        }

        checkHeader(HEADER_MWMO);
        size = super.data.getInt();

        if (useGlobalMapObj()) {
            // There should be one WMO definition with its placement information.
            wmo = readString(super.data);

            checkHeader(HEADER_MODF);
            size = super.data.getInt();
            this.wmoPlacement = new MODF();
            this.wmoPlacement.read(super.data);
        }
        
        if (loadChildren) {
            ADT adt;
            Vec2f liquidMapBounds;
            String base = FilenameUtils.getPath(this.filename) + FilenameUtils.getBaseName(this.filename);
            for (int x = 0; x < MAP_TILE_SIZE; x++) {
                for (int y = 0; y < MAP_TILE_SIZE; y++) {
                    if (hasTerrain(x, y)) {
                        adt = new ADT();
                        adt.init(manager, base + "_" + y + "_" + x + ".adt", false);
                        adtArray[x][y] = adt;
                        liquidMapBounds = adt.getLiquidMapBounds();
                        if(liquidMapBounds.x > this.liquidMaxHeight) {
                            this.liquidMaxHeight = liquidMapBounds.x;
                        }
                        if(liquidMapBounds.y < this.liquidMinHeight) {
                            this.liquidMinHeight = liquidMapBounds.y;
                        }
                    }
                }
            }
        }        

        super.init = true;
    }

    /**
     * Indicates whether this WDT has only a WMO in it (as a dungeon).
     *
     * @return True if this WDT is only storing a WMO information or false if it
     * also has terrain information.
     */
    public boolean useGlobalMapObj() {
        return hasFlag(FLAG_USE_GLOBAL_MAP_OBJ);
    }

    /**
     * Returns the tilemap corresponding to the terrain definition. Each tile is
     * represented by a boolean value indicating whether this tile has terrain
     * information.
     *
     * @return An array of array of boolean representing the terrain information
     * stored in this WDT file.
     */
    public boolean[][] getTileMap() {
        boolean[][] tileMap = new boolean[MAP_TILE_SIZE][MAP_TILE_SIZE];

        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                tileMap[i][j] = hasTerrain(i, j);
            }
        }

        return tileMap;
    }

    /**
     * Indicates whether the tile at the position row/col has a terrain
     * information (under the format of an ADT file) or not.
     *
     * @param row The row of the tile.
     * @param col The column of the tile.
     * @return True if the tile has a terrain information, false otherwise.
     */
    public boolean hasTerrain(int row, int col) {
        return this.listAreas.get(row * MAP_TILE_SIZE + col).hasADT();
    }

    /**
     * Save the Terrain data information under the format of a TileMap (PNG
     * file). If the file already exist, this method erase it before creating a
     * new one.
     *
     * @param path Path of the file to be created.
     * @throws IOException IOException is throw by the file handler if there's
     * any issue with the filesystem.
     */
    public void saveTileMap(String path) throws IOException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE, MAP_TILE_SIZE, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                if (hasTerrain(j, i)) {
                    img.setRGB(i, j, Color.YELLOW.getRGB());
                } else {
                    img.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }

        File imgFile = new File(path);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", imgFile);
    }

    /**
     * Save the hole map data information under PNG format.
     *
     * @param pngPath
     * @throws IOException
     */
    public void saveHoleMap(String pngPath) throws IOException, FileReaderException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE * ADT.SIZE_TILE_MAP, MAP_TILE_SIZE * ADT.SIZE_TILE_MAP, BufferedImage.TYPE_INT_RGB);

        ADT adt;
        boolean[][] holeMap;
        int offsetX = 0;
        int offsetY = 0;
        for (int x = 0; x < MAP_TILE_SIZE; x++) {
            for (int y = 0; y < MAP_TILE_SIZE; y++) {
                adt = adtArray[x][y];
                if (adt != null) {
                    holeMap = adt.getHoleMap();
                    for (int i = 0; i < holeMap.length; i++) {
                        for (int j = 0; j < holeMap[i].length; j++) {
                            if (holeMap[i][j]) {
                                img.setRGB(i + offsetX, j + offsetY, Color.WHITE.getRGB());
                            } else {
                                img.setRGB(i + offsetX, j + offsetY, Color.ORANGE.getRGB());
                            }
                        }
                    }
                } else {

                }

                offsetX += ADT.SIZE_TILE_MAP;
                if (offsetX >= ADT.SIZE_TILE_MAP * MAP_TILE_SIZE) {
                    offsetX = 0;
                    offsetY += ADT.SIZE_TILE_MAP;
                }
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

    public void saveLiquidMap(String pngPath) throws IOException, FileReaderException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE * ADT.SIZE_TILE_MAP, MAP_TILE_SIZE * ADT.SIZE_TILE_MAP, BufferedImage.TYPE_INT_RGB);
        BufferedImage oceanImg = new BufferedImage(ADT.SIZE_TILE_MAP, ADT.SIZE_TILE_MAP, BufferedImage.TYPE_INT_RGB);

        // Initialiazation of a full void style image.
        for (int i = 0; i < ADT.SIZE_TILE_MAP; i++) {
            for (int j = 0; j < ADT.SIZE_TILE_MAP; j++) {
                oceanImg.setRGB(i, j, new Color(0, 32, 50).getRGB());
            }
        }

        ADT adt;
        int offsetX = 0;
        int offsetY = 0;
        Graphics2D g2 = img.createGraphics();
        for (int x = 0; x < MAP_TILE_SIZE; x++) {
            for (int y = 0; y < MAP_TILE_SIZE; y++) {
                adt = adtArray[x][y];
                if (adt != null) {
                    g2.drawImage(adt.getLiquidMap(true), null, offsetX, offsetY);
                } else {
                    g2.drawImage(oceanImg, null, offsetX, offsetY);
                }

                offsetX += ADT.SIZE_TILE_MAP;
                if (offsetX >= ADT.SIZE_TILE_MAP * MAP_TILE_SIZE) {
                    offsetX = 0;
                    offsetY += ADT.SIZE_TILE_MAP;
                }
            }
        }

        g2.dispose();

        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", imgFile);
    }    
    
    public void saveLiquidHeightMap(String pngPath) throws IOException, FileReaderException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE * ADT.SIZE_TILE_HEIGHTMAP, MAP_TILE_SIZE * ADT.SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);
        BufferedImage oceanImg = new BufferedImage(ADT.SIZE_TILE_HEIGHTMAP, ADT.SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);

        // Initialiazation of a full void style image.
        for (int i = 0; i < ADT.SIZE_TILE_HEIGHTMAP; i++) {
            for (int j = 0; j < ADT.SIZE_TILE_HEIGHTMAP; j++) {
                oceanImg.setRGB(i, j, Color.BLACK.getRGB());
            }
        }

        ADT adt;
        int offsetX = 0;
        int offsetY = 0;
        Graphics2D g2 = img.createGraphics();
        for (int x = 0; x < MAP_TILE_SIZE; x++) {
            for (int y = 0; y < MAP_TILE_SIZE; y++) {
                adt = adtArray[x][y];
                if (adt != null) {
                    g2.drawImage(adt.getLiquidHeightMap(this.liquidMaxHeight, this.liquidMinHeight), null, offsetX, offsetY);
                } else {
                    g2.drawImage(oceanImg, null, offsetX, offsetY);
                }

                offsetX += ADT.SIZE_TILE_MAP;
                if (offsetX >= ADT.SIZE_TILE_MAP * MAP_TILE_SIZE) {
                    offsetX = 0;
                    offsetY += ADT.SIZE_TILE_MAP;
                }
            }
        }

        g2.dispose();

        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", imgFile);
    }    
    
    public void saveLiquidLightMap(String pngPath) throws IOException, FileReaderException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE * ADT.SIZE_TILE_HEIGHTMAP, MAP_TILE_SIZE * ADT.SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);
        BufferedImage oceanImg = new BufferedImage(ADT.SIZE_TILE_HEIGHTMAP, ADT.SIZE_TILE_HEIGHTMAP, BufferedImage.TYPE_INT_RGB);

        // Initialiazation of a full void style image.
        for (int i = 0; i < ADT.SIZE_TILE_HEIGHTMAP; i++) {
            for (int j = 0; j < ADT.SIZE_TILE_HEIGHTMAP; j++) {
                oceanImg.setRGB(i, j, Color.TRANSLUCENT);
            }
        }

        ADT adt;
        int offsetX = 0;
        int offsetY = 0;
        Graphics2D g2 = img.createGraphics();
        for (int x = 0; x < MAP_TILE_SIZE; x++) {
            for (int y = 0; y < MAP_TILE_SIZE; y++) {
                adt = adtArray[x][y];
                if (adt != null) {
                    g2.drawImage(adt.getLiquidLightMap(), null, offsetX, offsetY);
                } else {
                    g2.drawImage(oceanImg, null, offsetX, offsetY);
                }

                offsetX += ADT.SIZE_TILE_MAP;
                if (offsetX >= ADT.SIZE_TILE_MAP * MAP_TILE_SIZE) {
                    offsetX = 0;
                    offsetY += ADT.SIZE_TILE_MAP;
                }
            }
        }

        g2.dispose();

        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", imgFile);
    }
    
    // Getter & Setter.
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public List<AreaInfo> getListAreas() {
        return listAreas;
    }

    public void setListAreas(List<AreaInfo> listAreas) {
        this.listAreas = listAreas;
    }

    public String getWmo() {
        return wmo;
    }

    public void setWmo(String wmo) {
        this.wmo = wmo;
    }

    public MODF getWmoPlacement() {
        return wmoPlacement;
    }

    public void setWmoPlacement(MODF wmoPlacement) {
        this.wmoPlacement = wmoPlacement;
    }

    // Private methods.
    /**
     * Read the version of the file. If the file version doesn't match the
     * version header, it throws an exception.
     *
     * @param in The ByteBuffer from which the version needs to be read.
     * @throws WDTException If the expected header is not found.
     */
    private void readVersion(ByteBuffer in) throws WDTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MVER)) {
            throw new WDTException("Expected header " + HEADER_MVER + ", received header: " + sb.toString());
        }

        // We skip the size as we know it's 4.
        in.getInt();
        this.version = in.getInt();
    }

    /**
     * Check whether the flag field has the bit at the position flag set or not.
     *
     * @param flag The position of the bit that must be checked.
     * @return True if the bit is set, false otherwise.
     */
    private boolean hasFlag(int flag) {
        return FlagUtils.hasFlag(this.flags, flag);
    }
}
