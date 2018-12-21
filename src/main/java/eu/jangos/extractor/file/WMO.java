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
import eu.jangos.extractor.file.common.MapUnit;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractor.file.wmo.WMOBlock;
import eu.jangos.extractor.file.wmo.WMODoodadDef;
import eu.jangos.extractor.file.wmo.WMODoodadSet;
import eu.jangos.extractor.file.wmo.WMOFog;
import eu.jangos.extractor.file.wmo.WMOGroupInfo;
import eu.jangos.extractor.file.wmo.WMOLight;
import eu.jangos.extractor.file.wmo.WMOMaterials;
import eu.jangos.extractor.file.wmo.WMOPortal;
import eu.jangos.extractor.file.wmo.WMOPortalRef;
import eu.jangos.extractor.file.wmo.group.MLIQ;
import eu.jangos.extractorfx.rendering.LiquidTileMapRenderType;
import eu.jangos.extractorfx.rendering.PolygonMesh;
import eu.mangos.shared.flags.FlagUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.text.Text;
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
public class WMO extends FileReader {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

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

    public static final int FLAG_DO_NOT_ATTENUATE_VERTICES = 0x1;
    public static final int FLAG_USE_UNIFIED_RENDER_PATH = 0x2;
    public static final int FLAG_USE_LIQUID_FROM_DBC = 0x4;
    public static final int FLAG_DO_NOT_FIX_VERTEX_COLOR_ALPHA = 0x8;

    private static final int SUPPORTED_VERSION = 17;

    private NumberFormat formatter = new DecimalFormat("000");

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

    private List<WMOGroup> wmoGroupList = new ArrayList<>();

    private TriangleMesh objectMesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
    private PolygonMesh liquidMesh = new PolygonMesh();
    
    public boolean isRootFile(byte[] array) throws FileReaderException {
        super.data = ByteBuffer.wrap(array);
        super.data.order(ByteOrder.LITTLE_ENDIAN);
        logger.debug("Checking is filename " + filename + " is a root WMO.");

        if (array.length == 0) {
            logger.error("Data array for file " + filename + " is empty");
            throw new WMOException("Data array is empty.");
        }

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

    public void init(MPQManager manager, String filename) throws JMpqException, FileReaderException, MPQException {
        init(manager, filename, false);
    }

    @Override
    public void init(MPQManager manager, String filename, boolean loadChildren) throws FileReaderException, MPQException, JMpqException {
        super.data = ByteBuffer.wrap(manager.getMPQForFile(filename).extractFileAsBytes(filename));

        if (data.remaining() == 0) {
            logger.error("Data array for file " + filename + " is empty");
            throw new WMOException("Data array is empty.");
        }

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

        // Init group information.
        WMOGroup wmoGroup;
        for (int i = 0; i < this.nGroups; i++) {
            String wmoGroupPath = FilenameUtils.removeExtension(this.filename) + "_" + formatter.format(i) + ".wmo";
            wmoGroup = new WMOGroup();
            wmoGroup.init(manager, wmoGroupPath);
            this.wmoGroupList.add(wmoGroup);
        }

        init = true;
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
        this.wmoGroupList.clear();
        this.liquidMesh.getPoints().clear();
        this.liquidMesh.getTexCoords().clear();
        this.liquidMesh.getFaceSmoothingGroups().clear();        
        clearMesh(this.objectMesh);
    }

    private void clearMesh(TriangleMesh mesh) {        
        mesh.getPoints().clear();        
        mesh.getTexCoords().clear();
        mesh.getFaces().clear();        
        mesh.getNormals().clear();
    }
    
    public boolean doNotAttenuateVertices() {
        return hasFlag(FLAG_DO_NOT_ATTENUATE_VERTICES);
    }

    public boolean useUnifiedRenderPath() {
        return hasFlag(FLAG_USE_UNIFIED_RENDER_PATH);
    }

    public boolean useDBCLiquidID() {
        return hasFlag(FLAG_USE_LIQUID_FROM_DBC);
    }

    public boolean doNotFixVertexColorAlpha() {
        return hasFlag(FLAG_DO_NOT_FIX_VERTEX_COLOR_ALPHA);
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
        return wmoGroupList;
    }

    public void setWmoGroupReadersList(List<WMOGroup> wmoGroupReadersList) {
        this.wmoGroupList = wmoGroupReadersList;
    }

    public PolygonMesh renderLiquid() {
        this.liquidMesh.getPoints().clear();
        this.liquidMesh.getTexCoords().clear();
        
        int offsetVertices = 0;
        
        for (int i = 0; i < this.nGroups; i++) {            
            WMOGroup wmoGroup = this.wmoGroupList.get(i);            
            
            if (wmoGroup.hasLiquid()) {
                // Wmo group file has liquid information.
                
                int[][] faces = new int[wmoGroup.getLiquidIndicesList().size()/4][8];                
                for (int face = 0, idx = 0; face < wmoGroup.getLiquidIndicesList().size(); face += 4, idx++) {                    
                    faces[idx][7] = wmoGroup.getLiquidIndicesList().get(face) + offsetVertices;                    
                    faces[idx][6] = wmoGroup.getLiquidIndicesList().get(face) + offsetVertices;                    
                    faces[idx][5] = wmoGroup.getLiquidIndicesList().get(face + 1) + offsetVertices;
                    faces[idx][4] = wmoGroup.getLiquidIndicesList().get(face + 1) + offsetVertices;                    
                    faces[idx][3] = wmoGroup.getLiquidIndicesList().get(face + 2) + offsetVertices;
                    faces[idx][2] = wmoGroup.getLiquidIndicesList().get(face + 2) + offsetVertices;                    
                    faces[idx][1] = wmoGroup.getLiquidIndicesList().get(face + 3) + offsetVertices;                    
                    faces[idx][0] = wmoGroup.getLiquidIndicesList().get(face + 3) + offsetVertices;                    
                    liquidMesh.getFaceSmoothingGroups().addAll(0);                    
                }                        
                liquidMesh.faces = ArrayUtils.addAll(liquidMesh.faces, faces);                                                
                
                int idx = 0;
                for (Vec3f v : wmoGroup.getLiquidVerticesList()) {
                    liquidMesh.getPoints().addAll(v.x, v.y, v.z);                    
                    liquidMesh.getTexCoords().addAll(wmoGroup.getTextureVertexList().get(idx).x, wmoGroup.getTextureVertexList().get(idx).y);
                    idx++;
                    offsetVertices++;
                }

            }
        }
        
        return this.liquidMesh;
    }
    
    public void saveWavefront(String file, boolean addTextures) throws IOException {
        // Check if liquid are already rendered.                
        if (this.liquidMesh.getPoints().size() == 0) {
            renderLiquid();
        }

        File objFile = new File(file);
        if (objFile.exists()) {
            objFile.delete();
        } else {
            objFile.getParentFile().mkdirs();
        }

        OutputStreamWriter writer = new FileWriter(objFile);
        writer.write(renderLiquidInWavefront(addTextures));
        writer.close();
    }
    
    /**
     * This methid returns the OBJ file as a String representation (including
     * carriage return).
     *
     * @return A String object representing the corresponding OBJ file
     * structure.
     */
    public String renderLiquidInWavefront(boolean addTextures) {        
        StringBuilder sb = new StringBuilder();        

        for (int i = 0; i < this.liquidMesh.getPoints().size(); i += 3) {
            sb.append("v " + this.liquidMesh.getPoints().get(i) + " " + this.liquidMesh.getPoints().get(i + 1) + " " + this.liquidMesh.getPoints().get(i + 2) + "\n");
        }

        if(addTextures) {
            for (int i = 0; i < this.liquidMesh.getTexCoords().size(); i += 2) {
                sb.append("vt " + this.liquidMesh.getTexCoords().get(i) + " " + this.liquidMesh.getTexCoords().get(i + 1) + "\n");
            }
        }

        for (int i = 0; i < this.liquidMesh.faces.length; i++) {
            sb.append("f ");
            for (int j = 0; j < this.liquidMesh.faces[i].length; j+=2) {
                sb.append(this.liquidMesh.faces[i][j]+1);
                if(addTextures) {
                    sb.append("/"+this.liquidMesh.faces[i][j+1]);
                }
                sb.append(" ");                
            }
            sb.append("\n");
        }        

        return sb.toString();
    }
    
    /**
     * This method return the liquid tile map that can be added to any JavaFX
     * group later on.
     *
     * @param viewportWidth The viewport width size into which this liquid tile
     * map will need to be rendered. Used to translate the position of the Tile
     * Map to the visible area.
     * @param viewportHeight The viewport width size into which this liquid tile
     * map will need to be rendered. Used to translate the position of the Tile
     * Map to the visible area.
     * @param renderType Indicates which type of rendering is requested.
     * @param renderWMOBoundaries Indicates whether total WMO boundaries must be
     * rendered or not.
     * @param renderGroup Indicates whether group rectangle must be displayed or
     * not.
     * @param renderNonLiquidGroup Indicates whether non-liquid wmo group must
     * be drawn or not.
     * @param displayGroupNumber Indicates whether group number must be shown or
     * not. Side-note, only the group number of rendered groups will be
     * displayed. If renderGroup and renderNonLiquidGroup are both set to false,
     * this parameter has no effect.
     * @return A Pane containing the liquid tile map.
     */
    public Pane getLiquidTileMap(int viewportWidth, int viewportHeight, LiquidTileMapRenderType renderType, boolean renderWMOBoundaries, boolean renderGroup, boolean renderNonLiquidGroup, boolean displayGroupNumber) {
        Pane pane = new AnchorPane();        
        
        Group liquid = new Group();
        Rectangle rect = new Rectangle();
        double heightRoot = this.boundingBox.getMax().y - this.boundingBox.getMin().y;
        double widthRoot = this.boundingBox.getMax().x - this.boundingBox.getMin().x;
        rect.setX(this.boundingBox.getMin().x);
        rect.setY(-this.boundingBox.getMin().y - heightRoot);
        rect.setWidth(widthRoot);
        rect.setHeight(heightRoot);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.RED);
        
        if (renderWMOBoundaries) {
            liquid.getChildren().add(rect);
        }

        for (WMOGroup group : this.wmoGroupList) {
            StackPane stackPane = new StackPane();
            Rectangle r = new Rectangle();
            double height = group.getGroup().getBoundingBox().getMax().y - group.getGroup().getBoundingBox().getMin().y;
            double width = group.getGroup().getBoundingBox().getMax().x - group.getGroup().getBoundingBox().getMin().x;

            r.setX(group.getGroup().getBoundingBox().getMin().x);
            r.setY(-group.getGroup().getBoundingBox().getMin().y - height);
            r.setWidth(width);
            r.setHeight(height);
            r.setFill(Color.TRANSPARENT);

            if (group.hasLiquid()) {                
                for (int x = 0; x < group.getLiquid().getxTiles(); x++) {
                    for (int y = 0; y < group.getLiquid().getyTiles(); y++) {
                        Rectangle tile = new Rectangle(x * MapUnit.UNIT_SIZE + group.getLiquid().getBaseCoordinates().x, -(y * MapUnit.UNIT_SIZE + group.getLiquid().getBaseCoordinates().y), MapUnit.UNIT_SIZE, MapUnit.UNIT_SIZE);
                        tile.setFill(Color.TRANSPARENT);

                        boolean render = true;
                        if (group.getLiquid().hasNoLiquid(x, y)) {
                            render = false;
                        } else {
                            tile.setFill(getColorForLiquid(renderType, group, x, y));
                        }
                        if (render) {
                            liquid.getChildren().add(tile);
                        }
                    }
                }
            }

            String[] temp = FilenameUtils.getName(group.getFilename()).split("\\.")[0].split("_");
            Text label = new Text(temp[temp.length - 1]);

            if (group.hasLiquid()) {
                r.setStroke(Color.BLUE);
                label.setStroke(Color.RED);
            } else {
                r.setStroke(Color.BLACK);
            }
            stackPane.setLayoutX(r.getX());
            stackPane.setLayoutY(r.getY());
            if ((renderGroup && group.hasLiquid()) || (renderNonLiquidGroup && !group.hasLiquid())) {
                stackPane.getChildren().add(r);
                if (displayGroupNumber) {
                    stackPane.getChildren().add(label);
                    StackPane.setAlignment(label, Pos.TOP_LEFT);
                }
            }
            pane.getChildren().add(stackPane);
            stackPane.getTransforms().addAll(new Translate(viewportWidth / 2, viewportHeight / 2));
        }
        liquid.getTransforms().addAll(new Translate(viewportWidth / 2, viewportHeight / 2));
        pane.getChildren().add(liquid);

        return pane;
    }

    private Color getColorForLiquid(LiquidTileMapRenderType renderType, WMOGroup group, int x, int y) {
        switch (renderType) {
            case RENDER_LIQUID_TYPE:
                if (group.getLiquid().isOverlap(x, y)) {
                    return Color.YELLOW;
                } else if (group.getLiquid().isWater(x, y)) {
                    return Color.BLUE;
                } else if (group.getLiquid().isOcean(x, y)) {
                    return Color.PURPLE;
                } else if (group.getLiquid().isMagma(x, y)) {
                    return Color.ORANGE;
                } else if (group.getLiquid().isSlime(x, y)) {
                    return Color.GREEN;
                }
                break;
            case RENDER_LIQUID_FISHABLE:
                if (group.getLiquid().isFishable(x, y)) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            case RENDER_LIQUID_ANIMATED:
                if (group.getLiquid().isAnimated(x, y)) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
        }
        return Color.BLACK;
    }
}
