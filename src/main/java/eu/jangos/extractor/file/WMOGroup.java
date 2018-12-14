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
import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBspNode;
import eu.jangos.extractor.file.common.CImVector;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.wmo.group.LiquidTypeEnum;
import eu.jangos.extractor.file.wmo.group.MLIQ;
import eu.jangos.extractor.file.wmo.group.MOBA;
import eu.jangos.extractor.file.wmo.group.MOGP;
import eu.jangos.extractor.file.wmo.group.MOPY;
import eu.mangos.shared.flags.FlagUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class WMOGroup {

    // Section for WMO Group File.
    private static final int SUPPORTED_GROUP_VERSION = 17;

    private static final String HEADER_MVER = "MVER";
    private static final String HEADER_MOGP = "MOGP";
    private static final String HEADER_MOPY = "MOPY";
    private static final String HEADER_MOVI = "MOVI";
    private static final String HEADER_MOVT = "MOVT";
    private static final String HEADER_MONR = "MONR";
    private static final String HEADER_MOTV = "MOTV";
    private static final String HEADER_MOBA = "MOBA";
    private static final String HEADER_MOLR = "MOLR";
    private static final String HEADER_MODR = "MODR";
    private static final String HEADER_MOBN = "MOBN";
    private static final String HEADER_MOBR = "MOBR";
    private static final String HEADER_MOCV = "MOCV";
    private static final String HEADER_MLIQ = "MLIQ";
    private static final String HEADER_MORI = "MORI";

    // Size of one record for the given chunk.
    private static final int SIZE_MOPY = 2;
    private static final int SIZE_MOVI = 2;
    private static final int SIZE_MOVT = 12;
    private static final int SIZE_MONR = 12;
    private static final int SIZE_MOTV = 8;
    private static final int SIZE_MOBA = 24;
    private static final int SIZE_MOLR = 2;
    private static final int SIZE_MODR = 2;
    private static final int SIZE_MOBN = 16;
    private static final int SIZE_MOBR = 2;
    private static final int SIZE_MOCV = 4;
    private static final int SIZE_MORI = 2;
    
    // Flag values
    public static final long FLAG_HAS_BSP_TREE = 0x1;
    public static final long FLAG_HAS_LIGHT_MAP = 0x2;
    public static final long FLAG_HAS_VERTEX_COLORS  = 0x4;
    public static final long FLAG_IS_EXTERIOR  = 0x8;
    public static final long FLAG_UNUSED_1  = 0x10;
    public static final long FLAG_UNUSED_2  = 0x20;
    public static final long FLAG_EXTERIOR_LIT  = 0x40;
    public static final long FLAG_UNREACHABLE  = 0x80;
    public static final long FLAG_UNUSED_3  = 0x100;
    public static final long FLAG_HAS_LIGHT  = 0x200;
    public static final long FLAG_UNUSED_4  = 0x400;
    public static final long FLAG_HAS_DOODADS  = 0x800;
    public static final long FLAG_HAS_LIQUID  = 0x1000;
    public static final long FLAG_IS_INTERIOR  = 0x2000;
    public static final long FLAG_UNUSED_5  = 0x4000;
    public static final long FLAG_UNUSED_6  = 0x8000;
    public static final long FLAG_ALWAYS_DRAW  = 0x10000;
    public static final long FLAG_HAS_TRIANGLESTRIP  = 0x20000;
    public static final long FLAG_SHOW_SKYBOX  = 0x40000;
    public static final long FLAG_IS_OCEAN  = 0x80000;
    public static final long FLAG_UNUSED_8  = 0x100000;
    public static final long FLAG_IS_MOUNT_ALLOWED  = 0x200000;
    public static final long FLAG_UNUSED_9  = 0x400000;
    public static final long FLAG_UNUSED_10  = 0x800000;
    public static final long FLAG_HAS_2_MOCV  = 0x1000000;
    public static final long FLAG_HAS_2_MOTV  = 0x2000000;
    public static final long FLAG_ANTIPORTAL  = 0x4000000;
    public static final long FLAG_UNUSED_11  = 0x8000000;
    public static final long FLAG_UNUSED_12  = 0x10000000;
    public static final long FLAG_EXTERIOR_CULL  = 0x20000000;
    public static final long FLAG_HAS_3_MOTV  = 0x40000000;
    public static final long FLAG_UNUSED_13  = 0x80000000;        
    
    public static final int LIQUID_BASIC_TYPES_WATER = 0;
    public static final int LIQUID_BASIC_TYPES_OCEAN = 1;
    public static final int LIQUID_BASIC_TYPES_MAGMA = 2;
    public static final int LIQUID_BASIC_TYPES_SLIME = 3;
    public static final int LIQUID_BASIC_TYPES_MASK = 3;    
    public static final int LIQUID_WMO_WATER = 13;
    public static final int LIQUID_WMO_OCEAN = 14;
    public static final int LIQUID_GREEN_LAVA = 15;
    public static final int LIQUID_WMO_MAGMA = 19;
    public static final int LIQUID_WMO_SLIME = 20;
    public static final int LIQUID_FIRST_NON_BASIC_LIQUID_TYPE = 21;
    public static final int LIQUID_NAXX_SLIME = 21;  
    
    private ByteBuffer data;
    private String filename;

    private MOGP group = new MOGP();
    private List<MOPY> materialsList = new ArrayList<>();
    private List<Short> indexList = new ArrayList<>();
    private List<Vec3f> vertexList = new ArrayList<>();
    private List<Vec3f> normalList = new ArrayList<>();
    private List<Vec2f> textureVertexList = new ArrayList<>();
    private List<MOBA> batchList = new ArrayList<>();
    private List<Short> lightRefList = new ArrayList<>();
    private List<Short> doodadRefList = new ArrayList<>();
    private List<CAaBspNode> bspTreeList = new ArrayList<>();
    private List<Short> nodeFaceIndices = new ArrayList<>();
    private List<CImVector> colorVertexList = new ArrayList<>();
    private MLIQ liquid = new MLIQ();
    private List<Short> triangleStripIndices = new ArrayList<>();

    public void init(byte[] array, String filename) throws WMOException {
        this.data = ByteBuffer.wrap(array);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        this.filename = filename;
        
        checkHeader(HEADER_MVER);
        // Let's skip size.
        this.data.getInt();

        int version = this.data.getInt();
        if (version != SUPPORTED_GROUP_VERSION) {
            throw new WMOException("Group file version is not supported, supported version: " + SUPPORTED_GROUP_VERSION + ", actual version: " + version);
        }

        int chunkSize = 0;
        clear();

        checkHeader(HEADER_MOGP);
        // Skip size.
        chunkSize = this.data.getInt();
        this.group.read(data);

        checkHeader(HEADER_MOPY);
        chunkSize = data.getInt() / SIZE_MOPY;
        MOPY material;
        for (int i = 0; i < chunkSize; i++) {
            material = new MOPY();
            material.read(data);
            this.materialsList.add(material);
        }

        checkHeader(HEADER_MOVI);
        chunkSize = data.getInt() / SIZE_MOVI;
        for (int i = 0; i < chunkSize; i++) {
            indexList.add(data.getShort());
        }

        checkHeader(HEADER_MOVT);
        chunkSize = data.getInt() / SIZE_MOVT;
        for (int i = 0; i < chunkSize; i++) {
            vertexList.add(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        }

        checkHeader(HEADER_MONR);
        chunkSize = data.getInt() / SIZE_MONR;
        for (int i = 0; i < chunkSize; i++) {
            normalList.add(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        }

        checkHeader(HEADER_MOTV);
        chunkSize = data.getInt() / SIZE_MOTV;
        for (int i = 0; i < chunkSize; i++) {
            textureVertexList.add(new Vec2f(data.getFloat(), data.getFloat()));
        }

        checkHeader(HEADER_MOBA);
        chunkSize = data.getInt() / SIZE_MOBA;
        MOBA batch;
        for (int i = 0; i < chunkSize; i++) {
            batch = new MOBA();
            batch.read(data);
            batchList.add(batch);
        }

        if (hasLight()) {
            checkHeader(HEADER_MOLR);
            chunkSize = data.getInt() / SIZE_MOLR;
            for (int i = 0; i < chunkSize; i++) {
                lightRefList.add(data.getShort());
            }
        }

        if (hasDoodad()) {
            checkHeader(HEADER_MODR);
            chunkSize = data.getInt() / SIZE_MODR;
            for (int i = 0; i < chunkSize; i++) {
                doodadRefList.add(data.getShort());
            }
        }

        if (hasBSPTree()) {
            checkHeader(HEADER_MOBN);
            chunkSize = data.getInt() / SIZE_MOBN;
            CAaBspNode node;
            for (int i = 0; i < chunkSize; i++) {
                node = new CAaBspNode();
                node.read(data);
                bspTreeList.add(node);
            }

            checkHeader(HEADER_MOBR);
            chunkSize = data.getInt() / SIZE_MOBR;
            for (int i = 0; i < chunkSize; i++) {
                nodeFaceIndices.add(data.getShort());
            }
        }

        if (hasVertexColors()) {
            checkHeader(HEADER_MOCV);
            chunkSize = data.getInt() / SIZE_MOCV;
            CImVector vector;
            for (int i = 0; i < chunkSize; i++) {
                vector = new CImVector();
                vector.read(data);
                colorVertexList.add(vector);
            }
        }

        if (hasLiquid()) {                    
            checkHeader(HEADER_MLIQ);
            chunkSize = data.getInt();
            
            LiquidTypeEnum liquidType = LiquidTypeEnum.LIQUID_UNKNOWN;            
            if (this.group.getGroupLiquid() < LiquidTypeEnum.LIQUID_FIRST_NONBASIC_LIQUID_TYPE) {                
                liquidType = LiquidTypeEnum.getLiquidToWMO(this.group.getGroupLiquid(), this.group.getFlags());
            } else {                
                liquidType = LiquidTypeEnum.convert(this.group.getGroupLiquid());
            }
                   
            liquid.read(data, liquidType);            
        }

        if (hasTriangleStrip()) {
            checkHeader(HEADER_MORI);
            chunkSize = data.getInt() / SIZE_MORI;
            for (int i = 0; i < chunkSize; i++) {
                triangleStripIndices.add(data.getShort());
            }
        }
    }    
    
    private boolean hasFlag(long flag) {
        return FlagUtils.hasFlag(this.group.getFlags(), flag);
    }
    
    public boolean hasLight() {
        return hasFlag(FLAG_HAS_LIGHT);
    }
    
    public boolean hasLiquid() {
        return hasFlag(FLAG_HAS_LIQUID);
    }
    
    public boolean hasDoodad() {
        return hasFlag(FLAG_HAS_DOODADS);
    }
    
    public boolean hasBSPTree() {
        return hasFlag(FLAG_HAS_BSP_TREE);
    }
    
    public boolean hasVertexColors() {
        return hasFlag(FLAG_HAS_VERTEX_COLORS);
    }
    
    public boolean hasTriangleStrip() {
        return hasFlag(FLAG_HAS_TRIANGLESTRIP);
    }
    
    public boolean isOcean() {
        return hasFlag(FLAG_IS_OCEAN);
    }
    
    public boolean isMagma() {
        return false;
    }
    
    
    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public MOGP getGroup() {
        return group;
    }

    public void setGroup(MOGP group) {
        this.group = group;
    }    
    
    public List<MOPY> getMaterialsList() {
        return materialsList;
    }

    public void setMaterialsList(List<MOPY> materialsList) {
        this.materialsList = materialsList;
    }

    public List<Short> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Short> indexList) {
        this.indexList = indexList;
    }

    public List<Vec3f> getVertexList() {
        return vertexList;
    }

    public void setVertexList(List<Vec3f> vertexList) {
        this.vertexList = vertexList;
    }

    public List<Vec3f> getNormalList() {
        return normalList;
    }

    public void setNormalList(List<Vec3f> normalList) {
        this.normalList = normalList;
    }

    public List<Vec2f> getTextureVertexList() {
        return textureVertexList;
    }

    public void setTextureVertexList(List<Vec2f> textureVertexList) {
        this.textureVertexList = textureVertexList;
    }

    public List<MOBA> getBatchList() {
        return batchList;
    }

    public void setBatchList(List<MOBA> batchList) {
        this.batchList = batchList;
    }

    public List<Short> getLightRefList() {
        return lightRefList;
    }

    public void setLightRefList(List<Short> lightRefList) {
        this.lightRefList = lightRefList;
    }

    public List<Short> getDoodadRefList() {
        return doodadRefList;
    }

    public void setDoodadRefList(List<Short> doodadRefList) {
        this.doodadRefList = doodadRefList;
    }

    public List<CAaBspNode> getBspTreeList() {
        return bspTreeList;
    }

    public void setBspTreeList(List<CAaBspNode> bspTreeList) {
        this.bspTreeList = bspTreeList;
    }

    public List<Short> getNodeFaceIndices() {
        return nodeFaceIndices;
    }

    public void setNodeFaceIndices(List<Short> nodeFaceIndices) {
        this.nodeFaceIndices = nodeFaceIndices;
    }

    public List<CImVector> getColorVertexList() {
        return colorVertexList;
    }

    public void setColorVertexList(List<CImVector> colorVertexList) {
        this.colorVertexList = colorVertexList;
    }

    public MLIQ getLiquid() {
        return liquid;
    }

    public void setLiquid(MLIQ liquid) {
        this.liquid = liquid;
    }

    public List<Short> getTriangleStripIndices() {
        return triangleStripIndices;
    }

    public void setTriangleStripIndices(List<Short> triangleStripIndices) {
        this.triangleStripIndices = triangleStripIndices;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }                    
    
    private void checkHeader(String expectedHeader) throws WMOException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new WMOException(this.filename + " - Expected header " + expectedHeader + ", received header: " + sb.toString());
        }
    }

    private void clear() {
        materialsList.clear();
        indexList.clear();
        vertexList.clear();
        normalList.clear();
        textureVertexList.clear();
        batchList.clear();
        lightRefList.clear();
        doodadRefList.clear();
        bspTreeList.clear();
        nodeFaceIndices.clear();
        colorVertexList.clear();        
        triangleStripIndices.clear();
    }

}
