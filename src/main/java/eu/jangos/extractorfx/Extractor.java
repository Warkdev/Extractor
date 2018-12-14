/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractorfx;

import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WDT;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
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

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String ADT = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\terrain.MPQ";
    //private static final String ADT = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\terrain.MPQ";
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
    private static final String WMO = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\wmo.MPQ";
    private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    private static final String m2Example = "world\\azeroth\\elwynn\\passivedoodads\\trees\\elwynntreecanopy04.M2";
    private static final String MODEL = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\model.MPQ";
    private static final WDT wdtReader = new WDT();
    private static final ADT adtReader = new ADT();
    private static final ADT2OBJConverter adtConverter = new ADT2OBJConverter(adtReader);
    private static final WMO wmoReader = new WMO();
    private static final ModelConverter wmoConverter = new WMO2OBJConverter(wmoReader);
    private static final M2 m2Reader = new M2();
    private static final ModelConverter m2Converter = new M22OBJConverter(m2Reader);    
    private static final Map<String, ModelConverter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final File adtFile = new File(ADT);
    private static final File modelFile = new File(MODEL);
    private static final File wmoFile = new File(WMO);

    public static void main(String[] args) {

        //extractM2(m2Example);
        //extractAllM2(true);
        //extractWmo(wmoExample, false, true);
        //extractAllWMO(false, false);
        //extractMap(map, true, false, false, false);
        //extractAllMaps(false, false, false, false, false);
        extractAllWdt();
    }

    private static void extractAllWMO(boolean addModels, boolean saveToFile) {
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
                //System.out.println("Extracting WMO... " + path);
                String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
                wmoReader.init(data, path);
                ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
                wmoReader.getLiquidMap(false);
                if (saveToFile) {
                    wmoConverter.saveToFile(outputFile, false, false);
                }

                //System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractWmo(String path, boolean addModels, boolean saveToFile) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmoReader.init(wmoEditor.extractFileAsBytes(path), path);
            ((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
            wmoReader.getLiquidMap(false);
            if (saveToFile) {
                wmoConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
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
                    m2Reader.init(modelEditor.extractFileAsBytes(file), file);
                    ((M22OBJConverter) m2Converter).convert(1, 100000);
                    if (saveToFile) {
                        m2Converter.saveToFile(outputFile, false, false);
                    }
                }
                done++;
                System.out.println("Done: " + done + " / Total: " + total);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractM2(String path) {
        try {
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            m2Reader.init(modelEditor.extractFileAsBytes(path), path);
            ((M22OBJConverter) m2Converter).convert(1, 17);
            m2Converter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO, boolean saveToFile) {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            for (String path : adtEditor.getFileNames()) {
                if (path.endsWith(".adt")) {
                    try {
                        String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
                        adtReader.init(adtEditor.extractFileAsBytes(path), path);
                        for (String wmo : adtReader.getWorldObjects()) {
                            System.out.println(adtReader.getFilename() + ";" + wmo);
                        }
                        adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
                        if (saveToFile) {
                            adtConverter.saveToFile(outputFile, false, false);
                        }
                    } catch (ConverterException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (FileReaderException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO) {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map.png";
            String outputDetailedLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map_details.png";
            adtReader.init(adtEditor.extractFileAsBytes(path), path);
            //adtReader.saveLiquidMap(outputLiquidMap, false);
            //adtReader.saveLiquidMap(outputDetailedLiquidMap, true);
            adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
            adtConverter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllWdt() {
        try {
            JMpqEditor adtEditor = new JMpqEditor(adtFile, MPQOpenOption.READ_ONLY);
            JMpqEditor modelEditor = new JMpqEditor(modelFile, MPQOpenOption.READ_ONLY);
            JMpqEditor wmoEditor = new JMpqEditor(wmoFile, MPQOpenOption.READ_ONLY);

            for (String path : adtEditor.getFileNames()) {
                if (path.endsWith(".wdt")) {
                    try {
                        String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".png";
                        wdtReader.init(adtEditor.extractFileAsBytes(path), path);
                        /**for (String wmo : adtReader.getWorldObjects()) {
                            System.out.println(adtReader.getFilename() + ";" + wmo);
                        }  */
                        wdtReader.saveTileMap(outputFile);
                    } catch (FileReaderException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
