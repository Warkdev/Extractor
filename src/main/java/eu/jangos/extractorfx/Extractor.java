/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractorfx;

import eu.jangos.extractor.file.ADTFileReader;
import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.WMOFileReader;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.exception.WMOException;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.shape.MeshView;
import org.apache.commons.io.FilenameUtils;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

/**
 *
 * @author Warkdev
 */
public class Extractor {

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String ADT = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_33_41.adt";    
    private static final String WMO = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\wmo.MPQ";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\duskwood_humantwostory\\duskwood_humantwostory.wmo";
    private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    private static final String m2Example = "world\\generic\\goblin\\passivedoodads\\goblinmachinery\\goblinmachinery.m2";
    private static final String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
    private static final ADTFileReader adtReader = new ADTFileReader();
    private static final ADT2OBJConverter adtConverter = new ADT2OBJConverter(adtReader);
    private static final WMOFileReader wmoReader = new WMOFileReader();
    private static final ModelConverter wmoConverter = new WMO2OBJConverter(wmoReader);
    private static final M2FileReader m2Reader = new M2FileReader();
    private static final ModelConverter m2Converter = new M22OBJConverter(m2Reader);
    private static final Map<String, ModelConverter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);    
    private static final File adtFile = new File(ADT);
    private static final File modelFile = new File(MODEL);
    private static final File wmoFile = new File(WMO);    

    public static void main(String[] args) {

        //extractM2(m2Example);
        //extractAllM2(false);
        //extractWmo(wmoExample);
        //extractAllWMO();
        extractMap(map, true, true, true, true);
    }

    private static void extractAllWMO(boolean addModels) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            int total = wmoEditor.getTotalFileCount();
            int done = 0;
            for (String path : wmoEditor.getFileNames()) {
                done++;
                if (!path.endsWith(".wmo")) {
                    continue;
                }
                byte[] data = wmoEditor.extractFileAsBytes(path);
                if (!wmoReader.isRootFile(data)) {
                    continue;
                }
                System.out.println("Extracting WMO... " + path);
                String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
                wmoReader.init(data, path);
                ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
                wmoConverter.saveToFile(outputFile);

                System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WMOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractWmo(String path, boolean addModels) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmoReader.init(wmoEditor.extractFileAsBytes(path), path);
            ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
            wmoConverter.saveToFile(outputFile);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WMOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            int total = modelEditor.getTotalFileCount();
            int done = 0;
            for (String file : modelEditor.getFileNames()) {
                if (file.endsWith(".m2")) {
                    System.out.println("Extracting M2... " + file);
                    String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(file) + ".obj";
                    m2Reader.init(modelEditor.extractFileAsBytes(file));
                    m2Converter.convert();
                    if(saveToFile)
                        m2Converter.saveToFile(outputFile);
                }
                done++;
                System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (M2Exception ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractM2(String path) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);            
            
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            m2Reader.init(modelEditor.extractFileAsBytes(path));
            m2Converter.convert();
            m2Converter.saveToFile(outputFile);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (M2Exception ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO) {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);            
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);
            
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            adtReader.init(adtEditor.extractFileAsBytes(path), path);
            adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
            adtConverter.saveToFile(outputFile);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
