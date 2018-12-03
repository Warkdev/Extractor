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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class WMOFileReader {

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

    private static final int SUPPORTED_VERSION = 17;
    
    private ByteBuffer data;
    private boolean init = false;
        
    private int nMaterials;
    private int nGroups;
    private int nPortals;    
    private int nLights;    
    private int nUnknown;
    private int nXX;
    private int nXXX;
    
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
    private List<String> doodadNameList = new ArrayList<>();
    private List<WMODoodadDef> doodadDefList = new ArrayList<>();
    private List<WMOFog> fogList = new ArrayList<>();
    private List<C4Plane> convexVolumePlanesList = new ArrayList<>();

    private List<WMOGroupFileReader> wmoGroupReadersList = new ArrayList<>();
        
    public WMOFileReader() {

    }

    public void init(byte[] array) throws WMOException {
        this.data = ByteBuffer.wrap(array);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        clear();

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
        // Skip the size.
        this.data.getInt();            
                
        this.nMaterials = this.data.getInt();        
        this.nGroups = this.data.getInt();
        this.nPortals = this.data.getInt();
        this.nLights = this.data.getInt();
        this.nUnknown = this.data.getInt(); 
        this.nXX = this.data.getInt(); // Suppossingly amount of MODD
        this.nXXX = this.data.getInt(); // Supposingly amount of MODS
        this.ambColor.setR(this.data.get());
        this.ambColor.setG(this.data.get());
        this.ambColor.setB(this.data.get());
        this.ambColor.setA(this.data.get());
        this.wmoAreaTableID = this.data.getInt();
        this.boundingBox.setMin(new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat()));
        this.boundingBox.setMax(new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat()));        
        this.flags = this.data.getShort();
        this.numLod = this.data.getShort();
        
        this.textureNameList = readStringChunk(this.data.position(), HEADER_MOTX);

        checkHeader(HEADER_MOMT);
        chunkSize = data.getInt() / SIZE_MOMT;

        WMOMaterials material;
        for (int i = 0; i < nMaterials; i++) {
            material = new WMOMaterials();
            material.read(this.data);
            this.materials.add(material);
        }

        this.groupNameList = readStringChunk(this.data.position(), HEADER_MOGN);

        checkHeader(HEADER_MOGI);
        chunkSize = data.getInt() / SIZE_MOGI;

        WMOGroupInfo groupInfo;
        for (int i = 0; i < nGroups; i++) {
            groupInfo = new WMOGroupInfo();
            groupInfo.read(this.data);
            groupInfoList.add(groupInfo);
        }

        checkHeader(HEADER_MOSB);
        chunkSize = this.data.getInt();
        this.skyBoxName = readString(this.data);
        // No skyBoxName, moving further to MOPV header.
        if (this.skyBoxName.isEmpty()) {
            this.data.position(this.data.position() + chunkSize - 1);
        }

        checkHeader(HEADER_MOPV);
        chunkSize = this.data.getInt() / SIZE_MOPV;

        Vec3f portalVertex;
        for (int i = 0; i < chunkSize; i++) {
            portalVertex = new Vec3f(this.data.getFloat(), this.data.getFloat(), this.data.getFloat());
            this.portalVertexList.add(portalVertex);
        }

        checkHeader(HEADER_MOPT);
        chunkSize = this.data.getInt() / SIZE_MOPT;

        WMOPortal portal;
        for (int i = 0; i < nPortals; i++) {
            portal = new WMOPortal();
            portal.read(this.data);
            this.portalList.add(portal);
        }

        checkHeader(HEADER_MOPR);
        chunkSize = this.data.getInt() / SIZE_MOPR;

        WMOPortalRef portalRef;
        for (int i = 0; i < chunkSize; i++) {
            portalRef = new WMOPortalRef();
            portalRef.read(this.data);
            this.portalRefList.add(portalRef);
        }

        checkHeader(HEADER_MOVV);
        chunkSize = this.data.getInt() / SIZE_MOVV;

        Vec3f visibleBlockVertices;
        for (int i = 0; i < chunkSize; i++) {
            visibleBlockVertices = new Vec3f();
            visibleBlockVertices.set(this.data.getFloat(), this.data.getFloat(), this.data.getFloat());
            this.visibleBlockVerticesList.add(visibleBlockVertices);
        }

        checkHeader(HEADER_MOVB);
        chunkSize = this.data.getInt() / SIZE_MOVB;

        WMOBlock visibleBlock;
        for (int i = 0; i < chunkSize; i++) {
            visibleBlock = new WMOBlock();
            visibleBlock.read(this.data);
            this.visibleBlockList.add(visibleBlock);
        }

        checkHeader(HEADER_MOLT);
        chunkSize = this.data.getInt() / SIZE_MOLT;

        WMOLight light;
        for (int i = 0; i < chunkSize; i++) {
            light = new WMOLight();
            light.read(this.data);
            this.lightList.add(light);
        }

        checkHeader(HEADER_MODS);
        chunkSize = this.data.getInt() / SIZE_MODS;

        WMODoodadSet doodadSet;
        for (int i = 0; i < chunkSize; i++) {
            doodadSet = new WMODoodadSet();
            doodadSet.read(this.data);
            this.doodadSetList.add(doodadSet);
        }

        this.doodadNameList = readStringChunk(this.data.position(), HEADER_MODN);

        checkHeader(HEADER_MODD);
        chunkSize = this.data.getInt() / SIZE_MODD;

        WMODoodadDef doodadDef;
        for (int i = 0; i < chunkSize; i++) {
            doodadDef = new WMODoodadDef();
            doodadDef.read(this.data);
            this.doodadDefList.add(doodadDef);
        }

        checkHeader(HEADER_MFOG);
        chunkSize = this.data.getInt() / SIZE_MFOG;

        WMOFog fog;
        for (int i = 0; i < chunkSize; i++) {
            fog = new WMOFog();
            fog.read(this.data);
            this.fogList.add(fog);
        }

        // MCVP is an optional header.
        if (data.hasRemaining()) {
            checkHeader(HEADER_MCVP);
            chunkSize = this.data.getInt() / SIZE_MCVP;

            C4Plane convexVolume;
            for (int i = 0; i < chunkSize; i++) {
                convexVolume = new C4Plane();
                convexVolume.getNormal().set(this.data.getFloat(), this.data.getFloat(), this.data.getFloat());
                convexVolume.setDistance(this.data.getFloat());
                this.convexVolumePlanesList.add(convexVolume);
            }
        }

        init = true;
    }    
    
    public void initGroup(byte[] array) throws WMOException {
        WMOGroupFileReader reader = new WMOGroupFileReader();
        reader.init(array, this.flags);
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
        this.doodadNameList.clear();
        this.fogList.clear();
        this.convexVolumePlanesList.clear();
        this.wmoGroupReadersList.clear();
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
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
        return nUnknown;
    }

    public void setnUnknown(int nUnknown) {
        this.nUnknown = nUnknown;
    }

    public int getnXX() {
        return nXX;
    }

    public void setnXX(int nXX) {
        this.nXX = nXX;
    }

    public int getnXXX() {
        return nXXX;
    }

    public void setnXXX(int nXXX) {
        this.nXXX = nXXX;
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

    public List<String> getDoodadNameList() {
        return doodadNameList;
    }

    public void setDoodadNameList(List<String> doodadNameList) {
        this.doodadNameList = doodadNameList;
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

    public List<WMOGroupFileReader> getWmoGroupReadersList() {
        return wmoGroupReadersList;
    }

    public void setWmoGroupReadersList(List<WMOGroupFileReader> wmoGroupReadersList) {
        this.wmoGroupReadersList = wmoGroupReadersList;
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

        data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new WMOException("Expected header " + expectedHeader + ", received header: " + sb.toString());
        }
    }   
}
