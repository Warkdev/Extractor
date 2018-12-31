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
import eu.jangos.extractor.file.common.CAaBox;
import java.nio.ByteBuffer;
import javafx.geometry.Point3D;

/**
 *
 * @author Warkdev
 */
public class MODF {
    
    private int mwidEntry;
    private int uniqueId;
    private Point3D position;
    private Point3D orientation;
    private CAaBox boundingBox = new CAaBox();        
    private short flags;
    private short doodadSet;
    private short nameSet;
    private short padding;

    public void read(ByteBuffer in) {
        this.mwidEntry = in.getInt();
        this.uniqueId = in.getInt();
        this.position = new Point3D(in.getFloat(), in.getFloat(), in.getFloat());        
        this.orientation = new Point3D(in.getFloat(), in.getFloat(), in.getFloat());                
        this.boundingBox.read(in);
        this.flags = in.getShort();
        this.doodadSet = in.getShort();
        this.nameSet = in.getShort();
        this.padding = in.getShort();
    }
    
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

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    public Point3D getOrientation() {
        return orientation;
    }

    public void setOrientation(Point3D orientation) {
        this.orientation = orientation;
    }

    public CAaBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(CAaBox boundingBox) {
        this.boundingBox = boundingBox;
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
