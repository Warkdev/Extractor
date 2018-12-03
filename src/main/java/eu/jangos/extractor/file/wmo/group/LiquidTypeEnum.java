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

import eu.mangos.shared.flags.FlagUtils;

/**
 *
 * @author Warkdev
 */
public enum LiquidTypeEnum {
       
    LIQUID_BASIC_TYPE_WATER(0),
    LIQUID_BASIC_TYPE_OCEAN(1),
    LIQUID_BASIC_TYPE_MAGMA(2),
    LIQUID_BASIC_TYPE_SLIME(3),
    LIQUID_WMO_WATER(13),
    LIQUID_WMO_OCEAN(14),
    LIQUID_GREEN_LAVA(15),
    LIQUID_WMO_MAGMA(19),
    LIQUID_WMO_SLIME(20),
    LIQUID_NAXX_SLIME(21),
    LIQUID_UNKNOWN(-1);
    
    public static final int LIQUID_BASIC_MASK = 3;
    public static final int LIQUID_END_BASIC_LIQUIDS = 20;
    public static final int LIQUID_FIRST_NONBASIC_LIQUID_TYPE = 21;
    
    private int code;

    private LiquidTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }        
    
    public static LiquidTypeEnum convert(int value) {
        for(LiquidTypeEnum type : values())
        {
            if(type.code == value)
            {
                return type;
            }
        }
        
        return LIQUID_UNKNOWN;
    }
            
    public static LiquidTypeEnum getLiquidToWMO(int groupLiquid, int mogpFlags) {
        LiquidTypeEnum basic = convert(groupLiquid & LIQUID_BASIC_MASK);
        switch(basic) {
            case LIQUID_BASIC_TYPE_WATER:
                return FlagUtils.hasFlag(mogpFlags, MOGPFlagEnum.IS_OCEAN.getValue()) ? LIQUID_WMO_OCEAN : LIQUID_WMO_WATER;
            case LIQUID_BASIC_TYPE_OCEAN:
                return LIQUID_WMO_OCEAN;
            case LIQUID_BASIC_TYPE_MAGMA:
                return LIQUID_WMO_MAGMA;
            case LIQUID_BASIC_TYPE_SLIME:
                return LIQUID_WMO_SLIME;
            default:
                return LIQUID_UNKNOWN;
        }
    }
    
}
