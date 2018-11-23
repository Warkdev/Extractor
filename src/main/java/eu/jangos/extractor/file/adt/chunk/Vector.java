/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file.adt.chunk;

/**
 *
 * @author Warkdev
 */
public class Vector {
    private float x;
    private float y;
    private float z;

    public Vector() {
    }
    
    /**
     * Specifity of a vector for blizzard, it's stored as z, x, y.
     * @param z
     * @param x
     * @param y 
     */
    public Vector(float z, float x, float y) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    
    public float getNormalizedX() {
        return (this.x / 127.0f) * -1;
    }
    
    public float getNormalizedY() {
        return this.y / 127.0f;
    }
    
    public float getNormalizedZ() {
        return (this.z / 127.0f) * -1;
    }
}
