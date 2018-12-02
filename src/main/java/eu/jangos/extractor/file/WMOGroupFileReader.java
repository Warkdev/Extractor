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
import eu.jangos.extractor.file.common.CImVector;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.common.CAaBspNode;
import eu.jangos.extractor.file.wmo.group.LiquidTypeEnum;
import eu.jangos.extractor.file.wmo.group.MLIQ;
import eu.jangos.extractor.file.wmo.group.MOBA;
import eu.jangos.extractor.file.wmo.group.MOGP;
import eu.jangos.extractor.file.wmo.group.MOGPFlagEnum;
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
public class WMOGroupFileReader {

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

    private ByteBuffer data;

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

    public void init(byte[] array, short headerFlags) throws WMOException {
        this.data = ByteBuffer.wrap(array);
        this.data.order(ByteOrder.LITTLE_ENDIAN);

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

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_LIGHT.getValue())) {
            checkHeader(HEADER_MOLR);
            chunkSize = data.getInt() / SIZE_MOLR;
            for (int i = 0; i < chunkSize; i++) {
                lightRefList.add(data.getShort());
            }
        }

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_DOODADS.getValue())) {
            checkHeader(HEADER_MODR);
            chunkSize = data.getInt() / SIZE_MODR;
            for (int i = 0; i < chunkSize; i++) {
                doodadRefList.add(data.getShort());
            }
        }

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_BSP_TREE.getValue())) {
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

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_VERTEX_COLORS.getValue())) {
            checkHeader(HEADER_MOCV);
            chunkSize = data.getInt() / SIZE_MOCV;
            CImVector vector;
            for (int i = 0; i < chunkSize; i++) {
                vector = new CImVector();
                vector.read(data);
                colorVertexList.add(vector);
            }
        }

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_WATER.getValue())) {
            checkHeader(HEADER_MLIQ);
            chunkSize = data.getInt();
            
            LiquidTypeEnum liquidType = LiquidTypeEnum.LIQUID_UNKNOWN;
            if (this.group.getGroupLiquid() < LiquidTypeEnum.LIQUID_FIRST_NONBASIC_LIQUID_TYPE) {
                liquidType = LiquidTypeEnum.LIQUID_UNKNOWN.getLiquidToWMO(this.group.getGroupLiquid(), headerFlags, this.group.getFlags());
            } else {
                liquidType = LiquidTypeEnum.LIQUID_UNKNOWN.convert(this.group.getGroupLiquid());
            }
                   
            liquid.read(data, liquidType);            
        }

        if (FlagUtils.hasFlag(group.getFlags(), MOGPFlagEnum.HAS_TRIANGLESTRIP.getValue())) {
            checkHeader(HEADER_MORI);
            chunkSize = data.getInt() / SIZE_MORI;
            for (int i = 0; i < chunkSize; i++) {
                triangleStripIndices.add(data.getShort());
            }
        }
    }

    private void checkHeader(String expectedHeader) throws WMOException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new WMOException("Expected header " + expectedHeader + ", received header: " + sb.toString());
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
