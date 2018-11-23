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

/**
 *
 * @author Warkdev
 */
public class M2SkinProfile {
    private M2Array<Short> vertices;
    private M2Array<Short> indices;
    private M2Array<Byte> bones;
    private M2Array<M2SkinSection> subMeshes;
    private M2Array<M2Batch> batches;

    public M2SkinProfile() {
    }

    public M2Array<Short> getVertices() {
        return vertices;
    }

    public void setVertices(M2Array<Short> vertices) {
        this.vertices = vertices;
    }

    public M2Array<Short> getIndices() {
        return indices;
    }

    public void setIndices(M2Array<Short> indices) {
        this.indices = indices;
    }

    public M2Array<Byte> getBones() {
        return bones;
    }

    public void setBones(M2Array<Byte> bones) {
        this.bones = bones;
    }

    public M2Array<M2SkinSection> getSubMeshes() {
        return subMeshes;
    }

    public void setSubMeshes(M2Array<M2SkinSection> subMeshes) {
        this.subMeshes = subMeshes;
    }

    public M2Array<M2Batch> getBatches() {
        return batches;
    }

    public void setBatches(M2Array<M2Batch> batches) {
        this.batches = batches;
    }
    
    
}
