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

import com.sun.javafx.geom.Vec3f;
import eu.mangos.shared.flags.FlagUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class MLIQ {
    
    // Used to decode flag value. It's guessed that the flag (one byte), is ordered as made as several meaning:
    // AA BB CC DD. Where DD is the liquid type except if CC = 11.    
    private static final int MASK_LIQUID = 0x03;
    private static final int FLAG_IS_WATER = 0x00;    
    private static final int FLAG_IS_OCEAN = 0x01;
    private static final int FLAG_IS_MAGMA = 0x02;
    private static final int FLAG_IS_SLIME = 0x03;
    private static final int FLAG_IS_ANIMATED = 0x04;
    private static final int MASK_NO_LIQUID = 0x0F;
    private static final int FLAG_E = 0x10;
    private static final int FLAG_F = 0x20;
    private static final int FLAG_FISHABLE = 0x40;
    private static final int FLAG_OVERLAP = 0x80;
    
    // Other flag values are unknown.

    private int xVerts;
    private int yVerts;
    private int xTiles;
    private int yTiles;
    private Vec3f baseCoordinates = new Vec3f();
    private short materialId;

    private List<WaterVert> liquidVertexList = new ArrayList<>();
    
    // Flag is only one byte but as java use signed numbers and wow unsigned one, it's stored as short.
    private List<Short> flags = new ArrayList<>();

    public void read(ByteBuffer data) {
        this.liquidVertexList.clear();
        this.flags.clear();

        this.xVerts = data.getInt();
        this.yVerts = data.getInt();
        this.xTiles = data.getInt();
        this.yTiles = data.getInt();
        this.baseCoordinates.set(data.getFloat(), data.getFloat(), data.getFloat());
        this.materialId = data.getShort();

        WaterVert liquidVertex;
        for (int i = 0; i < this.xVerts * this.yVerts; i++) {
            liquidVertex = new WaterVert();
            liquidVertex.read(data);
            liquidVertexList.add(liquidVertex);
        }

        for (int i = 0; i < this.xTiles * this.yTiles; i++) {
            flags.add((short) Byte.toUnsignedInt(data.get()));
        }
    }

    public int getxVerts() {
        return xVerts;
    }

    public void setxVerts(int xVerts) {
        this.xVerts = xVerts;
    }

    public int getyVerts() {
        return yVerts;
    }

    public void setyVerts(int yVerts) {
        this.yVerts = yVerts;
    }

    public int getxTiles() {
        return xTiles;
    }

    public void setxTiles(int xTiles) {
        this.xTiles = xTiles;
    }

    public int getyTiles() {
        return yTiles;
    }

    public void setyTiles(int yTiles) {
        this.yTiles = yTiles;
    }

    public Vec3f getBaseCoordinates() {
        return baseCoordinates;
    }

    public void setBaseCoordinates(Vec3f baseCoordinates) {
        this.baseCoordinates = baseCoordinates;
    }

    public short getMaterialId() {
        return materialId;
    }

    public void setMaterialId(short materialId) {
        this.materialId = materialId;
    }

    public List<WaterVert> getLiquidVertexList() {
        return liquidVertexList;
    }

    public void setLiquidVertexList(List<WaterVert> liquidVertexList) {
        this.liquidVertexList = liquidVertexList;
    }

    public List<Short> getFlags() {
        return flags;
    }

    public void setFlags(List<Short> flags) {
        this.flags = flags;
    }

    public boolean hasNoLiquid(int row, int col) {
        return hasFlag(row, col, MASK_NO_LIQUID);
    }    
    
    public boolean isWater(int row, int col) {
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
    
    public boolean isAnimated(int row, int col) {
        return !hasNoLiquid(row, col) && hasFlag(row, col, FLAG_IS_ANIMATED);
    }
    
    public boolean isFlagESet(int row, int col) {
        return hasFlag(row, col, FLAG_E);
    }
    
    public boolean isFlagFSet(int row, int col) {
        return hasFlag(row, col, FLAG_F);
    }
    
    public boolean isFishable(int row, int col) {
        return !hasNoLiquid(row, col) && hasFlag(row, col, FLAG_FISHABLE);
    }
    
    public boolean isOverlap(int row, int col) {
        return hasFlag(row, col, FLAG_OVERLAP);
    }   
    
    private boolean hasFlag(int row, int col, int flag) {
        return FlagUtils.hasFlag(this.flags.get(getFlagPosition(row, col)), flag);
    }

    public WaterVert getVertextAt(int row, int col) {
        return this.liquidVertexList.get(getDataPosition(row, col));
    }
    
    private int getDataPosition(int row, int col) {
        return col * xVerts + row;
    }
    
    private int getFlagPosition(int row, int col) {
        return col * xTiles + row;
    }
}
