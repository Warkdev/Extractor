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
package eu.jangos.extractorfx.obj;

import eu.jangos.extractor.file.common.Quaternion;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;

/**
 *
 * @author Warkdev
 */
public abstract class ModelConverter {

    protected TriangleMesh mesh;

    public ModelConverter() {
        this.mesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
    }    

    /**
     * This methid returns the OBJ file as a String representation (including
     * carriage return).
     *
     * @return A String object representing the corresponding OBJ file
     * structure.
     */
    public String getOBJasAString(boolean addNormals, boolean addTextures) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.mesh.getPoints().size(); i += 3) {
            sb.append("v " + this.mesh.getPoints().get(i) + " " + this.mesh.getPoints().get(i + 1) + " " + this.mesh.getPoints().get(i + 2) + "\n");
        }

        if(addNormals) {
            for (int i = 0; i < this.mesh.getNormals().size(); i += 3) {
                sb.append("vn " + this.mesh.getNormals().get(i) + " " + this.mesh.getNormals().get(i + 1) + " " + this.mesh.getNormals().get(i + 2) + "\n");
            }
    }

        if(addTextures) {
            for (int i = 0; i < this.mesh.getTexCoords().size(); i += 2) {
                sb.append("vt " + this.mesh.getTexCoords().get(i) + " " + this.mesh.getTexCoords().get(i + 1) + "\n");
            }
        }

        for (int i = 0; i < this.mesh.getFaces().size(); i += 9) {
            sb.append("f " + (this.mesh.getFaces().get(i) + 1) + "/" + (addNormals ? (this.mesh.getFaces().get(i + 1) + 1) : "") + "/" + (addTextures ? (this.mesh.getFaces().get(i + 2) + 1) : "")
                    + " " + (this.mesh.getFaces().get(i + 3) + 1) + "/" + (addNormals ? (this.mesh.getFaces().get(i + 4) + 1) : "") + "/" + (addTextures ? (this.mesh.getFaces().get(i + 5) + 1) : "")
                    + " " + (this.mesh.getFaces().get(i + 6) + 1) + "/" + (addNormals ? (this.mesh.getFaces().get(i + 7) + 1) : "") + "/" + (addTextures ? (this.mesh.getFaces().get(i + 8) + 1) : "") + "\n");            
        }

        return sb.toString();
    }

    /**
     * This method is saving the OBJ file structure to the file given in
     * parameters.
     *
     * @param file The OBJ file (including path) where the structure needs to be
     * saved.
     * @throws ConverterException
     */
    public void saveToFile(String file, boolean addNormals, boolean addTextures) throws ConverterException, IOException {
        if (file == null || file.isEmpty()) {
            throw new ConverterException("Provided file is null or empty.");
        }

        // Check if there's anything to save.
        if (this.mesh.getPoints().size() == 0) {
            return;
        }

        File objFile = new File(file);
        if (objFile.exists()) {
            objFile.delete();
        } else {
            objFile.getParentFile().mkdirs();
        }

        OutputStreamWriter writer = new FileWriter(objFile);
        writer.write(getOBJasAString(addNormals, addTextures));
        writer.close();
    }

    protected Rotate getAngleAndAxis(Quaternion q) {
        Rotate rotate = new Rotate();

        if (q.getW() > 1) {
            q.normalize(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        }
        double angle = Math.toDegrees(2 * Math.acos(q.getW()));
        double s = Math.sqrt(1 - q.getW() * q.getW()); // assuming quaternion normalised then w is less than 1, so term always positive.
        double x = q.getX(); // if it is important that axis is normalised then replace with x=1; y=z=0;
        double y = q.getY();
        double z = q.getZ();
        if (s > 0.001) { // test to avoid divide by zero, s is always positive due to sqrt                    
            x = q.getX() / s; // normalise axis
            y = q.getY() / s;
            z = q.getZ() / s;
        }

        rotate.setAngle(angle);
        rotate.setAxis(new Point3D(x, y, z));

        return rotate;
    }

    /**
     * Provides a rotation object matching the Euler angles provided for rotation.
     * This method assumes that the provided information are in degrees.
     * Tip: WoW rotation for Euler angle is YXZ. ZXY. --> XZY
     * @param attitude The first rotation angle. 
     * @param heading The second rotation angle.
     * @param bank The last rotation angle.
     * @return A Rotate object with Axis & Rotation angle around that axis.
     */
    protected Rotate getAngleAndPivot(double attitude, double heading, double bank) {
        Rotate rotate = new Rotate();

        heading = Math.toRadians(heading);
        attitude = Math.toRadians(attitude);
        bank = Math.toRadians(bank);

        // Assuming the angles are in radians.
        double c1 = Math.cos(heading / 2);
        double s1 = Math.sin(heading / 2);
        double c2 = Math.cos(attitude / 2);
        double s2 = Math.sin(attitude / 2);
        double c3 = Math.cos(bank / 2);
        double s3 = Math.sin(bank / 2);
        double c1c2 = c1 * c2;
        double s1s2 = s1 * s2;
        double w = c1c2 * c3 - s1s2 * s3;
        double x = c1c2 * s3 + s1s2 * c3;
        double y = s1 * c2 * c3 + c1 * s2 * s3;
        double z = c1 * s2 * c3 - s1 * c2 * s3;
        double angle = 2 * Math.acos(w);
        double norm = x * x + y * y + z * z;
        if (norm < 0.001) { // when all euler angles are zero angle =0 so
            // we can set axis to anything to avoid divide by zero
            x = 1;
            y = z = 0;
        } else {
            norm = Math.sqrt(norm);
            x /= norm;
            y /= norm;
            z /= norm;
        }

        rotate.setAngle(Math.toDegrees(angle));
        rotate.setAxis(new Point3D(x, y, z));
        
        return rotate;
    }

    protected void clearMesh() {
        this.mesh.getPoints().clear();
        this.mesh.getNormals().clear();
        this.mesh.getTexCoords().clear();
        this.mesh.getFaces().clear();
    }
    
    public TriangleMesh getMesh() {
        return mesh;
    }

    public void setMesh(TriangleMesh mesh) {
        this.mesh = mesh;
    }

}
