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

    private static final int HAS_LIQUID = 0x02;
    private static final int NO_LIQUID = 0x0F;
    private static final int IS_MAGMA = 0x40;

    private int xVerts;
    private int yVerts;
    private int xTiles;
    private int yTiles;
    private Vec3f baseCoordinates = new Vec3f();
    private short materialId;

    private List<WMOLVert> liquidVertexList = new ArrayList<>();
    private List<Short> flags = new ArrayList<>();

    public void read(ByteBuffer data, LiquidTypeEnum liquidType) {
        this.liquidVertexList.clear();
        this.flags.clear();

        this.xVerts = data.getInt();
        this.yVerts = data.getInt();
        this.xTiles = data.getInt();
        this.yTiles = data.getInt();
        this.baseCoordinates.set(data.getFloat(), data.getFloat(), data.getFloat());
        this.materialId = data.getShort();

        WMOLVert liquidVertex;
        for (int i = 0; i < this.xVerts * this.yVerts; i++) {
            liquidVertex = new WMOLVert();
            liquidVertex.read(data, liquidType);
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

    public List<WMOLVert> getLiquidVertexList() {
        return liquidVertexList;
    }

    public void setLiquidVertexList(List<WMOLVert> liquidVertexList) {
        this.liquidVertexList = liquidVertexList;
    }

    public List<Short> getFlags() {
        return flags;
    }

    public void setFlags(List<Short> flags) {
        this.flags = flags;
    }

    public boolean hasNoLiquid(int row, int col) {
        return hasFlag(row, col, NO_LIQUID);
    }

    public boolean hasLiquid(int row, int col) {
        return hasFlag(row, col, HAS_LIQUID);
    }

    public boolean isMagma(int row, int col) {
        return hasFlag(row, col, IS_MAGMA);
    }
    
    private boolean hasFlag(int row, int col, int flag) {
        return FlagUtils.hasFlag(this.flags.get(getFlagPosition(row, col)), flag);
    }

    private int getFlagPosition(int row, int col) {
        return col * xTiles + row;
    }
}
