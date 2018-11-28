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
import eu.jangos.extractor.file.WMOFileReader;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
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
        String WMO = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\wmo.MPQ";
        String wmoExample = "world\\wmo\\azeroth\\buildings\\nsabbey\\nsabbey.wmo";
        ADTFileReader adt = new ADTFileReader();
        ADT2OBJConverter adtConverter = new ADT2OBJConverter(adt);
        M2FileReader m2 = new M2FileReader();
        M22OBJConverter m2Converter = new M22OBJConverter(m2);
        WMOFileReader wmoReader = new WMOFileReader();
        WMO2OBJConverter wmoConverter = new WMO2OBJConverter(wmoReader);
        File mpq = new File(PATH);
        File modelFile = new File(MODEL);
        File wmoFile = new File(WMO);

        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            adt.init(editor.extractFileAsBytes(map));
            //adtConverter.convert();
            //adtConverter.saveToFile(obj);

            // Exporting M2.
            for (MDDF doodad : adt.getDoodadPlacement()) {
                String mdx = FilenameUtils.removeExtension(adt.getModels().get(doodad.getMmidEntry())) + ".m2";
                int view = 1;

                //world\azeroth\elwynn\passivedoodads\detail\elwynnvineyard\elwynnvineyard01.m2
                String m2ObjFile = "D:\\Downloads\\Test\\" + FilenameUtils.removeExtension(mdx) + "_" + view + ".obj";
                if (modelEditor.hasFile(mdx)) {
                    m2.init(modelEditor.extractFileAsBytes(mdx));
                    m2Converter.convert(view);
                    //m2Converter.saveToFile(m2ObjFile);                    
                }
            }

            // Exporting WMO.
            for (MODF wmo : adt.getWorldObjectsPlacement()) {
                String wmoPath = adt.getWorldObjects().get(wmo.getMwidEntry());
                if (wmoEditor.hasFile(wmoPath)) {
                    wmoReader.init(wmoEditor.extractFileAsBytes(wmoPath));
                } else {
                    System.out.println(wmoPath + ": NOK");
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
        } catch (WMOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
