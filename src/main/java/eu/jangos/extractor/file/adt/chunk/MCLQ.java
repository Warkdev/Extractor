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

import eu.mangos.shared.flags.FlagUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class MCLQ {
    public static final int LIQUID_DATA_LENGTH = 9;
    public static final int LIQUID_FLAG_LENGTH = 8;
     
    private static final int HAS_LIQUID = 0x04;
    private static final int NO_LIQUID = 0x0F;
    private static final int DARK = 0x40;    
    
    private float minHeight;
    private float maxHeight;
    private List<Integer> light;
    private List<Float> height;
    private List<Byte> flags;
    private int nFlowvs;
    private List<SWFlowv> flowvs;

    public MCLQ() {
        this.light = new ArrayList<>();
        this.height = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.flowvs = new ArrayList<>();
    }

    public void read(ByteBuffer data) {        
        this.minHeight = data.getFloat();
        this.maxHeight = data.getFloat();
        for(int i = 0; i < LIQUID_DATA_LENGTH; i++) {
            for(int j = 0; j < LIQUID_DATA_LENGTH; j++) {
                this.light.add(data.getInt());
                this.height.add(data.getFloat());
            }
        }
        
        for(int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
            for(int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                this.flags.add(data.get());                
            }
        }
        
        this.nFlowvs = data.getInt();        
        SWFlowv flow;
        for(int i = 0; i < (nFlowvs == 0 ? 2 : nFlowvs); i++) {
            flow = new SWFlowv();
            flow.read(data);
            this.flowvs.add(flow);
        }                                
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public List<Byte> getFlags() {
        return flags;
    }

    public void setFlags(List<Byte> flags) {
        this.flags = flags;
    }
    
    

    public List<Integer> getLight() {
        return light;
    }

    public void setLight(List<Integer> light) {
        this.light = light;
    }

    public List<Float> getHeight() {
        return height;
    }

    public void setHeight(List<Float> height) {
        this.height = height;
    }             
    
    public boolean hasNoLiquid(int row, int col) {
        return hasFlag(row, col, NO_LIQUID);
    }
    
    public boolean hasLiquid(int row, int col) {
        return hasFlag(row, col, HAS_LIQUID);
    }        
    
    public boolean isDark(int row, int col) {
        return hasFlag(row, col, DARK);
    }
    
    private boolean hasFlag(int row, int col, int flag) {
        return FlagUtils.hasFlag(this.flags.get(getFlagPosition(row, col)), flag);
    }        
    
    private int getFlagPosition(int row, int col) {
        return row * LIQUID_FLAG_LENGTH + col;
    }               
    
    private void clear() {
        this.light.clear();
        this.height.clear();
    }
    
}