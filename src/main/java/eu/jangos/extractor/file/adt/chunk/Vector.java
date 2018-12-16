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

/**
 *
 * @author Warkdev
 */
public class Vector {
    private float x;
    private float y;
    private float z;

    public Vector() {
    }
    
    /**
     * Specifity of a vector for blizzard, it's stored as z, x, y.
     * @param z
     * @param x
     * @param y 
     */
    public Vector(float z, float x, float y) {
        this.x = x;
        this.y = y;
        this.z = z;
    }    
    
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
    
    public float getNormalizedX() {
        return (this.x / 127.0f) * -1;
    }
    
    public float getNormalizedY() {
        return this.y / 127.0f;
    }
    
    public float getNormalizedZ() {
        return (this.z / 127.0f) * -1;
    }
}
