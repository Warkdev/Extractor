/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractorfx;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.ADTFileReader;
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.RenderBatch;
import eu.jangos.extractor.file.Vertex;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
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
        String obj = "D:\\Downloads\\Test\\Azeroth_32_48.obj";
        String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
        ADTFileReader adt = new ADTFileReader();
        ADT2OBJConverter adtConverter = new ADT2OBJConverter(adt);
        M2FileReader m2 = new M2FileReader();
        File mpq = new File(PATH);
        File modelFile = new File(MODEL);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            adt.init(editor.extractFileAsBytes(map));
            adtConverter.convert();
            adtConverter.saveToFile(obj);

            List<M2Vertex> verticesList;
            List<Vertex> vertices = new ArrayList<>();
            List<RenderBatch> renderBatches = new ArrayList<>();
            List<Short> indiceList;
            // Exporting M2.
            for (MDDF doodad : adt.getDoodadPlacement()) {
                RenderBatch batch = new RenderBatch();
                String mdx = FilenameUtils.removeExtension(adt.getModels().get(doodad.getMmidEntry())) + ".m2";
                File mdxFile = new File("D:\\Downloads\\Test\\" + mdx);
                File m2ObjFile = new File("D:\\Downloads\\Test\\" + FilenameUtils.removeExtension(mdx) + ".obj");
                int view = 0;
                if (modelEditor.hasFile(mdx)) {
                    if (m2ObjFile.exists()) {
                        m2ObjFile.delete();
                    }

                    OutputStreamWriter writer = new FileWriter(m2ObjFile);

                    m2.init(modelEditor.extractFileAsBytes(mdx));
                    verticesList = m2.getVertices();
                    // Converting M2Vertex to Vertex.
                    for (M2Vertex v : verticesList) {
                        Vertex vertex = new Vertex();
                        vertex.setPosition(new Vec3f(v.getPosition().x, v.getPosition().z, v.getPosition().y * (-1)));
                        vertex.setNormal(new Vec3f(v.getNormal().x, v.getNormal().z, v.getNormal().y));
                        vertex.setTextCoord(new Vec2f(v.getTexCoords()[0].x, v.getTexCoords()[0].y));

                        writer.write("v " + vertex.getPosition().x + " " + vertex.getPosition().y + " " + vertex.getPosition().z + "\n");
                        writer.write("vt " + vertex.getTextCoord().x + " " + vertex.getTextCoord().y + "\n");
                        writer.write("vn " + vertex.getNormal().x + " " + vertex.getNormal().y + " " + vertex.getNormal().z + "\n");

                    }
                                        
                    indiceList = m2.getSkins().get(view).getIndices();

                    for (int i = 0; i < m2.getSkins().get(view).getSubMeshes().size(); i++) {
                        if (mdx.startsWith("character")) {
                            if (m2.getSkins().get(view).getSubMeshes().get(i).getId() != 0) {
                                if (m2.getSkins().get(view).getSubMeshes().get(i).getId() != 1) {
                                    continue;
                                }
                            }
                        }

                        // Subtilité pour start Triangle !!
                        batch.setFirstFace(m2.getSkins().get(view).getSubMeshes().get(i).getIndexStart());
                        batch.setNumFaces(m2.getSkins().get(view).getSubMeshes().get(i).getIndexCount());
                        batch.setGroupID(i);
                        for (int j = 0; j < m2.getSkins().get(view).getTextureUnit().size(); j++) {
                            if (m2.getSkins().get(view).getTextureUnit().get(j).getSkinSectionIndex() == i) {
                                batch.setBlendType(m2.getSkins().get(view).getTextureUnit().get(j).getMaterialIndex());
                                batch.setMaterialID(m2.getTextureLookup().get(m2.getSkins().get(view).getTextureUnit().get(j).getTextureComboIndex()));
                            }
                        }

                        renderBatches.add(batch);
                    }

                    for (RenderBatch b : renderBatches) {
                        int i = b.getFirstFace();                        

                        while (i < batch.getFirstFace() + batch.getNumFaces()) {
                            writer.write("f " + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) + "/" + (indiceList.get(i) + 1) 
                                    + " " + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) + "/" + (indiceList.get(i + 1) + 1) 
                                    + " " + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "/" + (indiceList.get(i + 2) + 1) + "\n");
                            i += 3;
                        }
                    }

                    writer.close();                    
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (M2Exception ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
