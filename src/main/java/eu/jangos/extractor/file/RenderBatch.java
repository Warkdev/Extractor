/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

/**
 *
 * @author Warkdev
 */
public class RenderBatch {
    private int firstFace;
    private int materialID;
    private int numFaces;
    private int groupID;
    private int blendType;

    public int getFirstFace() {
        return firstFace;
    }

    public void setFirstFace(int firstFace) {
        this.firstFace = firstFace;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public int getNumFaces() {
        return numFaces;
    }

    public void setNumFaces(int numFaces) {
        this.numFaces = numFaces;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getBlendType() {
        return blendType;
    }

    public void setBlendType(int blendType) {
        this.blendType = blendType;
    }
    
    
}
