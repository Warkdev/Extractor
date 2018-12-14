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
import eu.jangos.extractor.file.common.C4Plane;
import eu.jangos.extractor.file.common.CAaBox;
import eu.jangos.extractor.file.common.CArgb;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.wmo.WMOBlock;
import eu.jangos.extractor.file.wmo.WMODoodadDef;
import eu.jangos.extractor.file.wmo.WMODoodadSet;
import eu.jangos.extractor.file.wmo.WMOFog;
import eu.jangos.extractor.file.wmo.WMOGroupInfo;
import eu.jangos.extractor.file.wmo.WMOLight;
import eu.jangos.extractor.file.wmo.WMOMaterials;
import eu.jangos.extractor.file.wmo.WMOPortal;
import eu.jangos.extractor.file.wmo.WMOPortalRef;
import eu.mangos.shared.flags.FlagUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Warkdev
 */
public class WMO extends FileReader {

    // Section for WMO ROOT File.
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
    private static final String HEADER_MOPR = "MOPR";
    private static final int SIZE_MOPR = 8;
    private static final String HEADER_MOVV = "MOVV";
    private static final int SIZE_MOVV = 12;
    private static final String HEADER_MOVB = "MOVB";
    private static final int SIZE_MOVB = 4;
    private static final String HEADER_MOLT = "MOLT";
    private static final int SIZE_MOLT = 48;
    private static final String HEADER_MODS = "MODS";
    private static final int SIZE_MODS = 32;
    private static final String HEADER_MODN = "MODN";
    private static final String HEADER_MODD = "MODD";
    private static final int SIZE_MODD = 40;
    private static final String HEADER_MFOG = "MFOG";
    private static final int SIZE_MFOG = 48;
    private static final String HEADER_MCVP = "MCVP";
    private static final int SIZE_MCVP = 16;

    public static final int FLAG_USE_LIQUID_FROM_DBC = 0x04;
    
    private static final int SUPPORTED_VERSION = 17;

    private int nMaterials;
    private int nGroups;
    private int nPortals;
    private int nLights;
    private int nModels;
    private int nDoodads;
    private int nDoodaSets;

    private CArgb ambColor = new CArgb();

    /**
     * Foreign key to WMOAreaTAble.dbc.
     */
    private int wmoAreaTableID;
    private CAaBox boundingBox = new CAaBox();
    private short flags;
    private short numLod;

    private List<String> textureNameList = new ArrayList<>();
    private List<WMOMaterials> materials = new ArrayList<>();
    private List<String> groupNameList = new ArrayList<>();
    private List<WMOGroupInfo> groupInfoList = new ArrayList<>();
    private String skyBoxName;
    private List<Vec3f> portalVertexList = new ArrayList<>();
    private List<WMOPortal> portalList = new ArrayList<>();
    private List<WMOPortalRef> portalRefList = new ArrayList<>();
    private List<Vec3f> visibleBlockVerticesList = new ArrayList<>();
    private List<WMOBlock> visibleBlockList = new ArrayList<>();
    private List<WMOLight> lightList = new ArrayList<>();
    private List<WMODoodadSet> doodadSetList = new ArrayList<>();
    private Map<Integer, String> doodadNameMap = new HashMap<>();
    private List<WMODoodadDef> doodadDefList = new ArrayList<>();
    private List<WMOFog> fogList = new ArrayList<>();
    private List<C4Plane> convexVolumePlanesList = new ArrayList<>();

    private List<WMOGroup> wmoGroupReadersList = new ArrayList<>();

    public boolean isRootFile(byte[] array) throws FileReaderException {
        super.data = ByteBuffer.wrap(array);
        super.data.order(ByteOrder.LITTLE_ENDIAN);

        checkHeader(HEADER_MVER);
        // Skip the size.
        super.data.getInt();
        int version = super.data.getInt();
        if (version != SUPPORTED_VERSION) {
            throw new WMOException("WMO Version is not supported by this reader.");
        }

        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        data.get(header);

        sb = sb.append(new String(header)).reverse();

        if (!sb.toString().equals(HEADER_MOHD)) {
            return false;
        }

        return true;
    }

    @Override
    public void init(byte[] array, String filename) throws FileReaderException {
        super.data = ByteBuffer.wrap(array);
        super.data.order(ByteOrder.LITTLE_ENDIAN);
        super.filename = filename;
        clear();

        checkHeader(HEADER_MVER);
        // Skip the size.
        super.data.getInt();
        int version = super.data.getInt();
        if (version != SUPPORTED_VERSION) {
            throw new WMOException("WMO Version is not supported by this reader.");
        }

        int chunkSize = 0;        
        
        // We're now reading the header of the file.
        checkHeader(HEADER_MOHD);
        // Skip the size.
        super.data.getInt();

        this.nMaterials = super.data.getInt();
        this.nGroups = super.data.getInt();
        this.nPortals = super.data.getInt();
        this.nLights = super.data.getInt();
        this.nModels = super.data.getInt();
        this.nDoodads = super.data.getInt(); // Suppossingly amount of MODD
        this.nDoodaSets = super.data.getInt(); // Supposingly amount of MODS
        this.ambColor.setR(super.data.get());
        this.ambColor.setG(super.data.get());
        this.ambColor.setB(super.data.get());
        this.ambColor.setA(super.data.get());
        this.wmoAreaTableID = super.data.getInt();
        this.boundingBox.setMin(new Vec3f(super.data.getFloat(), super.data.getFloat(), super.data.getFloat()));
        this.boundingBox.setMax(new Vec3f(super.data.getFloat(), super.data.getFloat(), super.data.getFloat()));
        this.flags = super.data.getShort();
        this.numLod = super.data.getShort();

        this.textureNameList = readStringChunk(super.data.position(), HEADER_MOTX);

        checkHeader(HEADER_MOMT);
        chunkSize = data.getInt() / SIZE_MOMT;

        WMOMaterials material;
        for (int i = 0; i < nMaterials; i++) {
            material = new WMOMaterials();
            material.read(super.data);
            this.materials.add(material);
        }

        this.groupNameList = readStringChunk(super.data.position(), HEADER_MOGN);

        checkHeader(HEADER_MOGI);
        chunkSize = data.getInt() / SIZE_MOGI;

        WMOGroupInfo groupInfo;
        for (int i = 0; i < nGroups; i++) {
            groupInfo = new WMOGroupInfo();
            groupInfo.read(super.data);
            groupInfoList.add(groupInfo);
        }

        checkHeader(HEADER_MOSB);
        chunkSize = super.data.getInt();
        this.skyBoxName = readString(super.data);
        // There are 0 padding at the end of the String.
        if (this.skyBoxName.length() < chunkSize) {
            super.data.position(super.data.position() + (chunkSize - this.skyBoxName.length()) - 1);
        }

        checkHeader(HEADER_MOPV);
        chunkSize = super.data.getInt() / SIZE_MOPV;

        Vec3f portalVertex;
        for (int i = 0; i < chunkSize; i++) {
            portalVertex = new Vec3f(super.data.getFloat(), super.data.getFloat(), super.data.getFloat());
            this.portalVertexList.add(portalVertex);
        }

        checkHeader(HEADER_MOPT);
        chunkSize = super.data.getInt() / SIZE_MOPT;

        WMOPortal portal;
        for (int i = 0; i < nPortals; i++) {
            portal = new WMOPortal();
            portal.read(super.data);
            this.portalList.add(portal);
        }

        checkHeader(HEADER_MOPR);
        chunkSize = super.data.getInt() / SIZE_MOPR;

        WMOPortalRef portalRef;
        for (int i = 0; i < chunkSize; i++) {
            portalRef = new WMOPortalRef();
            portalRef.read(super.data);
            this.portalRefList.add(portalRef);
        }

        checkHeader(HEADER_MOVV);
        chunkSize = super.data.getInt() / SIZE_MOVV;

        Vec3f visibleBlockVertices;
        for (int i = 0; i < chunkSize; i++) {
            visibleBlockVertices = new Vec3f();
            visibleBlockVertices.set(super.data.getFloat(), super.data.getFloat(), super.data.getFloat());
            this.visibleBlockVerticesList.add(visibleBlockVertices);
        }

        checkHeader(HEADER_MOVB);
        chunkSize = super.data.getInt() / SIZE_MOVB;

        WMOBlock visibleBlock;
        for (int i = 0; i < chunkSize; i++) {
            visibleBlock = new WMOBlock();
            visibleBlock.read(super.data);
            this.visibleBlockList.add(visibleBlock);
        }

        checkHeader(HEADER_MOLT);
        chunkSize = super.data.getInt() / SIZE_MOLT;

        WMOLight light;
        for (int i = 0; i < chunkSize; i++) {
            light = new WMOLight();
            light.read(super.data);
            this.lightList.add(light);
        }

        checkHeader(HEADER_MODS);
        chunkSize = super.data.getInt() / SIZE_MODS;

        WMODoodadSet doodadSet;
        for (int i = 0; i < chunkSize; i++) {
            doodadSet = new WMODoodadSet();
            doodadSet.read(super.data);
            this.doodadSetList.add(doodadSet);
        }

        this.doodadNameMap = readStringChunkAsMap(super.data.position(), HEADER_MODN);

        checkHeader(HEADER_MODD);
        chunkSize = super.data.getInt() / SIZE_MODD;

        WMODoodadDef doodadDef;
        for (int i = 0; i < chunkSize; i++) {
            doodadDef = new WMODoodadDef();
            doodadDef.read(super.data);
            this.doodadDefList.add(doodadDef);
        }

        checkHeader(HEADER_MFOG);
        chunkSize = super.data.getInt() / SIZE_MFOG;

        WMOFog fog;
        for (int i = 0; i < chunkSize; i++) {
            fog = new WMOFog();
            fog.read(super.data);
            this.fogList.add(fog);
        }

        // MCVP is an optional header.
        if (data.hasRemaining()) {
            checkHeader(HEADER_MCVP);
            chunkSize = super.data.getInt() / SIZE_MCVP;

            C4Plane convexVolume;
            for (int i = 0; i < chunkSize; i++) {
                convexVolume = new C4Plane();
                convexVolume.getNormal().set(super.data.getFloat(), super.data.getFloat(), super.data.getFloat());
                convexVolume.setDistance(super.data.getFloat());
                this.convexVolumePlanesList.add(convexVolume);
            }
        }

        init = true;
    }

    public void initGroup(byte[] array, String filename) throws WMOException {
        WMOGroup reader = new WMOGroup();
        reader.init(array, filename);
        this.wmoGroupReadersList.add(reader);
    }

    private void clear() {
        this.textureNameList.clear();
        this.materials.clear();
        this.groupNameList.clear();
        this.groupInfoList.clear();
        this.portalVertexList.clear();
        this.portalList.clear();
        this.portalRefList.clear();
        this.visibleBlockVerticesList.clear();
        this.visibleBlockList.clear();
        this.lightList.clear();
        this.doodadSetList.clear();
        this.doodadDefList.clear();
        this.doodadNameMap.clear();
        this.fogList.clear();
        this.convexVolumePlanesList.clear();
        this.wmoGroupReadersList.clear();
    }

    public String[][] getLiquidMap(boolean displayLiquidType) {
        String[][] liquidMap = new String[SIZE_MOMT][SIZE_MOMT];
        
        int x = 0;
        int y = 0;
                
        for(WMOGroup group : wmoGroupReadersList) {
            //System.out.println(super.filename+";"+group.getFilename()+";"+this.flags+";"+this.useDBCLiquidID()+";"+group.hasLiquid()+";"+group.getGroup().getGroupLiquid());
        }
        
        for(WMOGroup group : wmoGroupReadersList) {
            if(group.hasLiquid()) {
                x = group.getLiquid().getxTiles();
                y = group.getLiquid().getyTiles();
                System.out.println(group.getFilename()+";true;"+x + ";"+y+";"+group.getGroup().getBoundingBox().getMin().x
                        +";"+group.getGroup().getBoundingBox().getMin().y
                        +";"+group.getGroup().getBoundingBox().getMin().z
                        +";"+group.getGroup().getBoundingBox().getMax().x
                +";"+group.getGroup().getBoundingBox().getMax().x
                +";"+group.getGroup().getBoundingBox().getMax().z
                +";"+group.getLiquid().getBaseCoordinates().x
                +";"+group.getLiquid().getBaseCoordinates().y
                +";"+group.getLiquid().getBaseCoordinates().z);                
            } else {
                System.out.println(group.getFilename()+";false;0;0;"+group.getGroup().getBoundingBox().getMin().x
                        +";"+group.getGroup().getBoundingBox().getMin().y
                        +";"+group.getGroup().getBoundingBox().getMin().z
                        +";"+group.getGroup().getBoundingBox().getMax().x
                +";"+group.getGroup().getBoundingBox().getMax().x
                +";"+group.getGroup().getBoundingBox().getMax().z
                +";0;0;0");                
            }
        }          
        
        return liquidMap;
    }
    
    public boolean useDBCLiquidID() {
        return hasFlag(FLAG_USE_LIQUID_FROM_DBC);
    }
    
    private boolean hasFlag(int flag) {
        return FlagUtils.hasFlag(this.flags, flag);
    }
    
    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        super.data = data;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public int getnGroups() {
        return nGroups;
    }

    public void setnGroups(int nGroups) {
        this.nGroups = nGroups;
    }

    public int getnPortals() {
        return nPortals;
    }

    public void setnPortals(int nPortals) {
        this.nPortals = nPortals;
    }

    public int getnLights() {
        return nLights;
    }

    public void setnLights(int nLights) {
        this.nLights = nLights;
    }

    public int getnUnknown() {
        return nModels;
    }

    public void setnUnknown(int nUnknown) {
        this.nModels = nUnknown;
    }

    public int getnXX() {
        return nDoodads;
    }

    public void setnXX(int nXX) {
        this.nDoodads = nXX;
    }

    public int getnXXX() {
        return nDoodaSets;
    }

    public void setnXXX(int nXXX) {
        this.nDoodaSets = nXXX;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public short getNumLod() {
        return numLod;
    }

    public void setNumLod(short numLod) {
        this.numLod = numLod;
    }

    public int getnMaterials() {
        return nMaterials;
    }

    public void setnMaterials(int nMaterials) {
        this.nMaterials = nMaterials;
    }

    public CArgb getAmbColor() {
        return ambColor;
    }

    public void setAmbColor(CArgb ambColor) {
        this.ambColor = ambColor;
    }

    public int getWmoAreaTableID() {
        return wmoAreaTableID;
    }

    public void setWmoAreaTableID(int wmoAreaTableID) {
        this.wmoAreaTableID = wmoAreaTableID;
    }

    public CAaBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(CAaBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<String> getTextureNameList() {
        return textureNameList;
    }

    public void setTextureNameList(List<String> textureNameList) {
        this.textureNameList = textureNameList;
    }

    public List<WMOMaterials> getMaterials() {
        return materials;
    }

    public void setMaterials(List<WMOMaterials> materials) {
        this.materials = materials;
    }

    public List<String> getGroupNameList() {
        return groupNameList;
    }

    public void setGroupNameList(List<String> groupNameList) {
        this.groupNameList = groupNameList;
    }

    public List<WMOGroupInfo> getGroupInfoList() {
        return groupInfoList;
    }

    public void setGroupInfoList(List<WMOGroupInfo> groupInfoList) {
        this.groupInfoList = groupInfoList;
    }

    public String getSkyBoxName() {
        return skyBoxName;
    }

    public void setSkyBoxName(String skyBoxName) {
        this.skyBoxName = skyBoxName;
    }

    public List<Vec3f> getPortalVertexList() {
        return portalVertexList;
    }

    public void setPortalVertexList(List<Vec3f> portalVertexList) {
        this.portalVertexList = portalVertexList;
    }

    public List<WMOPortal> getPortalList() {
        return portalList;
    }

    public void setPortalList(List<WMOPortal> portalList) {
        this.portalList = portalList;
    }

    public List<WMOPortalRef> getPortalRefList() {
        return portalRefList;
    }

    public void setPortalRefList(List<WMOPortalRef> portalRefList) {
        this.portalRefList = portalRefList;
    }

    public List<Vec3f> getVisibleBlockVerticesList() {
        return visibleBlockVerticesList;
    }

    public void setVisibleBlockVerticesList(List<Vec3f> visibleBlockVerticesList) {
        this.visibleBlockVerticesList = visibleBlockVerticesList;
    }

    public List<WMOBlock> getVisibleBlockList() {
        return visibleBlockList;
    }

    public void setVisibleBlockList(List<WMOBlock> visibleBlockList) {
        this.visibleBlockList = visibleBlockList;
    }

    public List<WMOLight> getLightList() {
        return lightList;
    }

    public void setLightList(List<WMOLight> lightList) {
        this.lightList = lightList;
    }

    public List<WMODoodadSet> getDoodadSetList() {
        return doodadSetList;
    }

    public void setDoodadSetList(List<WMODoodadSet> doodadSetList) {
        this.doodadSetList = doodadSetList;
    }

    public int getnModels() {
        return nModels;
    }

    public void setnModels(int nModels) {
        this.nModels = nModels;
    }

    public int getnDoodads() {
        return nDoodads;
    }

    public void setnDoodads(int nDoodads) {
        this.nDoodads = nDoodads;
    }

    public int getnDoodaSets() {
        return nDoodaSets;
    }

    public void setnDoodaSets(int nDoodaSets) {
        this.nDoodaSets = nDoodaSets;
    }

    public Map<Integer, String> getDoodadNameMap() {
        return doodadNameMap;
    }

    public void setDoodadNameMap(Map<Integer, String> doodadNameMap) {
        this.doodadNameMap = doodadNameMap;
    }

    public List<WMODoodadDef> getDoodadDefList() {
        return doodadDefList;
    }

    public void setDoodadDefList(List<WMODoodadDef> doodadDefList) {
        this.doodadDefList = doodadDefList;
    }

    public List<WMOFog> getFogList() {
        return fogList;
    }

    public void setFogList(List<WMOFog> fogList) {
        this.fogList = fogList;
    }

    public List<C4Plane> getConvexVolumePlanesList() {
        return convexVolumePlanesList;
    }

    public void setConvexVolumePlanesList(List<C4Plane> convexVolumePlanesList) {
        this.convexVolumePlanesList = convexVolumePlanesList;
    }

    public List<WMOGroup> getWmoGroupReadersList() {
        return wmoGroupReadersList;
    }

    public void setWmoGroupReadersList(List<WMOGroup> wmoGroupReadersList) {
        this.wmoGroupReadersList = wmoGroupReadersList;
    }

    private Map<Integer, String> readStringChunkAsMap(int offset, String expectedHeader) throws FileReaderException {
        Map<Integer, String> stringMap = new HashMap<>();

        super.data.position(offset);

        checkHeader(expectedHeader);

        int size = super.data.getInt();
        int start = super.data.position();
        int recordOffset;
        String temp;
        while (super.data.position() - start < size) {
            recordOffset = super.data.position() - offset - 8;
            temp = readPaddedString(super.data, 4);
            if (!temp.isEmpty()) {
                stringMap.put(recordOffset, temp);
            }
        }

        return stringMap;
    }

    private String readString(ByteBuffer in) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                break;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private String readPaddedString(ByteBuffer in, int padding) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                // There's 0 padding at the end of string and we want to skip them except if there's only one 0.                
                int skip = padding - ((sb.length() + 1) % padding);
                in.position(in.position() + (skip == padding ? 0 : skip));
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }

}
