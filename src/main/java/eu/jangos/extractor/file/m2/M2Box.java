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
public class M2Box {
    private Vec3f modelRotationSpeedMin;
    private Vec3f modelRotationSpeedMax;

    public M2Box() {
    }

    public Vec3f getModelRotationSpeedMin() {
        return modelRotationSpeedMin;
    }

    public void setModelRotationSpeedMin(Vec3f modelRotationSpeedMin) {
        this.modelRotationSpeedMin = modelRotationSpeedMin;
    }

    public Vec3f getModelRotationSpeedMax() {
        return modelRotationSpeedMax;
    }

    public void setModelRotationSpeedMax(Vec3f modelRotationSpeedMax) {
        this.modelRotationSpeedMax = modelRotationSpeedMax;
    }
    
    
}
