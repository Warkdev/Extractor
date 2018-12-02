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

/**
 *
 * @author Warkdev
 */
public enum MOGPFlagEnum {
    
    HAS_BSP_TREE((long) 0x1),
    HAS_LIGHT_MAP((long) 0x2),
    HAS_VERTEX_COLORS((long) 0x4),
    IS_EXTERIOR((long) 0x8),
    UNUSED_1((long) 0x10),
    UNUSED_2((long) 0x20),
    EXTERIOR_LIT((long) 0x40),
    UNREACHABLE((long) 0x80),
    UNUSED_3((long) 0x100),
    HAS_LIGHT((long) 0x200),
    UNUSED_4((long) 0x400),
    HAS_DOODADS((long) 0x800),
    HAS_WATER((long) 0x1000),
    IS_INTERIOR((long) 0x2000),
    UNUSED_5((long) 0x4000),
    UNUSED_6((long) 0x8000),
    ALWAYS_DRAW((long) 0x10000),
    HAS_TRIANGLESTRIP((long) 0x20000),
    SHOW_SKYBOX((long) 0x40000),
    IS_OCEAN((long) 0x80000),
    UNUSED_8((long) 0x100000),
    IS_MOUNT_ALLOWED((long) 0x200000),
    UNUSED_9((long) 0x400000),
    UNUSED_10((long) 0x800000),
    HAS_2_MOCV((long) 0x1000000),
    HAS_2_MOTV((long) 0x2000000),
    ANTIPORTAL((long) 0x4000000),
    UNUSED_11((long) 0x8000000),
    UNUSED_12((long) 0x10000000),
    EXTERIOR_CULL((long) 0x20000000),
    HAS_3_MOTV((long) 0x40000000),
    UNUSED_13((long) 0x80000000),    
    UNKNOWN((long) -1);
    
    private final long value;
    
    private MOGPFlagEnum(long value) {
        this.value = value;
    }   

    public long getValue() {
        return value;
    }        
    
    public static MOGPFlagEnum convert(long value)
    {
        for(MOGPFlagEnum type : values())
        {
            if(type.value == value)
            {
                return type;
            }
        }
        
        return UNKNOWN;
    }

}
