/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file.adt.chunk;

import eu.mangos.shared.flags.FlagUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class MCNK {
    public static final int FLAG_RIVER = 0x04;
    public static final int FLAG_OCEAN = 0x08;
    public static final int FLAG_MAGMA = 0x10;
    public static final int FLAG_SLIME = 0x20;
    
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
    private float posX;
    private float posY;
    private float posZ;
    private int offsetMCCV;
    private int offsetMCLV;
    private MCVT vertices = new MCVT();
    private MCNR normals = new MCNR();     
    private MCLQ liquids = null;
    private MCLY[] textureLayers = new MCLY[4];
    private List<Integer> mcrfList = new ArrayList<>();    
    
    public MCNK() {
        for(int i = 0; i < this.textureLayers.length; i++) {
            textureLayers[i] = new MCLY();
        }
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

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }    
    
    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public void setPosZ(float posZ) {
        this.posZ = posZ;
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

    public MCLQ getLiquids() {
        return liquids;
    }

    public void setLiquids(MCLQ liquids) {
        this.liquids = liquids;
    }
    
    public boolean hasLiquid() {
        return this.liquids != null;
    }
    
    public boolean isRiver() {
        return hasLiquid() && FlagUtils.hasFlag(flags, FLAG_RIVER);
    }
    
    public boolean isOcean() {
        return hasLiquid() && FlagUtils.hasFlag(flags, FLAG_OCEAN);
    }
    
    public boolean isMagma() {
        return hasLiquid() && FlagUtils.hasFlag(flags, FLAG_MAGMA);
    }
    
    public boolean isSlime() {
        return hasLiquid() && FlagUtils.hasFlag(flags, FLAG_SLIME);
    }
}
