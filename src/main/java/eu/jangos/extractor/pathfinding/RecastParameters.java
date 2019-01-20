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

import eu.jangos.extractor.file.common.MapUnit;

/**
 *
 * @author Warkdev
 */
public class RecastParameters {

    // Rasterization - Cell Data.
    public static final float BASE_UNIT_DIM = 0.2f + (0.2f/3);
    public static final int VERTEX_PER_MAP = (int) (MapUnit.TILE_SIZE / BASE_UNIT_DIM + 0.5f);
    public static final int VERTEX_PER_TILE = 80;    
    public static final float CELL_SIZE = BASE_UNIT_DIM;
    public static final float CELL_HEIGHT = BASE_UNIT_DIM;    
    
    // Agent data.
    public static final float AGENT_MAX_SLOPE_ANGLE = 60;
    public static final float AGENT_HEIGHT = 6 * CELL_HEIGHT;
    public static final float AGENT_CLIMB = 4 * CELL_HEIGHT;
    public static final float AGENT_RADIUS = 2 * CELL_SIZE;            
    
    // Region data.
    public static final int REGION_MIN_AREA = 60;
    public static final int REGION_MERGE_AREA = 50;
    
    // Polygonization data.
    public static final float POLY_MAX_EDGE_LEN = (VERTEX_PER_TILE + 1) * CELL_SIZE;    
    public static final float POLY_MAX_EDGE_ERROR = 2;
    public static final int POLY_VERTS_PER_POLYGON = 6;
    
    // Detail Mesh Data.
    public static final float DETAIL_SAMPLE_DIST = 64;
    public static final float DETAIL_SAMPLE_MAX_ERROR = 2;
          
        
}
