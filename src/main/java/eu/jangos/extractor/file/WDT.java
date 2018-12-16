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

import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.WDTException;
import eu.jangos.extractor.file.wdt.AreaInfo;
import eu.mangos.shared.flags.FlagUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * WDT represents a WDT file from WoW package. This class allows to read its definition.
 * @author Warkdev
 */
public class WDT extends FileReader {

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

    @Override
    public void init(byte[] in, String filename) throws FileReaderException {
        super.init = false;
        listAreas.clear();

        super.filename = filename;
        super.data = ByteBuffer.wrap(in);
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
        
        if(useGlobalMapObj()) {
            // There should be one WMO definition with its placement information.
            wmo = readString(super.data);
            
            checkHeader(HEADER_MODF);
            size = super.data.getInt();
            this.wmoPlacement = new MODF();
            this.wmoPlacement.read(super.data);
        }                
        
        super.init = true;
    }

    /**
     * Indicates whether this WDT has only a WMO in it (as a dungeon).
     * @return True if this WDT is only storing a WMO information or false if it also has terrain information.
     */
    public boolean useGlobalMapObj() {
        return hasFlag(FLAG_USE_GLOBAL_MAP_OBJ);
    }

    /**
     * Returns the tilemap corresponding to the terrain definition. Each tile is represented by a boolean value indicating whether this tile has terrain information.
     * @return An array of array of boolean representing the terrain information stored in this WDT file.
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
     * Indicates whether the tile at the position row/col has a terrain information (under the format of an ADT file) or not.
     * @param row The row of the tile.
     * @param col The column of the tile.
     * @return True if the tile has a terrain information, false otherwise.
     */
    public boolean hasTerrain(int row, int col) {
        return this.listAreas.get(row * MAP_TILE_SIZE + col).hasADT();
    }

    /**
     * Save the Terrain data information under the format of a TileMap (PNG file).
     * If the file already exist, this method erase it before creating a new one.
     * @param path Path of the file to be created.
     * @throws IOException IOException is throw by the file handler if there's any issue with the filesystem.
     */
    public void saveTileMap(String path) throws IOException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE, MAP_TILE_SIZE, BufferedImage.TYPE_INT_RGB);        

        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                if (hasTerrain(i, j)) {
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
     * Read the version of the file. If the file version doesn't match the version header, it throws an exception.
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
     * @param flag The position of the bit that must be checked.
     * @return True if the bit is set, false otherwise.
     */
    private boolean hasFlag(int flag) {
        return FlagUtils.hasFlag(this.flags, flag);
    }
}
