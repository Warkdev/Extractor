/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file.adt.chunk;

import com.sun.javafx.geom.Vec3f;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class MDDF {
    
    private int mmidEntry;
    private int uniqueId;
    private Vec3f position = new Vec3f();
    private Vec3f orientation = new Vec3f();
    private short scale;
    private short flags;

    public void read(ByteBuffer in) {        
        this.mmidEntry = in.getInt();
        this.uniqueId = in.getInt();
        this.position.x = in.getFloat();
        this.position.y = in.getFloat();
        this.position.z = in.getFloat();
        this.orientation.x = in.getFloat();
        this.orientation.y = in.getFloat();
        this.orientation.z = in.getFloat();
        this.scale = in.getShort();
        this.flags = in.getShort();                    
    }
    
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

    public Vec3f getPosition() {
        return position;
    }

    public void setPosition(Vec3f position) {
        this.position = position;
    }

    public Vec3f getOrientation() {
        return orientation;
    }

    public void setOrientation(Vec3f orientation) {
        this.orientation = orientation;
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
