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
package eu.jangos.extractor.file.wmo;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBox;
import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class WMOGroupInfo {
    private int flags;
    private CAaBox boundingBox = new CAaBox();
    private int nameOffset;

    public void read(ByteBuffer data) {
        this.flags = data.getInt();
        this.boundingBox.setMin(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        this.boundingBox.setMax(new Vec3f(data.getFloat(), data.getFloat(), data.getFloat()));
        this.nameOffset = data.getInt();
    }
    
    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public CAaBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(CAaBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getNameOffset() {
        return nameOffset;
    }

    public void setNameOffset(int nameOffset) {
        this.nameOffset = nameOffset;
    }        
}
