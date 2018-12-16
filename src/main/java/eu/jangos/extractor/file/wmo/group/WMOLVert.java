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
public class WMOLVert {
    private WMOLiquid liquid;    

    public void read(ByteBuffer data, LiquidTypeEnum liquidType) {
        switch(liquidType) {
            case LIQUID_WMO_OCEAN:
            case LIQUID_WMO_WATER:
                this.liquid = new WMOWVert();
                break;
            case LIQUID_WMO_MAGMA:
            case LIQUID_WMO_SLIME:            
                this.liquid = new WMOMVert();
                break;
        }
        liquid.read(data);
    }

    public WMOLiquid getLiquid() {
        return liquid;
    }

    public void setLiquid(WMOLiquid liquid) {
        this.liquid = liquid;
    }                       
}
