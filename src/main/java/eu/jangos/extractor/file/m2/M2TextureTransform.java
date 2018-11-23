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
import com.sun.javafx.geom.Vec4f;

/**
 *
 * @author Warkdev
 */
public class M2TextureTransform {
    private M2Track<Vec3f> translation;
    private M2Track<Vec4f> rotation;
    private M2Track<Vec3f> scaling;

    public M2TextureTransform() {
    }

    public M2Track<Vec3f> getTranslation() {
        return translation;
    }

    public void setTranslation(M2Track<Vec3f> translation) {
        this.translation = translation;
    }

    public M2Track<Vec4f> getRotation() {
        return rotation;
    }

    public void setRotation(M2Track<Vec4f> rotation) {
        this.rotation = rotation;
    }

    public M2Track<Vec3f> getScaling() {
        return scaling;
    }

    public void setScaling(M2Track<Vec3f> scaling) {
        this.scaling = scaling;
    }
    
    
}
