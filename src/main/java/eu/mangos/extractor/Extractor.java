/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.Vec3f;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import de.javagl.obj.Obj;
import de.javagl.obj.Objs;
import eu.mangos.extractor.file.ADTFileReader;
import eu.mangos.extractor.file.RenderBatch;
import eu.mangos.extractor.file.Vertex;
import eu.mangos.extractor.file.chunk.MCNK;
import eu.mangos.extractor.file.exception.ADTException;
import eu.mangos.shared.flags.FlagUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.portable.IndirectionException;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

/**
 *
 * @author Warkdev
 */
public class Extractor {

    public static void main(String[] args) {

        String PATH = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
        String map = "World\\Maps\\Azeroth\\Azeroth_34_48.adt";
        String obj = "D:\\Downloads\\Azeroth_34_48.obj";
        ADTFileReader adt = new ADTFileReader();
        File mpq = new File(PATH);
        File objFile = new File(obj);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            
            adt.init(editor.extractFileAsBytes(map));

            // Now, exporting data to OBJ. Hard work starts.
            List<RenderBatch> renderBatches = new ArrayList<>();
            List<Vertex> verticeList = new ArrayList<>();
            List<Integer> indiceList = new ArrayList<>();
            Map<Integer, String> materials = new HashMap<>();

            float initialChunkX = adt.getListMapChunks().get(0).getPosX();
            float initialChunkY = adt.getListMapChunks().get(0).getPosY();
            int index = 0;
            
            for (MCNK chunk : adt.getListMapChunks()) {                
                int offset = verticeList.size();                
                RenderBatch batch = new RenderBatch();

                for (int i = 0, idx = 0; i < 17; i++) {
                    for(int j = 0; j < (((i % 2) != 0) ? 8 : 9); j++) {
                        Vertex vertex = new Vertex();
                        // Calculating Normals.
                        vertex.setNormal(new Vec3f(chunk.getNormals().getPoints()[idx].getX() / 127.0f, chunk.getNormals().getPoints()[idx].getY() / 127.0f, chunk.getNormals().getPoints()[idx].getZ() / 127.0f));                                                
                        float x, y, z;                        
                        // Calculating Positions.
                        x = chunk.getPosY() - (j * ADTFileReader.UNIT_SIZE);
                        y = chunk.getVertices().getPoints()[idx++] + chunk.getPosZ();
                        z = chunk.getPosX() - (i * ADTFileReader.UNIT_SIZE * 0.5f);                                                
                        if(( i % 2) != 0) {
                            x -= 0.5f * ADTFileReader.UNIT_SIZE;
                        }
                        vertex.setPosition(new Vec3f(x, y ,z));
                        // Calculating TexCoord in high resolution.
                        x = ((chunk.getPosX() - initialChunkX) * (-1)) / ADTFileReader.CHUNK_SIZE;
                        y = (chunk.getPosZ() - initialChunkY) * (-1) / ADTFileReader.CHUNK_SIZE;
                        vertex.setTextCoord(new Vec2f(x, y));
                        verticeList.add(vertex);
                    }
                }
                
                batch.setFirstFace(indiceList.size());                
                boolean isHole;
                for (int j = 9, xx = 0, yy = 0; j < 145; j++, xx++) {
                    if(xx >= 8) {
                        xx = 0; ++yy;
                    }
                    isHole = true;
                    
                    // Low res hole ?!
                    if(!FlagUtils.hasFlag(chunk.getFlags(), 0x10000)) {
                        // Calculate current hole number.
                        int currentHole = (int) Math.pow(2, Math.floor(xx / 2f) * 1f + Math.floor(yy / 2f) * 4f);
                        if(!FlagUtils.hasFlag(chunk.getHoles(), currentHole)) {
                            isHole = false;
                        }
                    } else {
                        // Should never be used in Vanilla.
                        System.out.println("Hello world");
                    }
                    
                    if(!isHole) {
                        // Adding triangles
                        indiceList.add(offset + j + 8);
                        indiceList.add(offset + j - 9);
                        indiceList.add(offset + j);
                        
                        indiceList.add(offset + j - 9);
                        indiceList.add(offset + j - 8);
                        indiceList.add(offset + j);
                        
                        indiceList.add(offset + j - 8);
                        indiceList.add(offset + j + 9);
                        indiceList.add(offset + j);
                        
                        indiceList.add(offset + j + 9);
                        indiceList.add(offset + j + 8);
                        indiceList.add(offset + j);
                    }
                    
                    if((j + 1) % (9 + 8) == 0) j += 9;
                }

                materials.put(index + 1, "test_"+index);
                batch.setMaterialID(index + 1);
                
                batch.setNumFaces(indiceList.size() - batch.getFirstFace());
                
                renderBatches.add(batch);
                index++;
            }

            // Now, the truth.
            if(objFile.exists()) {
                objFile.delete();
            }
            OutputStreamWriter writer = new FileWriter(objFile);
            
            for (Vertex v : verticeList) {
                writer.write("v " + v.getPosition().x + " " + v.getPosition().y + " " + v.getPosition().z+"\n");
                writer.write("vt " + v.getTextCoord().x + " " + (-1) * v.getTextCoord().y+"\n");
                writer.write("vn " + v.getNormal().x + " " + v.getNormal().y + " " + v.getNormal().z+"\n");                
            }
            
            for(RenderBatch batch : renderBatches) {
                int i = batch.getFirstFace();
                if(materials.containsKey(batch.getMaterialID())) {
                    writer.write("usemtl "+materials.get(batch.getMaterialID())+"\n");
                    writer.write("s 1"+"\n");                    
                }
                
                while (i < batch.getFirstFace() + batch.getNumFaces()) {
                    writer.write("f " + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "/" 
                            + (indiceList.get(i + 2) + 1) + " " + (indiceList.get(i + 1) + 1) + "/" 
                            + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) + " " 
                            + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1)+"\n");
                    i += 3;
                }
            }
            
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
