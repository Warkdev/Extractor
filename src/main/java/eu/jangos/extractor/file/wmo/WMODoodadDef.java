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
package eu.jangos.extractor.file.wmo;

import com.sun.javafx.geom.Vec3f;
import com.sun.javafx.geom.Vec4f;
import eu.jangos.extractor.file.common.CImVector;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class WMODoodadDef {
    private int nameIndex;
    private int flag;
    private Vec3f positon = new Vec3f();
    private Vec4f orientation = new Vec4f();
    private float scale;
    private CImVector color = new CImVector();

    public WMODoodadDef() {
    }

    public void read(ByteBuffer data) {                
        this.nameIndex = data.get() << 16 + data.get() << 8 + data.get();
        this.flag = data.get();
        this.positon.set(data.getFloat(), data.getFloat(), data.getFloat());
        this.orientation.x = data.getFloat();
        this.orientation.y = data.getFloat();
        this.orientation.z = data.getFloat();
        this.orientation.w = data.getFloat();                     
        this.scale = data.getFloat();
        this.color.setB(data.get());
        this.color.setG(data.get());
        this.color.setR(data.get());
        this.color.setA(data.get());
    }
    
    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public Vec3f getPositon() {
        return positon;
    }

    public void setPositon(Vec3f positon) {
        this.positon = positon;
    }

    public Vec4f getOrientation() {
        return orientation;
    }

    public void setOrientation(Vec4f orientation) {
        this.orientation = orientation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public CImVector getColor() {
        return color;
    }

    public void setColor(CImVector color) {
        this.color = color;
    }
    
}
