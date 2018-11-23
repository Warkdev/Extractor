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

import com.sun.javafx.geom.Vec3f;

/**
 *
 * @author Warkdev
 */
class M2SkinSection {
    private short SkinSectionId;
    private short Level;
    private short vertexStart;
    private short vertexCount;
    private short indexStart;
    private short indexCount;
    private short boneCount;
    private short boneComboIndex;
    private short boneInfluences;
    private short centerBoneIndex;
    private Vec3f centerPosition;

    public M2SkinSection() {
    }

    public short getSkinSectionId() {
        return SkinSectionId;
    }

    public void setSkinSectionId(short SkinSectionId) {
        this.SkinSectionId = SkinSectionId;
    }

    public short getLevel() {
        return Level;
    }

    public void setLevel(short Level) {
        this.Level = Level;
    }

    public short getVertexStart() {
        return vertexStart;
    }

    public void setVertexStart(short vertexStart) {
        this.vertexStart = vertexStart;
    }

    public short getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(short vertexCount) {
        this.vertexCount = vertexCount;
    }

    public short getIndexStart() {
        return indexStart;
    }

    public void setIndexStart(short indexStart) {
        this.indexStart = indexStart;
    }

    public short getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(short indexCount) {
        this.indexCount = indexCount;
    }

    public short getBoneCount() {
        return boneCount;
    }

    public void setBoneCount(short boneCount) {
        this.boneCount = boneCount;
    }

    public short getBoneComboIndex() {
        return boneComboIndex;
    }

    public void setBoneComboIndex(short boneComboIndex) {
        this.boneComboIndex = boneComboIndex;
    }

    public short getBoneInfluences() {
        return boneInfluences;
    }

    public void setBoneInfluences(short boneInfluences) {
        this.boneInfluences = boneInfluences;
    }

    public short getCenterBoneIndex() {
        return centerBoneIndex;
    }

    public void setCenterBoneIndex(short centerBoneIndex) {
        this.centerBoneIndex = centerBoneIndex;
    }

    public Vec3f getCenterPosition() {
        return centerPosition;
    }

    public void setCenterPosition(Vec3f centerPosition) {
        this.centerPosition = centerPosition;
    }
    
    
}
