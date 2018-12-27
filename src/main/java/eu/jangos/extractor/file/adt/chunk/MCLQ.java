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

import eu.jangos.extractor.file.ChunkLiquidRenderer;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.ModelRendererException;
import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractorfx.rendering.PolygonMesh;
import eu.jangos.extractorfx.rendering.Render2DType;
import eu.jangos.extractorfx.rendering.Render3DType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Pane;

/**
 *
 * @author Warkdev
 */
public class MCLQ extends ChunkLiquidRenderer {

    private float minHeight;
    private float maxHeight;
    private List<Integer> light;
    private List<Float> height;    
    private int nFlowvs;
    private List<SWFlowv> flowvs;

    public MCLQ() {
        this.light = new ArrayList<>();
        this.height = new ArrayList<>();
        super.flags = new ArrayList<>();
        this.flowvs = new ArrayList<>();
    }

    public void read(ByteBuffer data) {
        clear();
        
        this.minHeight = data.getFloat();
        this.maxHeight = data.getFloat();
        for (int i = 0; i < LIQUID_DATA_LENGTH; i++) {
            for (int j = 0; j < LIQUID_DATA_LENGTH; j++) {
                this.light.add(data.getInt());
                this.height.add(data.getFloat());
            }
        }

        for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
            for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                super.flags.add((short) Byte.toUnsignedInt(data.get()));
            }
        }

        this.nFlowvs = data.getInt();
        SWFlowv flow;
        for (int i = 0; i < (nFlowvs == 0 ? 2 : nFlowvs); i++) {
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

    public boolean hasNoSurroundingLiquid(int row, int col) {
        // Check bottom right tile.
        if (row >= 0 && row < LIQUID_FLAG_LENGTH && (col - 1) >= 0 && (col - 1) < LIQUID_FLAG_LENGTH) {
            if (!hasNoLiquid(row, col - 1)) {
                return false;
            }
        }

        // Check bottom left tile.
        if (row >= 0 && row < LIQUID_FLAG_LENGTH && col >= 0 && col < LIQUID_FLAG_LENGTH) {
            if (!hasNoLiquid(row, col)) {
                return false;
            }
        }

        // Check upper right tile.
        if ((row - 1) >= 0 && (row - 1) < LIQUID_FLAG_LENGTH && (col - 1) >= 0 && (col - 1) < LIQUID_FLAG_LENGTH) {
            if (!hasNoLiquid(row - 1, col - 1)) {
                return false;
            }
        }

        // Check upper left tile.
        if ((row - 1) >= 0 && (row - 1) < LIQUID_FLAG_LENGTH && col >= 0 && col < LIQUID_FLAG_LENGTH) {
            if (!hasNoLiquid(row - 1, col)) {
                return false;
            }
        }

        return true;
    }

    private void clear() {
        this.light.clear();
        this.height.clear();
    }
    
    @Override
    public Pane render2D(Render2DType renderType) throws ModelRendererException, FileReaderException {
        switch(renderType) {
            case RENDER_TILEMAP_LIQUID_ANIMATED:
            case RENDER_TILEMAP_LIQUID_FISHABLE:
            case RENDER_TILEMAP_LIQUID_TYPE:
            case RENDER_TILEMAP_LIQUID_HEIGHTMAP:
                return renderLiquid(renderType);
            default:
                throw new UnsupportedOperationException("Render type are not supported.");
        }
    }

    @Override
    public PolygonMesh render3D(Render3DType renderType, Map<String, M2> cache) throws ModelRendererException, MPQException, FileReaderException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
