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
public class WMOWVert extends WMOLiquid {
    private byte flow1;
    private byte flow2;
    private byte flow1Pct;
    private byte filler;
    private float height;

    public void read(ByteBuffer data) {
        this.flow1 = data.get();
        this.flow2 = data.get();
        this.flow1Pct = data.get();
        this.filler = data.get();
        this.height = data.getFloat();
    }
    
    public byte getFlow1() {
        return flow1;
    }

    public void setFlow1(byte flow1) {
        this.flow1 = flow1;
    }

    public byte getFlow2() {
        return flow2;
    }

    public void setFlow2(byte flow2) {
        this.flow2 = flow2;
    }

    public byte getFlow1Pct() {
        return flow1Pct;
    }

    public void setFlow1Pct(byte flow1Pct) {
        this.flow1Pct = flow1Pct;
    }

    public byte getFiller() {
        return filler;
    }

    public void setFiller(byte filler) {
        this.filler = filler;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }        
}
