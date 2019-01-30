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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import org.apache.commons.lang3.ArrayUtils;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastConstants;

/**
 *
 * @author Warkdev
 */
public class Utils {

    /**
     * This method will print the bounding box of the mesh provided in
     * parameter. It's calculated from the mesh itself.
     *
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
        for (int i = 0; i < mesh.getPoints().size(); i += 3) {
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
        for (int i = 0; i < mesh.getPoints().size(); i += 3) {
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

    public static void savePolygonMesh(PolyMesh pMesh, String location) {
        if(pMesh == null) {
            return;
        }
        File file = new File(location);
        StringBuilder sb = new StringBuilder();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        int nvp = pMesh.nvp;
        float cs = pMesh.cs;
        float ch = pMesh.ch;
        float[] orig = pMesh.bmin;
        List<Integer> index = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < pMesh.npolys; i++) {
            int[] p = ArrayUtils.subarray(pMesh.polys, i * nvp * 2, i * nvp * 2 + (nvp * 2));
            int[] vi = new int[3];
            for (int j = 2; j < nvp; j++) {
                if (p[j] == RecastConstants.RC_MESH_NULL_IDX) {
                    break;
                }
                vi[0] = p[0];
                vi[1] = p[j - 1];
                vi[2] = p[j];
                for (int k = 0; k < 3; k++) {
                    int[] v = ArrayUtils.subarray(pMesh.verts, vi[k] * 3, vi[k] * 3 + 3);
                    float x = orig[0] + v[0] * cs;
                    float y = orig[1] + (v[1] + 1) * ch;
                    float z = orig[2] + v[2] * cs;
                    sb.append("v " + x + " " + y + " " + z+"\n");
                    counter++;
                    if (counter % 3 == 0) {
                        index.add(counter - 2);
                        index.add(counter);
                        index.add(counter - 1);
                    }
                }
            }
        }

        for (int i = 0; i < index.size(); i += 3) {
            sb.append("f " + index.get(i) + " " + index.get(i + 1) + " " + index.get(i + 2)+"\n");
        }
        
        try {
            try (OutputStreamWriter writer = new FileWriter(file)) {
                writer.write(sb.toString());
            }
        } catch (IOException ex) {  
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     * @param dMesh
     * @param location 
     * @see https://github.com/recastnavigation/recastnavigation/blob/05b2b8da80037887d3e79af8a59a8f0f8ed02602/DebugUtils/Source/RecastDebugDraw.cpp
     * @see 
     */
    public static void savePolygonDetailMesh(PolyMeshDetail dMesh, String location) {
        if(dMesh == null) {
            return;
        }
        File file = new File(location);
        StringBuilder sb = new StringBuilder();
        
        if(file.exists()) {
            file.delete();
        } else {
            file.getParentFile().mkdirs();
        }
        
        List<Integer> index = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < dMesh.nmeshes; i++) {
            int[] m = ArrayUtils.subarray(dMesh.meshes, i*4, i*4+4);
            int bVerts = m[0];
            int bTris = m[2];
            int nTris = m[3];
            float[] verts = ArrayUtils.subarray(dMesh.verts, bVerts*3, dMesh.verts.length);
            int[] tris = ArrayUtils.subarray(dMesh.tris, bTris*4, dMesh.tris.length);
            
            for (int j = 0; j < nTris; j++) {
                sb.append("v "+verts[tris[j*4+0]*3]+" "+verts[(tris[j*4+0]*3)+1]+" "+verts[(tris[j*4+0]*3)+2]+"\n");
                sb.append("v "+verts[tris[j*4+1]*3]+" "+verts[(tris[j*4+1]*3)+1]+" "+verts[(tris[j*4+1]*3)+2]+"\n");
                sb.append("v "+verts[tris[j*4+2]*3]+" "+verts[(tris[j*4+2]*3)+1]+" "+verts[(tris[j*4+2]*3)+2]+"\n");                
                counter+=3;
                index.add(counter - 2);
                index.add(counter);
                index.add(counter - 1);
            }                        
        }
        
        for (int i = 0; i < index.size(); i += 3) {
            sb.append("f " + index.get(i) + " " + index.get(i + 1) + " " + index.get(i + 2)+"\n");
        }
        
        try {
            try (OutputStreamWriter writer = new FileWriter(file)) {
                writer.write(sb.toString());
            }
        } catch (IOException ex) {  
            ex.printStackTrace();
        }
    }
}
