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
package eu.jangos.extractorfx;

import eu.jangos.extractor.file.ADT;
import eu.jangos.extractor.file.M2;
import eu.jangos.extractor.file.WDT;
import eu.jangos.extractor.file.WMO;
import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractorfx.obj.ADTConverter;
import eu.jangos.extractorfx.obj.M2Converter;
import eu.jangos.extractorfx.obj.ModelConverter;
import eu.jangos.extractorfx.obj.WMOConverter;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Warkdev
 */
public class Extractor {

    private static final Logger logger = LoggerFactory.getLogger(Extractor.class);

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String DATA = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\";
    private static final String azeroth = "world\\maps\\azeroth\\azeroth.wdt";
    private static final String kalimdor = "world\\maps\\kalimdor\\kalimdor.wdt";
    //private static final String map = "World\\Maps\\emeralddream\\emeralddream_33_27.adt";
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_38_25.adt";
    //private static final String map = "world\\maps\\kalimdor\\kalimdor_44_34.adt";
    private static MPQManager manager;
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\KL_Diremaul\\KL_Diremaul_Instance.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_ScarletMonestary\\Monestary_Cathedral.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_raid.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_raid.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_ScarletMonestary\\Monestary_Cathedral.wmo";
    //private static final String wmoExample = "World\\wmo\\Lorderon\\Undercity\\Undercity.wmo";
    //private static final String wmoExample = "world\\wmo\\dungeon\\kl_orgrimmarlavadungeon\\lavadungeon.wmo";
    //private static final String wmoExample = "World\\wmo\\KhazModan\\Cities\\Ironforge\\ironforge.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Buildings\\Stranglethorn_BootyBay\\BootyBay.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\MD_Crypt\\MD_Crypt_D.wmo";
    private static final String wmoExample = "world\\wmo\\azeroth\\buildings\\stormwind\\stormwind.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Buildings\\Prison_Camp\\PrisonOublietteLarge.wmo";
    //private static final String wmoExample = "World\\wmo\\Azeroth\\Collidable Doodads\\Elwynn\\AbbeyGate\\abbeygate01.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\KL_Blackfathom\\Blackfathom_instance.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\LD_Stratholme\\Stratholme_B.wmo";
    //private static final String wmoExample = "World\\wmo\\Kalimdor\\Darnassis\\Darnassis.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\AZ_Blackrock\\Blackrock.wmo";
    //private static final String wmoExample = "World\\wmo\\Lorderon\\Buildings\\EasternPlaguelands\\UndeadZiggurat\\UndeadZiggurat.wmo";
    //private static final String wmoExample = "World\\wmo\\Dungeon\\AZ_Blackrock\\Blackrock_lower_guild.wmo";
    private static final String m2Example = "world\\azeroth\\elwynn\\passivedoodads\\trees\\elwynntreecanopy04.M2";
    private static final WDT wdt = new WDT();
    private static final ADT adt = new ADT();
    private static final ADTConverter adtConverter = new ADTConverter(adt);
    private static final WMO wmo = new WMO();
    private static final WMOConverter wmoConverter = new WMOConverter(wmo);
    private static final M2 model = new M2();
    private static final ModelConverter m2Converter = new M2Converter(model);
    private static final Map<String, M2Converter> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final int MAX_HEIGHT = Integer.MAX_VALUE;

    public static void main(String[] args) throws IOException {
        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            logger.error(ex.getMessage());
        }
        //extractM2(m2Example);
        //extractAllM2(false);
        //extractWmo(wmoExample, true, false);
        extractAllWMO(true, false);        
        //extractMap(map, true, false, false,MAX_HEIGHT, false, MAX_HEIGHT, true);
        //extractAllMaps(false, false, false, false, false);
        //extractAllWdt();
        //extractAllTerrains();
        //extractWdt(azeroth);
        //extractWdt(kalimdor);
        //extractMap();
    }

    private static void extractAllWMO(boolean addModels, boolean saveToFile) {
        int total = manager.getListWMO().size();
        int done = 0;
        for (String path : manager.getListWMO()) {
            try {
                done++;

                byte[] data = manager.getMPQForFile(path).extractFileAsBytes(path);
                if (!wmo.isRootFile(data)) {
                    continue;
                }
                //logger.info("Extracting WMO... " + path);
                String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
                wmo.init(manager, path);                
                //wmoConverter.convert(manager, cache, path, addModels, MAX_HEIGHT);                
                if (saveToFile) {
                    wmoConverter.saveToFile(outputFile, false, false);
                }

            } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
                logger.error(ex.getMessage());
            }
            //logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractWmo(String path, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmo.init(manager, path);
            wmoConverter.convert(manager, cache, path, addModels, MAX_HEIGHT);            
            if (saveToFile) {
                wmoConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllM2(boolean saveToFile) {
        int total = manager.getListM2().size();
        int done = 0;
        for (String path : manager.getListM2()) {
            logger.info("Extracting M2... " + path);
            try {
                done++;
                if (path.endsWith(".m2")) {
                    String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
                    model.init(manager, path);
                    ((M2Converter) m2Converter).convert(1, 100000);
                    if (saveToFile) {
                        m2Converter.saveToFile(outputFile, false, false);
                    }

                }
            } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
                logger.error(ex.getMessage());
            }
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractM2(String path) {
        try {
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            model.init(manager, path);
            ((M2Converter) m2Converter).convert(1, 17);
            m2Converter.saveToFile(outputFile, false, false);
        } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllTerrains() {

        int total = manager.getListWDT().size();
        int done = 0;
        for (String wdtFile : manager.getListWDT()) {
            done++;
            if (wdtFile.endsWith(".wdt")) {
                try {
                    logger.info("Extracting WDT... " + wdtFile);
                    String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
                    String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + ".png";
                    String holesOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_holes.png";
                    String liquidOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_liquid.png";                    
                    wdt.init(manager, wdtFile, true);
                    for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                        for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                            if (wdt.hasTerrain(x, y)) {
                                extractMap(base + "_" + y + "_" + x + ".adt", false, false, false, MAX_HEIGHT, false, MAX_HEIGHT, false);
                            }
                        }
                    }                    
                    wdt.saveLiquidMap(liquidOutputFile);                                        
                } catch (IOException | FileReaderException | MPQException ex) {
                    logger.error(ex.getMessage());
                }
                logger.info("Done: " + done + " / Total: " + total);
            }
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean addModelsInWMO, boolean saveToFile) {
        for (String path : manager.getListADT()) {
            if (path.endsWith(".adt")) {
                try {
                    String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
                    adt.init(manager, path);
                    //adtConverter.convert(wmoEditor, modelEditor, cache, path, yUp, addWMO, addModels, addModelsInWMO);
                    if (saveToFile) {
                        adtConverter.saveToFile(outputFile, false, false);
                    }
                } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }

    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, int modelMaxHeight, boolean addModelsInWMO, int wmoModelMaxHeight, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            String outputLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map.png";
            String outputLiquidHeightMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_heightmap.png";
            String outputDetailedLiquidMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_liquid_map_details.png";
            String outputHoleMap = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + "_hole_map.png";
            adt.init(manager, path);
            //adt.saveLiquidMap(outputDetailedLiquidMap, true);
            //adt.saveLiquidHeightMap(outputLiquidHeightMap);
            adtConverter.convert(manager, cache, path, yUp, addWMO, addModels, modelMaxHeight, addModelsInWMO, wmoModelMaxHeight);
            if (saveToFile) {
                adtConverter.saveToFile(outputFile, false, false);
            }
        } catch (IOException | FileReaderException | MPQException | ConverterException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllWdt() {
        for (String path : manager.getListWDT()) {
            if (path.endsWith(".wdt")) {
                try {
                    String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".png";
                    wdt.init(manager, path, false);
                    //wdtReader.saveTileMap(outputFile);                        
                } catch (IOException | FileReaderException | MPQException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }

    }

    private static void extractWdt(String wdtFile) {

        try {
            logger.info("Extracting WDT... " + wdtFile);
            String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + ".png";
            String holesOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_holes.png";
            String liquidOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_liquid.png";
            String liquidHeightOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_liquid_height.png";
            String liquidLightOutputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile) + "_liquid_light.png";
            wdt.init(manager, wdtFile, true);
            for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                    if (wdt.hasTerrain(x, y)) {
                        extractMap(base + "_" + y + "_" + x + ".adt", false, false, false, MAX_HEIGHT, false, MAX_HEIGHT, false);
                    }
                }
            }
            //wdt.saveLiquidMap(liquidOutputFile);
            //wdt.saveLiquidHeightMap(liquidHeightOutputFile);
            wdt.saveLiquidLightMap(liquidLightOutputFile);
        } catch (IOException | FileReaderException | MPQException ex) {
            logger.error(ex.getMessage());
        }

    }
}
