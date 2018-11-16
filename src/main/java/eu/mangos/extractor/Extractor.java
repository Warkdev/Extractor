/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
        String obj = "D:\\Downloads\\Azeroth_32_48.obj";
        ADTFileReader adt = new ADTFileReader();
        File mpq = new File(PATH);
        File objFile = new File(obj);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            /**
             * for(String file : editor.getFileNames()) {
             * if(file.endsWith("adt")) { System.out.println(file); }
            }
             */
            //editor.extractFile(map, new File(file));
            adt.init(editor.extractFileAsBytes(map));

            List<RenderBatch> renderBatches = new ArrayList<>();
            List<Vertex> verticeList = new ArrayList<>();
            List<Integer> indiceList = new ArrayList<>();
            Map<Integer, String> materials = new HashMap<>();

            float initialChunkX = adt.getListMapChunks().get(0).getPosX();
            float initialChunkY = adt.getListMapChunks().get(0).getPosY();

            for (MCNK chunk : adt.getListMapChunks()) {
                int offset = verticeList.size();
                RenderBatch batch = new RenderBatch();

                for (int i = 0, idx = 0; i < 17; i++) {
                    for(int j = 0; j < (((i % 2) != 0) ? 8 : 9); j++) {
                        Vertex vertex = new Vertex();
                        vertex.setNormal(new Vec3f(chunk.getNormals().getPoints()[idx].getZ() / 127.0f, chunk.getNormals().getPoints()[idx].getX() / 127.0f, chunk.getNormals().getPoints()[idx].getY() / 127.0f));
                        vertex.setPosition(new Vec3f(initialChunkX, initialChunkY, initialChunkY));
                    }
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
