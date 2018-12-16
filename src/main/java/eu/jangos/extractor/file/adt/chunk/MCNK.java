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
package eu.jangos.extractor.file.adt.chunk;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.FileReader;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.mangos.shared.flags.FlagUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class MCNK extends FileReader {

    // Headers in MCNK Sub-chunk.
    private static final String HEADER_MCVT = "MCVT";
    private static final String HEADER_MCNR = "MCNR";
    private static final String HEADER_MCLY = "MCLY";
    private static final String HEADER_MCRF = "MCRF";
    private static final String HEADER_MCSH = "MCSH";
    private static final String HEADER_MCAL = "MCAL";
    private static final String HEADER_MCLQ = "MCLQ";
    private static final String HEADER_MCSE = "MCSE";

    // Flags in MCNK Sub-Chunk.
    public static final int FLAG_HAS_MCSH = 0x01;
    public static final int FLAG_IMPASS = 0x02;
    public static final int FLAG_RIVER = 0x04;
    public static final int FLAG_OCEAN = 0x08;
    public static final int FLAG_MAGMA = 0x10;
    public static final int FLAG_SLIME = 0x20;
    public static final int FLAG_HAS_MCCV = 0x40;

    private int flags;
    private int indexX;
    private int indexY;
    private int nbLayers;
    private int nDoodadRefs;
    private int offsetMCVT;
    private int offsetMCNR;
    private int offsetMCLY;
    private int offsetMCRF;
    private int offsetMCAL;
    private int sizeAlpha;
    private int offsetMCSH;
    private int sizeShadow;
    private int areadId;
    private int nMapObjRefs;
    private int holes;
    private byte[][] lowQualityTextMap = new byte[8][8];
    private int predTex;
    private int noEffectDoodad;
    private int offsetMCSE;
    private int nSndEmitters;
    private int offsetMCLQ;
    private int sizeLiquid;
    private Vec3f position = new Vec3f();    
    private int offsetMCCV;
    private int offsetMCLV;
    private MCVT vertices = new MCVT();
    private MCNR normals = new MCNR();
    private List<MCLQ> listLiquids = new ArrayList<>();
    private MCLY[] textureLayers = new MCLY[4];
    private List<Integer> mcrfList = new ArrayList<>();

    public MCNK() {
        for (int i = 0; i < this.textureLayers.length; i++) {
            textureLayers[i] = new MCLY();
        }
    }

    @Override
    public void init(byte[] data, String filename) throws IOException, FileReaderException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void read(ByteBuffer in) throws FileReaderException {
        super.data = in;
        
        int size;
        int start;                

        this.flags = super.data.getInt();
        this.indexX = super.data.getInt();
        this.indexY = super.data.getInt();
        this.nbLayers = super.data.getInt();
        this.nDoodadRefs = super.data.getInt();
        this.offsetMCVT = super.data.getInt();
        this.offsetMCNR = super.data.getInt();
        this.offsetMCLY = super.data.getInt();
        this.offsetMCRF = super.data.getInt();
        this.offsetMCAL = super.data.getInt();
        this.sizeAlpha = super.data.getInt();
        this.offsetMCSH = super.data.getInt();
        this.sizeShadow = super.data.getInt();
        this.areadId = super.data.getInt();
        this.nMapObjRefs = super.data.getInt();
        this.holes = super.data.getInt();

        for (int j = 0; j < 16; j++) {
            // Skipping low quality text map for now. (64 bytes)
            super.data.get();
        }

        this.predTex = super.data.getInt();
        this.noEffectDoodad = super.data.getInt();
        this.offsetMCSE = super.data.getInt();
        this.nSndEmitters = super.data.getInt();
        this.offsetMCLQ = super.data.getInt();
        this.sizeLiquid = super.data.getInt();
        this.position.x = super.data.getFloat();
        this.position.y = super.data.getFloat();
        this.position.z = super.data.getFloat();
        this.offsetMCCV = super.data.getInt();
        this.offsetMCLV = super.data.getInt();                
        
        // Unused
        super.data.getInt();
                
        // Must now parse MCVT            
        checkHeader(HEADER_MCVT);
        // We ignore size.
        super.data.getInt();
        this.vertices.read(super.data);

        // Must now parse MCNR
        checkHeader(HEADER_MCNR);
        // We ignore size.
        super.data.getInt();
        this.normals.read(super.data);

        // 13 unknown bytes at the end of normals:
        for (int j = 0; j < 13; j++) {
            super.data.get();
        }

        // Must now parse MCLY.
        checkHeader(HEADER_MCLY);

        // We ignore size.
        super.data.getInt();
        for (int j = 0; j < this.nbLayers; j++) {
            this.textureLayers[j].read(super.data);            
        }
        
        // Must now parse MCRF.
        checkHeader(HEADER_MCRF);

        size = super.data.getInt();
        start = super.data.position();        
        while (super.data.position() - start < size) {
            this.mcrfList.add(super.data.getInt());
        }

        // Must now parse MCSH.
        checkHeader(HEADER_MCSH);

        size = super.data.getInt();
        for (int j = 0; j < size; j++) {
            super.data.get();
        }

        // Must now parse MCAL.
        checkHeader(HEADER_MCAL);

        size = super.data.getInt();
        for (int j = 0; j < size; j++) {
            super.data.get();
        }

        // Must now parse MCLQ.
        checkHeader(HEADER_MCLQ);

        size = this.sizeLiquid - 8;
        // Then we skip the "size field" as it's always 0.
        super.data.getInt();
        // Documentation is spread over several codebase, none really figuring out what it is properly.
        // Thanks for Mangos/CMangos codebase on which this is based.            
        if (hasLiquid()) {            
            MCLQ liquid;
            // MCLQ can be made of several layers. It's assumed (guessed) that MCLQ are ordered by liquid type in the ADT.
            if (isRiver()) {
                liquid = new MCLQ();
                liquid.read(super.data);
                this.listLiquids.add(liquid);
            }
            if (isOcean()) {
                liquid = new MCLQ();
                liquid.read(super.data);
                this.listLiquids.add(liquid);
            }
            if (isMagma()) {
                liquid = new MCLQ();
                liquid.read(super.data);
                this.listLiquids.add(liquid);
            }
            if (isSlime()) {
                liquid = new MCLQ();
                liquid.read(super.data);
                this.listLiquids.add(liquid);
            }            
        }

        // Must now parse MCSE.
        checkHeader(HEADER_MCSE);

        // Flag value not well documented.
        super.data.getInt();
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getIndexX() {
        return indexX;
    }

    public void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

    public int getNbLayers() {
        return nbLayers;
    }

    public void setNbLayers(int nbLayers) {
        this.nbLayers = nbLayers;
    }

    public int getnDoodadRefs() {
        return nDoodadRefs;
    }

    public void setnDoodadRefs(int nDoodadRefs) {
        this.nDoodadRefs = nDoodadRefs;
    }

    public int getOffsetMCVT() {
        return offsetMCVT;
    }

    public void setOffsetMCVT(int offsetMCVT) {
        this.offsetMCVT = offsetMCVT;
    }

    public int getOffsetMCNR() {
        return offsetMCNR;
    }

    public void setOffsetMCNR(int offsetMCNR) {
        this.offsetMCNR = offsetMCNR;
    }

    public int getOffsetMCLY() {
        return offsetMCLY;
    }

    public void setOffsetMCLY(int offsetMCLY) {
        this.offsetMCLY = offsetMCLY;
    }

    public int getOffsetMCRF() {
        return offsetMCRF;
    }

    public void setOffsetMCRF(int offsetMCRF) {
        this.offsetMCRF = offsetMCRF;
    }

    public int getOffsetMCAL() {
        return offsetMCAL;
    }

    public void setOffsetMCAL(int offsetMCAL) {
        this.offsetMCAL = offsetMCAL;
    }

    public int getSizeAlpha() {
        return sizeAlpha;
    }

    public void setSizeAlpha(int sizeAlpha) {
        this.sizeAlpha = sizeAlpha;
    }

    public int getOffsetMCSH() {
        return offsetMCSH;
    }

    public void setOffsetMCSH(int offsetMCSH) {
        this.offsetMCSH = offsetMCSH;
    }

    public int getSizeShadow() {
        return sizeShadow;
    }

    public void setSizeShadow(int sizeShadow) {
        this.sizeShadow = sizeShadow;
    }

    public int getAreadId() {
        return areadId;
    }

    public void setAreadId(int areadId) {
        this.areadId = areadId;
    }

    public int getnMapObjRefs() {
        return nMapObjRefs;
    }

    public void setnMapObjRefs(int nMapObjRefs) {
        this.nMapObjRefs = nMapObjRefs;
    }

    public int getHoles() {
        return holes;
    }

    public void setHoles(int holes) {
        this.holes = holes;
    }

    public byte[][] getLowQualityTextMap() {
        return lowQualityTextMap;
    }

    public void setLowQualityTextMap(byte[][] lowQualityTextMap) {
        this.lowQualityTextMap = lowQualityTextMap;
    }

    public int getPredTex() {
        return predTex;
    }

    public void setPredTex(int predTex) {
        this.predTex = predTex;
    }

    public int getNoEffectDoodad() {
        return noEffectDoodad;
    }

    public void setNoEffectDoodad(int noEffectDoodad) {
        this.noEffectDoodad = noEffectDoodad;
    }

    public int getOffsetMCSE() {
        return offsetMCSE;
    }

    public void setOffsetMCSE(int offsetMCSE) {
        this.offsetMCSE = offsetMCSE;
    }

    public int getnSndEmitters() {
        return nSndEmitters;
    }

    public void setnSndEmitters(int nSndEmitters) {
        this.nSndEmitters = nSndEmitters;
    }

    public int getOffsetMCLQ() {
        return offsetMCLQ;
    }

    public void setOffsetMCLQ(int offsetMCLQ) {
        this.offsetMCLQ = offsetMCLQ;
    }

    public int getSizeLiquid() {
        return sizeLiquid;
    }

    public void setSizeLiquid(int sizeLiquid) {
        this.sizeLiquid = sizeLiquid;
    }

    public Vec3f getPosition() {
        return position;
    }

    public void setPosition(Vec3f position) {
        this.position = position;
    }

    public int getOffsetMCCV() {
        return offsetMCCV;
    }

    public void setOffsetMCCV(int offsetMCCV) {
        this.offsetMCCV = offsetMCCV;
    }

    public int getOffsetMCLV() {
        return offsetMCLV;
    }

    public void setOffsetMCLV(int offsetMCLV) {
        this.offsetMCLV = offsetMCLV;
    }

    public MCVT getVertices() {
        return vertices;
    }

    public void setVertices(MCVT vertices) {
        this.vertices = vertices;
    }

    public MCNR getNormals() {
        return normals;
    }

    public void setNormals(MCNR normals) {
        this.normals = normals;
    }

    public MCLY[] getTextureLayers() {
        return textureLayers;
    }

    public void setTextureLayers(MCLY[] textureLayers) {
        this.textureLayers = textureLayers;
    }

    public List<Integer> getMcrfList() {
        return mcrfList;
    }

    public void setMcrfList(List<Integer> mcrfList) {
        this.mcrfList = mcrfList;
    }

    public List<MCLQ> getListLiquids() {
        return listLiquids;
    }

    public void setListLiquids(List<MCLQ> listLiquids) {
        this.listLiquids = listLiquids;
    }

    public boolean hasLiquid() {
        return isRiver() || isOcean() || isMagma() || isSlime();
    }

    public boolean isRiver() {
        return FlagUtils.hasFlag(flags, FLAG_RIVER);
    }

    public boolean isOcean() {
        return FlagUtils.hasFlag(flags, FLAG_OCEAN);
    }

    public boolean isMagma() {
        return FlagUtils.hasFlag(flags, FLAG_MAGMA);
    }

    public boolean isSlime() {
        return FlagUtils.hasFlag(flags, FLAG_SLIME);
    }
}
