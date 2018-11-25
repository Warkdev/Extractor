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

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class M2Vertex {
    private Vec3f position;
    private byte[] boneWeights = new byte[4];
    private byte[] boneIndices = new byte[4];
    private Vec3f normal;
    private Vec2f[] texCoords = new Vec2f[2];

    public M2Vertex() {
        this.position = new Vec3f();
        this.normal = new Vec3f();
        for(int i = 0; i < texCoords.length; i++) {
            this.texCoords[i] = new Vec2f();
        }
    }

    public void read(ByteBuffer data) {
        this.position.set(data.getFloat(), data.getFloat(), data.getFloat());
        data.get(this.boneWeights);
        data.get(this.boneIndices);
        this.normal.set(data.getFloat(), data.getFloat(), data.getFloat());
        for(int i = 0; i < this.texCoords.length; i++) {
            texCoords[i].set(data.getFloat(), data.getFloat());
        }
    }
    
    public Vec3f getPosition() {
        return position;
    }

    public void setPosition(Vec3f position) {
        this.position = position;
    }

    public byte[] getBoneWeights() {
        return boneWeights;
    }

    public void setBoneWeights(byte[] boneWeights) {
        this.boneWeights = boneWeights;
    }

    public byte[] getBoneIndices() {
        return boneIndices;
    }

    public void setBoneIndices(byte[] boneIndices) {
        this.boneIndices = boneIndices;
    }

    public Vec3f getNormal() {
        return normal;
    }

    public void setNormal(Vec3f normal) {
        this.normal = normal;
    }

    public Vec2f[] getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vec2f[] texCoords) {
        this.texCoords = texCoords;
    }        
}
