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
package eu.jangos.extractor.file;

import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractor.file.common.Quaternion;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import eu.jangos.extractorfx.rendering.FileType2D;
import eu.jangos.extractorfx.rendering.FileType3D;
import eu.jangos.extractorfx.rendering.PolygonMesh;
import eu.jangos.extractorfx.rendering.Render2DType;
import eu.jangos.extractorfx.rendering.Render3DType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Warkdev
 */
public abstract class ModelRenderer {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelRenderer.class);
    
    protected PolygonMesh shapeMesh;
    protected PolygonMesh liquidMesh;

    public ModelRenderer() {
        this.liquidMesh = new PolygonMesh();
        this.shapeMesh = new PolygonMesh();
    }

    /**
     * Render2D provides a Pane object containing a 2D representation of the requested render type.
     * @param renderType The type of render.     
     * @param width The width of the pane to be displayed.
     * @param height The height of the pane to be displayed.
     * @return A Pane object representing the object to be rendered.
     * @throws ConverterException This method can throw a converter exception if an error occured during the rendering.
     * @throws FileReaderException This method can throw a file reader exception if an error occured during the reading of the file.
     */
    public abstract Pane render2D(Render2DType renderType, int width, int height) throws ConverterException, FileReaderException;
    
    /**
     * Render3D provides a PolygonMesh object containing a 3D representation of the requested render type.
     * @param renderType     
     * @param cache
     * @return A PolygonMesh object representing the object to be rendered.
     * @throws ConverterException This method can throw a converter exception if an error occured during the rendering.
     * @throws MPQException This method can throw a MPQ exception if a file to be extracted from the MPQ doesn't exist in the MPQ libraries.
     * @throws FileReaderException This method can throw a File Reader Exception if an extracted file is malformed.
     */
    public abstract PolygonMesh render3D(Render3DType renderType, Map<String, M2> cache) throws ConverterException, MPQException, FileReaderException;
    
    /**
     * Save2D saves an image of a rendered object using the method render2D.
     * @param path The path where the file needs to be stored.
     * @param fileType The file type extension to be saved.
     * @param renderType The render type.
     * @param width
     * @param height
     * @throws ConverterException This method throws a converter exception if an error occured during the rendering.
     * @throws FileReaderException This method throws a file reader exception if an error occured during the reading of the file.
     * @return True if the file has been saved, false otherwise.
     */
    public boolean save2D(String path, FileType2D fileType, Render2DType renderType, int width, int height) throws ConverterException, FileReaderException {
        switch(fileType) {
            case PNG:
                return savePNG(path, renderType, width, height);
            default:
                throw new UnsupportedOperationException("This file type is not supported.");
        }
    }
    
    /**
     * Save3D saves an image of a rendered object using the method render3D.
     * @param path The path where the file needs to be stored.
     * @param fileType The file type extension to be saved.
     * @param renderType The render type to be used.
     * @param addTextures Indicates whether texture informations must be added to the saved file.
     * @throws ConverterException This method can throw a converter exception in case of some conversion went wront.
     * @return True if the file has been saved, false otherwise.
     */
    public  boolean save3D(String path, FileType3D fileType, Render3DType renderType, boolean addTextures) throws ConverterException {
        switch(fileType) {
            case OBJ:
                return saveWavefront(path, renderType, addTextures);
            default:
                throw new UnsupportedOperationException("This file type is not supported.");
        }
    }      
            
    /**
     * Return the provided mesh under the form of a String representing a WAvefront OBJ format.
     * This method assumes that the provided mesh is complete and that it can be rendered.
     * @param mesh The mesh to convert to Wavefront OBJ format.
     * @param addTextures Specify whether texture coordinates must be added to the OBJ format or not.
     * @return A String containing the content of the Wavefront OBJ format.
     */
    private String getMeshAsOBJ(PolygonMesh mesh, boolean addTextures) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mesh.getPoints().size(); i += 3) {
            sb.append("v ").append(mesh.getPoints().get(i)).append(" ").append(mesh.getPoints().get(i + 1)).append(" ").append(mesh.getPoints().get(i + 2)).append("\n");
        }

        if (addTextures) {
            for (int i = 0; i < mesh.getTexCoords().size(); i += 2) {
                sb.append("vt ").append(mesh.getTexCoords().get(i)).append(" ").append(mesh.getTexCoords().get(i + 1)).append("\n");
            }
        }

        for (int i = 0; i < mesh.faces.length; i++) {
            sb.append("f ");
            for (int j = 0; j < mesh.faces[i].length; j += 2) {
                sb.append(mesh.faces[i][j] + 1);
                if (addTextures) {
                    sb.append("/").append(mesh.faces[i][j + 1]);
                }
                sb.append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * This methid returns the OBJ file as a String representation (including
     * carriage return).
     *
     * @return A String object representing the corresponding OBJ file
     * structure.
     */
    /**public String getOBJasAString(boolean addNormals, boolean addTextures) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.mesh.getPoints().size(); i += 3) {
            sb.append("v " + this.mesh.getPoints().get(i) + " " + this.mesh.getPoints().get(i + 1) + " " + this.mesh.getPoints().get(i + 2) + "\n");
        }

        if (addNormals) {
            for (int i = 0; i < this.mesh.getNormals().size(); i += 3) {
                sb.append("vn " + this.mesh.getNormals().get(i) + " " + this.mesh.getNormals().get(i + 1) + " " + this.mesh.getNormals().get(i + 2) + "\n");
            }
        }

        if (addTextures) {
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
    }*/

    /**
     * This method is saving the OBJ file structure to the file given in
     * parameters.
     *
     * @param file The OBJ file (including path) where the structure needs to be
     * saved.
     * @param renderType
     * @param  addTextures
     * @throws ConverterException     
     */
    private boolean saveWavefront(String file, Render3DType renderType, boolean addTextures) throws ConverterException {
        if (file == null || file.isEmpty()) {
            throw new ConverterException("Provided file is null or empty.");
        }

        String content;
        
        switch(renderType) {
            case MODEL:
            case TERRAIN:
                content = getMeshAsOBJ(this.shapeMesh, addTextures);
                break;
            case LIQUID:
                content = getMeshAsOBJ(this.liquidMesh, addTextures);
                break;
            default:
                throw new ConverterException("This render type is not supported");
        }        

        File objFile = new File(file);
        if (objFile.exists()) {
            objFile.delete();
        } else {
            objFile.getParentFile().mkdirs();
        }

        try {
            OutputStreamWriter writer = new FileWriter(objFile);
            writer.write(content);
            writer.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return false;
        }
        
        return true;
    }

    /**
     * Save a JavaFX Pane object to PNG file.
     * @param path The file where this image must be saved.
     * @param renderType The desired render type that will be used to render this object.     
     * @return True if the file has been saved, false otherwise.
     * @throws ConverterException 
     */
    private boolean savePNG(String path, Render2DType renderType, int width, int height) throws ConverterException, FileReaderException {
        if (path == null || path.isEmpty()) {
            throw new ConverterException("Provided file is null or empty.");
        }       
                
        Pane pane = render2D(renderType, width, height);             
        
        WritableImage image = new WritableImage(width, height);
        SnapshotParameters params = new SnapshotParameters();                                
        pane.snapshot(params, image);                                
        
        File file = new File(path);
        if(file.exists()) {
            file.delete();            
        } else {
            file.getParentFile().mkdirs();
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return false;
        }
        
        return true;
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
     * Provides a rotation object matching the Euler angles provided for
     * rotation. This method assumes that the provided information are in
     * degrees. Tip: WoW rotation for Euler angle is YXZ. ZXY. --> XZY
     *
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
        clearLiquidMesh();
        clearShapeMesh();
    }
    
    protected void clearLiquidMesh() {
        this.liquidMesh.getPoints().clear();        
        this.liquidMesh.getFaceSmoothingGroups().clear();        
        this.liquidMesh.getTexCoords().clear();        
    }

    protected void clearShapeMesh() {        
        this.shapeMesh.getPoints().clear();        
        this.shapeMesh.getFaceSmoothingGroups().clear();        ;
        this.shapeMesh.getTexCoords().clear();
    }

    public PolygonMesh getShapeMesh() {
        return shapeMesh;
    }

    public void setShapeMesh(PolygonMesh shapeMesh) {
        this.shapeMesh = shapeMesh;
    }

    public PolygonMesh getLiquidMesh() {
        return liquidMesh;
    }

    public void setLiquidMesh(PolygonMesh liquidMesh) {
        this.liquidMesh = liquidMesh;
    }

    

}
