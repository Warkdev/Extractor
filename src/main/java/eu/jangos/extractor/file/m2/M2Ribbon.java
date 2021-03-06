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
package eu.jangos.extractor.file.m2;

import javafx.geometry.Point3D;

/**
 *
 * @author Warkdev
 */
public class M2Ribbon {
    private int ribbonId;
    private int boneIndex;
    private Point3D position;
    private M2Array<Short> textureIndices;
    private M2Array<Short> materialIndices;
    private M2Track<Point3D> colorTrack;
    private M2Track<Short> alphaTrack;
    private M2Track<Float> heightAboveTrack;
    private M2Track<Float> heightBelowTrack;
    private float edgesPerSecond;
    private float edgeLifeTime;
    private float gravity;
    private short textureRows;
    private short textureCols;
    private M2Track<Short> texSlotTrack;
    private M2Track<Boolean> visibilityTrack;

    public M2Ribbon() {
    }

    public int getRibbonId() {
        return ribbonId;
    }

    public void setRibbonId(int ribbonId) {
        this.ribbonId = ribbonId;
    }

    public int getBoneIndex() {
        return boneIndex;
    }

    public void setBoneIndex(int boneIndex) {
        this.boneIndex = boneIndex;
    }

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    public M2Array<Short> getTextureIndices() {
        return textureIndices;
    }

    public void setTextureIndices(M2Array<Short> textureIndices) {
        this.textureIndices = textureIndices;
    }

    public M2Array<Short> getMaterialIndices() {
        return materialIndices;
    }

    public void setMaterialIndices(M2Array<Short> materialIndices) {
        this.materialIndices = materialIndices;
    }

    public M2Track<Point3D> getColorTrack() {
        return colorTrack;
    }

    public void setColorTrack(M2Track<Point3D> colorTrack) {
        this.colorTrack = colorTrack;
    }

    public M2Track<Short> getAlphaTrack() {
        return alphaTrack;
    }

    public void setAlphaTrack(M2Track<Short> alphaTrack) {
        this.alphaTrack = alphaTrack;
    }

    public M2Track<Float> getHeightAboveTrack() {
        return heightAboveTrack;
    }

    public void setHeightAboveTrack(M2Track<Float> heightAboveTrack) {
        this.heightAboveTrack = heightAboveTrack;
    }

    public M2Track<Float> getHeightBelowTrack() {
        return heightBelowTrack;
    }

    public void setHeightBelowTrack(M2Track<Float> heightBelowTrack) {
        this.heightBelowTrack = heightBelowTrack;
    }

    public float getEdgesPerSecond() {
        return edgesPerSecond;
    }

    public void setEdgesPerSecond(float edgesPerSecond) {
        this.edgesPerSecond = edgesPerSecond;
    }

    public float getEdgeLifeTime() {
        return edgeLifeTime;
    }

    public void setEdgeLifeTime(float edgeLifeTime) {
        this.edgeLifeTime = edgeLifeTime;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public short getTextureRows() {
        return textureRows;
    }

    public void setTextureRows(short textureRows) {
        this.textureRows = textureRows;
    }

    public short getTextureCols() {
        return textureCols;
    }

    public void setTextureCols(short textureCols) {
        this.textureCols = textureCols;
    }

    public M2Track<Short> getTexSlotTrack() {
        return texSlotTrack;
    }

    public void setTexSlotTrack(M2Track<Short> texSlotTrack) {
        this.texSlotTrack = texSlotTrack;
    }

    public M2Track<Boolean> getVisibilityTrack() {
        return visibilityTrack;
    }

    public void setVisibilityTrack(M2Track<Boolean> visibilityTrack) {
        this.visibilityTrack = visibilityTrack;
    }
    
    
}
