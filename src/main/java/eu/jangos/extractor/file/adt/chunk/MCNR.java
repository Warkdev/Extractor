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

import com.sun.javafx.geom.Vec3d;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class MCNR {    
    private Vec3d[] points = new Vec3d[145];

    public void read(ByteBuffer in) {
        for (int j = 0; j < points.length; j++) {
            this.points[j] = new Vec3d((double) in.get(), (double) in.get(), (double) in.get());
        }
    }
    
    public Vec3d[] getPoints() {
        return points;
    }

    public void setPoints(Vec3d[] points) {
        this.points = points;
    }
    
    
}
