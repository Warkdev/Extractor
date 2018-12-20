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

    private static final int MASK_LIQUID = 0x03;
    private static final int FLAG_IS_WATER = 0x00;
    private static final int FLAG_IS_OCEAN = 0x01;
    private static final int FLAG_IS_MAGMA = 0x02;
    private static final int FLAG_IS_SLIME = 0x03;
    private static final int FLAG_IS_ANIMATED = 0x04;
    private static final int FLAG_E = 0x08;
    private static final int MASK_NO_LIQUID = 0x0F;
    private static final int FLAG_C = 0x10;
    private static final int FLAG_D = 0x20;
    private static final int FLAG_FISHABLE = 0x40;
    private static final int FLAG_DARK = 0x80;

    private float minHeight;
    private float maxHeight;
    private List<Integer> light;
    private List<Float> height;
    private List<Short> flags;
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
        for (int i = 0; i < LIQUID_DATA_LENGTH; i++) {
            for (int j = 0; j < LIQUID_DATA_LENGTH; j++) {
                this.light.add(data.getInt());
                this.height.add(data.getFloat());
            }
        }

        for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
            for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                this.flags.add((short) Byte.toUnsignedInt(data.get()));
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

    public List<Short> getFlags() {
        return flags;
    }

    public void setFlags(List<Short> flags) {
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

    public float getHeightAt(int row, int col) {
        return this.height.get(getDataPosition(row, col));
    }

    public float getLightAt(int row, int col) {
        return this.light.get(getDataPosition(row, col));
    }

    public boolean hasNoLiquid(int row, int col) {
        return hasFlag(row, col, MASK_NO_LIQUID);
    }

    public boolean isRiver(int row, int col) {
        return !hasNoLiquid(row, col) && (this.flags.get(getFlagPosition(row, col)) & MASK_LIQUID) == FLAG_IS_WATER;
    }

    public boolean isOcean(int row, int col) {
        return !hasNoLiquid(row, col) && (this.flags.get(getFlagPosition(row, col)) & MASK_LIQUID) == FLAG_IS_OCEAN;
    }

    public boolean isMagma(int row, int col) {
        return !hasNoLiquid(row, col) && (this.flags.get(getFlagPosition(row, col)) & MASK_LIQUID) == FLAG_IS_MAGMA;
    }

    public boolean isSlime(int row, int col) {
        return !hasNoLiquid(row, col) && (this.flags.get(getFlagPosition(row, col)) & MASK_LIQUID) == FLAG_IS_SLIME;
    }

    public boolean isFishable(int row, int col) {
        return hasFlag(row, col, FLAG_FISHABLE);
    }

    public boolean isFlagC(int row, int col) {
        return hasFlag(row, col, FLAG_C);
    }

    public boolean isFlagD(int row, int col) {
        return hasFlag(row, col, FLAG_D);
    }

    public boolean isFlagE(int row, int col) {
        return hasFlag(row, col, FLAG_E);
    }

    public boolean isAnimated(int row, int col) {
        return hasFlag(row, col, FLAG_IS_ANIMATED);
    }

    public boolean isDark(int row, int col) {
        return hasFlag(row, col, FLAG_DARK);
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

    private boolean hasFlag(int row, int col, int flag) {
        return FlagUtils.hasFlag(this.flags.get(getFlagPosition(row, col)), flag);
    }

    private int getFlagPosition(int row, int col) {
        return row * LIQUID_FLAG_LENGTH + col;
    }

    private int getDataPosition(int row, int col) {
        return row * LIQUID_DATA_LENGTH + col;
    }

    private void clear() {
        this.light.clear();
        this.height.clear();
    }

}
