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
 *
 * @author Warkdev
 */
public class WDT extends FileReader {

    private static final String HEADER_MVER = "MVER";
    private static final String HEADER_MPHD = "MPHD";
    private static final String HEADER_MAIN = "MAIN";
    private static final String HEADER_MWMO = "MWMO";
    private static final String HEADER_MODF = "MODF";

    private static final int SUPPORTED_VERSION = 18;
    private static final int SIZE_AREA_INFO = 8;

    public static final int FLAG_USE_GLOBAL_MAP_OBJ = 0x0001;

    public static final int MAP_TILE_SIZE = 64;

    private int version;

    private int flags;

    private List<AreaInfo> listAreas = new ArrayList<>();

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

        super.init = true;
    }

    public boolean useGlobalMapObj() {
        return hasFlag(FLAG_USE_GLOBAL_MAP_OBJ);
    }

    public boolean[][] getTileMap() {
        boolean[][] tileMap = new boolean[MAP_TILE_SIZE][MAP_TILE_SIZE];

        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                tileMap[i][j] = hasAdt(i, j);
            }
        }

        return tileMap;
    }

    public boolean hasAdt(int row, int col) {
        return this.listAreas.get(row * MAP_TILE_SIZE + col).hasADT();
    }

    public void saveTileMap(String path) throws IOException {
        BufferedImage img = new BufferedImage(MAP_TILE_SIZE, MAP_TILE_SIZE, BufferedImage.TYPE_INT_RGB);        

        for (int i = 0; i < MAP_TILE_SIZE; i++) {
            for (int j = 0; j < MAP_TILE_SIZE; j++) {
                if (hasAdt(i, j)) {
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

    private boolean hasFlag(int flag) {
        return FlagUtils.hasFlag(this.flags, flag);
    }
}
