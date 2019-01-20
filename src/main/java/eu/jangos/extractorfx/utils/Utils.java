/*
 * Copyright 2019 Warkdev.
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
package eu.jangos.extractorfx.utils;

import eu.jangos.extractorfx.rendering.PolygonMesh;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Warkdev
 */
public class Utils {
    
    /**
     * This method will print the bounding box of the mesh provided in parameter. It's calculated from the mesh itself.
     * @param mesh The mesh for which the bounding box needs to be calculated.
     */
    public static void printBoundingBox(PolygonMesh mesh) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        
        Point3D point;
        for(int i = 0; i < mesh.getPoints().size(); i += 3) {
            point = new Point3D(mesh.getPoints().get(i), mesh.getPoints().get(i + 1), mesh.getPoints().get(i + 2));
            if (point.getX() < minX) {
                minX = (float) point.getX();
            } else if (point.getX() > maxX) {
                maxX = (float) point.getX();
            }
            
            if (point.getY() < minY) {
                minY = (float) point.getY();
            } else if (point.getY() > maxY) {
                maxY = (float) point.getY();
            }
            
            if (point.getZ() < minZ) {
                minZ = (float) point.getZ();
            } else if (point.getZ() > maxZ) {
                maxZ = (float) point.getZ();
            }
        }
        
        System.out.println(new BoundingBox(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ));        
    }
    
    public static void printBoundingBox(TriangleMesh mesh) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        
        Point3D point;
        for(int i = 0; i < mesh.getPoints().size(); i += 3) {
            point = new Point3D(mesh.getPoints().get(i), mesh.getPoints().get(i + 1), mesh.getPoints().get(i + 2));
            if (point.getX() < minX) {
                minX = (float) point.getX();
            } 
            
            if (point.getX() > maxX) {
                maxX = (float) point.getX();
            }
            
            if (point.getY() < minY) {
                minY = (float) point.getY();
            } 
            
            if (point.getY() > maxY) {
                maxY = (float) point.getY();
            }
            
            if (point.getZ() < minZ) {
                minZ = (float) point.getZ();
            } 
            
            if (point.getZ() > maxZ) {
                maxZ = (float) point.getZ();
            }
        }
        
        System.out.println(new BoundingBox(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ));        
    }
}
