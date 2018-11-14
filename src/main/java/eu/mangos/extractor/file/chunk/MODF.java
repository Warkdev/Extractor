/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor.file.chunk;

/**
 *
 * @author Warkdev
 */
public class MODF {
    
    private int mwidEntry;
    private int uniqueId;
    private float x;
    private float y;
    private float z;
    private float orX;
    private float orY;
    private float orZ;
    private float lowerBoundX;
    private float lowerBoundY;
    private float lowerBoundZ;
    private float upperBoundX;
    private float upperBoundY;
    private float upperBoundZ;
    private short flags;
    private short doodadSet;
    private short nameSet;
    private short padding;

    public int getMwidEntry() {
        return mwidEntry;
    }

    public void setMwidEntry(int mwidEntry) {
        this.mwidEntry = mwidEntry;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getOrX() {
        return orX;
    }

    public void setOrX(float orX) {
        this.orX = orX;
    }

    public float getOrY() {
        return orY;
    }

    public void setOrY(float orY) {
        this.orY = orY;
    }

    public float getOrZ() {
        return orZ;
    }

    public void setOrZ(float orZ) {
        this.orZ = orZ;
    }

    public float getLowerBoundX() {
        return lowerBoundX;
    }

    public void setLowerBoundX(float lowerBoundX) {
        this.lowerBoundX = lowerBoundX;
    }

    public float getLowerBoundY() {
        return lowerBoundY;
    }

    public void setLowerBoundY(float lowerBoundY) {
        this.lowerBoundY = lowerBoundY;
    }

    public float getLowerBoundZ() {
        return lowerBoundZ;
    }

    public void setLowerBoundZ(float lowerBoundZ) {
        this.lowerBoundZ = lowerBoundZ;
    }

    public float getUpperBoundX() {
        return upperBoundX;
    }

    public void setUpperBoundX(float upperBoundX) {
        this.upperBoundX = upperBoundX;
    }

    public float getUpperBoundY() {
        return upperBoundY;
    }

    public void setUpperBoundY(float upperBoundY) {
        this.upperBoundY = upperBoundY;
    }

    public float getUpperBoundZ() {
        return upperBoundZ;
    }

    public void setUpperBoundZ(float upperBoundZ) {
        this.upperBoundZ = upperBoundZ;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public short getDoodadSet() {
        return doodadSet;
    }

    public void setDoodadSet(short doodadSet) {
        this.doodadSet = doodadSet;
    }

    public short getNameSet() {
        return nameSet;
    }

    public void setNameSet(short nameSet) {
        this.nameSet = nameSet;
    }

    public short getPadding() {
        return padding;
    }

    public void setPadding(short padding) {
        this.padding = padding;
    }
    
    
    
}
