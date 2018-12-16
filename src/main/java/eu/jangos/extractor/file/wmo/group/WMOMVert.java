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
package eu.jangos.extractor.file.wmo.group;

import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class WMOMVert extends WMOLiquid {
    private short s;    
    private short t;    
    private float height;

    public void read(ByteBuffer data) {
        this.s = data.getShort();
        this.t = data.getShort();                
        this.height = data.getFloat();
    }

    public short getS() {
        return s;
    }

    public void setS(short s) {
        this.s = s;
    }

    public short getT() {
        return t;
    }

    public void setT(short t) {
        this.t = t;
    }
        
    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }        
}
