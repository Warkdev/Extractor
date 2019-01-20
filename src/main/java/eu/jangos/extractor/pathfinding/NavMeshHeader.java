/*
 * Copyright 2019 Warkdev.
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
package eu.jangos.extractor.pathfinding;

import org.recast4j.detour.NavMeshParams;

/**
 *
 * @author Warkdev
 */
public class NavMeshHeader {

    public static final int NAVMESHSET_MAGIC = 'M' << 24 | 'S' << 16 | 'E' << 8 | 'T'; //'MSET';
    public static final int NAVMESHSET_VERSION = 1;
    public static final int NAVMESHSET_VERSION_RECAST4J = 0x8801;

    private int magic;
    private int version;
    private int numTiles;
    private NavMeshParams params = new NavMeshParams();

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNumTiles() {
        return numTiles;
    }

    public void setNumTiles(int numTiles) {
        this.numTiles = numTiles;
    }

    public NavMeshParams getParams() {
        return params;
    }

    public void setParams(NavMeshParams params) {
        this.params = params;
    }
    
    

}
