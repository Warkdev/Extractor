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
package eu.jangos.extractor.file.common;

import com.sun.javafx.geom.Vec3f;
import java.nio.ByteBuffer;

/**
 * An axis aligned box described by the minimum and maximum point.
 * @author Warkdev
 */
public class CAaBox {
    private Vec3f min;
    private Vec3f max;

    public CAaBox() {
        this.min = new Vec3f();
        this.max = new Vec3f();
    }

    public Vec3f getMin() {
        return min;
    }

    public void setMin(Vec3f min) {
        this.min = min;
    }

    public Vec3f getMax() {
        return max;
    }

    public void setMax(Vec3f max) {
        this.max = max;
    }        
    
    public void read(ByteBuffer data) {
        this.min.set(data.getFloat(), data.getFloat(), data.getFloat());
        this.max.set(data.getFloat(), data.getFloat(), data.getFloat());
    }
}
