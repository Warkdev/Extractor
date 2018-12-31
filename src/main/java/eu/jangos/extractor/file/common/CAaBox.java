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

import java.nio.ByteBuffer;
import javafx.geometry.Point3D;

/**
 * An axis aligned box described by the minimum and maximum point.
 * @author Warkdev
 */
public class CAaBox {
    private Point3D min;
    private Point3D max;

    public CAaBox() {
        
    }

    public Point3D getMin() {
        return min;
    }

    public void setMin(Point3D min) {
        this.min = min;
    }

    public Point3D getMax() {
        return max;
    }

    public void setMax(Point3D max) {
        this.max = max;
    }        
    
    public void read(ByteBuffer data) {
        this.min = new Point3D(data.getFloat(), data.getFloat(), data.getFloat());
        this.max = new Point3D(data.getFloat(), data.getFloat(), data.getFloat());
    }
}
