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

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBox;
import eu.jangos.extractor.file.common.CArgb;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.wmo.WMOGroupInfo;
import eu.jangos.extractor.file.wmo.WMOMaterials;
import eu.jangos.extractor.file.wmo.WMOPortal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class WMOFileReader {

    private static final String HEADER_MVER = "MVER";
    private static final String HEADER_MOHD = "MOHD";
    private static final String HEADER_MOTX = "MOTX";
    private static final String HEADER_MOMT = "MOMT";
    private static final int SIZE_MOMT = 64;
    private static final String HEADER_MOGN = "MOGN";
    private static final String HEADER_MOGI = "MOGI";
    private static final int SIZE_MOGI = 32;
    private static final String HEADER_MOSB = "MOSB";    
    private static final String HEADER_MOPV = "MOPV";
    private static final int SIZE_MOPV = 12;
    private static final String HEADER_MOPT = "MOPT";
    private static final int SIZE_MOPT = 20;
    
    private static final int SUPPORTED_VERSION = 17;

    private ByteBuffer data;
    private boolean init = false;

    private int nTextures;
    private int nGroups;
    private int nPortals;
    private int nLights;
    private int nDoodadNames;
    private int nDoodadDefs;
    private int nDoodadSets;
    private CArgb ambColor = new CArgb();
    /**
     * Foreign key to WMOAreaTAble.dbc.
     */
    private int wmoAreaTableID;
    private CAaBox boundingBox = new CAaBox();
    private short doNotAttenuateVerticesBasedOnDistanceToPortal;
    private short useUnifiedRenderPath;
    private short useLiquidTypeDBC;
    private short doNotFixVertexColorAlpha;
    private short lod;
    private short numLod;

    private List<String> textureNameList = new ArrayList<>();
    private List<WMOMaterials> materials = new ArrayList<>();
    private List<String> groupNameList = new ArrayList<>();
    private List<WMOGroupInfo> groupInfoList = new ArrayList<>();
    private String skyBoxName;
    private List<Vec3f> portalVertexList = new ArrayList<>();
    private List<WMOPortal> portalList = new ArrayList<>();
    
    public WMOFileReader() {

    }

    public void init(byte[] array) throws WMOException {
        this.data = ByteBuffer.wrap(array);
        this.data.order(ByteOrder.LITTLE_ENDIAN);

        checkHeader(HEADER_MVER);
        // Skip the size.
        this.data.getInt();
        int version = this.data.getInt();
        if (version != SUPPORTED_VERSION) {
            throw new WMOException("WMO Version is not supported by this reader.");
        }

        int chunkSize = 0;
        
        // We're now reading the header of the file.
        checkHeader(HEADER_MOHD);

        this.nGroups = this.data.getInt();
        this.nPortals = this.data.getInt();
        this.nLights = this.data.getInt();
        this.nDoodadNames = this.data.getInt();
        this.nDoodadDefs = this.data.getInt();
        this.nDoodadSets = this.data.getInt();
        this.ambColor.setR(this.data.get());
        this.ambColor.setG(this.data.get());
        this.ambColor.setB(this.data.get());
        this.ambColor.setA(this.data.get());
        this.wmoAreaTableID = this.data.getInt();
        this.boundingBox.setMin(new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat()));
        this.boundingBox.setMax(new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat()));
        this.doNotAttenuateVerticesBasedOnDistanceToPortal = this.data.getShort();
        this.useUnifiedRenderPath = this.data.getShort();
        this.useLiquidTypeDBC = this.data.getShort();
        this.doNotFixVertexColorAlpha = this.data.getShort();
        this.lod = this.data.getShort();
        this.numLod = this.data.getShort();

        this.textureNameList = readStringChunk(this.data.position(), HEADER_MOTX);

        checkHeader(HEADER_MOMT);        
        chunkSize = data.getInt() / SIZE_MOMT;
        
        WMOMaterials material;
        for(int i = 0; i < chunkSize; i++) {
            material = new WMOMaterials();
            material.read(this.data);
            this.materials.add(material);
        }        
        
        this.groupNameList = readStringChunk(this.data.position(), HEADER_MOGN);
        
        checkHeader(HEADER_MOGI);
        chunkSize = data.getInt() / SIZE_MOGI;
        
        WMOGroupInfo groupInfo;
        for(int i = 0; i < chunkSize; i++) {
            groupInfo = new WMOGroupInfo();
            groupInfo.read(this.data);
            groupInfoList.add(groupInfo);
        }
        
        checkHeader(HEADER_MOSB);
        chunkSize = this.data.getInt();        
        this.skyBoxName = readString(this.data);
        // No skyBoxName, moving further to MOPV header.
        if(this.skyBoxName.isEmpty()) {
            this.data.position(this.data.position() + chunkSize - 1);
        }
        
        checkHeader(HEADER_MOPV);
        chunkSize = this.data.getInt() / SIZE_MOPV;
        
        Vec3f portalVertex;
        for(int i = 0; i < chunkSize; i++) {
            portalVertex = new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat());
            this.portalVertexList.add(portalVertex);
        }
        
        checkHeader(HEADER_MOPT);
        chunkSize = this.data.getInt() / SIZE_MOPT;
        
        WMOPortal portal;
        for(int i = 0; i < chunkSize; i++) {
            portal = new WMOPortal();
            portal.read(this.data);
            this.portalList.add(portal);
        }
        
        System.out.println(this.data.position());
    }

    private List<String> readStringChunk(int offset, String expectedHeader) throws WMOException {
        List<String> stringList = new ArrayList<>();

        this.data.position(offset);

        checkHeader(expectedHeader);

        int size = this.data.getInt();
        int start = this.data.position();
        String temp;
        while (this.data.position() - start < size) {
            temp = readString(this.data);
            if (!temp.isEmpty()) {
                stringList.add(temp);
            }
        }

        return stringList;
    }

    private String readString(ByteBuffer in) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    private void checkHeader(String expectedHeader) throws WMOException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new WMOException("Expected header " + expectedHeader + ", received header: " + sb.toString());
        }
    }
}
