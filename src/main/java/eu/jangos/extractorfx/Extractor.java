/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractorfx;

import com.esotericsoftware.minlog.Log;
import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WDT;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.obj.ADT2OBJConverter;
import eu.jangos.extractorfx.obj.M22OBJConverter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMO2OBJConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Warkdev
 */
public class Extractor {

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String DATA = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\";
    private static final String adtwowfile = "D:\\Downloads\\Azeroth_32_48.adt";
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_32_48.adt";
    private static MPQManager manager;
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\KL_Diremaul\\KL_Diremaul_Instance.wmo";
    private static final String wmoExample = "World\\wmo\\Dungeon\\LD_ScarletMonestary\\Monestary_Cathedral.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_raid.wmo";
    private static final String m2Example = "world\\azeroth\\elwynn\\passivedoodads\\trees\\elwynntreecanopy04.M2";
    private static final WDT wdtReader = new WDT();
    private static final ADT adtReader = new ADT();
    private static final ADT2OBJConverter adtConverter = new ADT2OBJConverter(adtReader);
    private static final WMO wmoReader = new WMO();
    private static final ModelConverter wmoConverter = new WMO2OBJConverter(wmoReader);
    private static final M2 m2Reader = new M2();
    private static final ModelConverter m2Converter = new M22OBJConverter(m2Reader);
    private static final Map<String, ModelConverter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);    

    public static void main(String[] args) throws IOException {
        // Disable logging for stupid 3rd party library.
        Log.NONE();
        
        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //extractM2(m2Example);
        //extractAllM2(true);
        extractWmo(wmoExample, true, true);
        //extractAllWMO(false, false);
        //extractMap(map, true, false, false, false, false);
        //extractAllMaps(false, false, false, false, false);
        //extractAllWdt();
        //extractAllTerrains();
        //extractMap();
    }

    private static void extractAllWMO(boolean addModels, boolean saveToFile) {
        for (String path : manager.getListWMO()) {
            try {
                int total = manager.getListWMO().size();
                int done = 0;

                done++;
                byte[] data = manager.getMPQForFile(path).extractFileAsBytes(path);
                if (!wmoReader.isRootFile(data)) {
                    continue;
                }
                //System.out.println("Extracting WMO... " + path);
                String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
                wmoReader.init(data, path);
                //((WMO2OBJConverter) (wmoConverter)).convert(manager.getMPQForFile(path), modelEditor, cache, path, addModels);
                wmoReader.getLiquidMap(false);
                if (saveToFile) {
                    wmoConverter.saveToFile(outputFile, false, false);
                }

                //System.out.println("Done: " + done + " / Total: " + total);
            } catch (IOException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConverterException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileReaderException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {

            }
        }
    }

    private static void extractWmo(String path, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmoReader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
            //((WMO2OBJConverter) (wmoConverter)).convert(wmoEditor, modelEditor, cache, path, addModels);
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
        } catch (MPQException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        for (String path : manager.getListM2()) {
            try {
                int total = manager.getListM2().size();
                int done = 0;
                if (path.endsWith(".mdx")) {                    
                    System.out.println("Extracting M2... " + path);
                    String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
                    m2Reader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
                    ((M22OBJConverter) m2Converter).convert(1, 100000);
                    if (saveToFile) {
                        m2Converter.saveToFile(outputFile, false, false);
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
            } catch (MPQException ex) {
                Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void extractM2(String path) {
        try {
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";            
            m2Reader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
            ((M22OBJConverter) m2Converter).convert(1, 17);
            m2Converter.saveToFile(outputFile, false, false);
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MPQException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllTerrains() {
        try {
            for (String wdt : manager.getListWDT()) {
                if (wdt.endsWith(".wdt")) {
                    try {
                        String base = FilenameUtils.getPath(wdt) + FilenameUtils.getBaseName(wdt);
                        String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdt) + ".png";
                        wdtReader.init(manager.getMPQForFile(wdt).extractFileAsBytes(wdt), wdt);
                        for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                            for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                                if (wdtReader.hasTerrain(x, y)) {
                                    extractMap(base + "_" + y + "_" + x + ".adt", false, false, false, false, false);
                                }
                            }
                        }
                    } catch (FileReaderException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MPQException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO, boolean saveToFile) {
        try {
            for (String path : manager.getListADT()) {
                if (path.endsWith(".adt")) {
                    try {
                        String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
                        adtReader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
                        for (String wmo : adtReader.getWorldObjects()) {
                            System.out.println(adtReader.getFilename() + ";" + wmo);
                        }
                        //adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
                        if (saveToFile) {
                            adtConverter.saveToFile(outputFile, false, false);

                        }
                    } catch (ConverterException ex) {
                        Logger.getLogger(Extractor.class
                                .getName()).log(Level.SEVERE, null, ex);

                    } catch (FileReaderException ex) {
                        Logger.getLogger(Extractor.class
                                .getName()).log(Level.SEVERE, null, ex);

                    } catch (MPQException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map.png";
            String outputDetailedLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map_details.png";
            adtReader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
            //adtReader.saveLiquidMap(outputLiquidMap, false);
            //adtReader.saveLiquidMap(outputDetailedLiquidMap, true);
            //adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
            if (saveToFile) {
                adtConverter.saveToFile(outputFile, false, false);

            }

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (ConverterException ex) {
            Logger.getLogger(Extractor.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (FileReaderException ex) {
            Logger.getLogger(Extractor.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (MPQException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void extractAllWdt() {
        try {
            for (String path : manager.getListWDT()) {
                if (path.endsWith(".wdt")) {
                    try {
                        String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".png";
                        wdtReader.init(manager.getMPQForFile(path).extractFileAsBytes(path), path);
                        //wdtReader.saveTileMap(outputFile);                        
                    } catch (FileReaderException ex) {
                        Logger.getLogger(Extractor.class
                                .getName()).log(Level.SEVERE, null, ex);

                    } catch (MPQException ex) {
                        Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
