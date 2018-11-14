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
public class MDDF {
    
    private int mmidEntry;
    private int uniqueId;
    private float x;
    private float y;
    private float z;
    private float orX;
    private float orY;
    private float orZ;
    private short scale;
    private short flags;

    public int getMmidEntry() {
        return mmidEntry;
    }

    public void setMmidEntry(int mmidEntry) {
        this.mmidEntry = mmidEntry;
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

    public short getScale() {
        return scale;
    }

    public void setScale(short scale) {
        this.scale = scale;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }
    
    
    
}
