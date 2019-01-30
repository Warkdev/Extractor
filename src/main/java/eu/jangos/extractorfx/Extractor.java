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

import eu.jangos.extractor.file.exception.FileReaderException;
import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.exception.ModelRendererException;
import eu.jangos.extractor.file.impl.ADT;
import eu.jangos.extractor.file.impl.M2;
import eu.jangos.extractor.file.impl.WDT;
import eu.jangos.extractor.file.impl.WMO;
import eu.jangos.extractor.file.mpq.MPQManager;
import eu.jangos.extractor.pathfinding.NavMeshHeader;
import eu.jangos.extractor.pathfinding.RecastParameters;
import eu.jangos.extractorfx.rendering.FileType2D;
import eu.jangos.extractorfx.rendering.FileType3D;
import eu.jangos.extractorfx.rendering.Render2DType;
import eu.jangos.extractorfx.rendering.Render3DType;
import eu.jangos.extractorfx.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.recast4j.detour.DetourBuilder;
import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.FindRandomPointResult;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.detour.io.NavMeshSetHeader;
import org.recast4j.recast.AreaModification;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastConstants;
import org.recast4j.recast.geom.InputGeomProvider;
import org.recast4j.recast.geom.SimpleInputGeomProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Warkdev
 */
public class Extractor extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Extractor.class);

    private static final String ROOT = "D:\\Downloads\\Test\\";
    private static final String DATA = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\";
    private static final String azeroth = "world\\maps\\azeroth\\azeroth.wdt";
    private static final String kalimdor = "world\\maps\\kalimdor\\kalimdor.wdt";
    //private static final String map = "World\\Maps\\emeralddream\\emeralddream_33_27.adt";
    //private static final String map = "World\\Maps\\deadminesinstance\\deadminesinstance_33_31.adt";
    private static final int idxX = 31;
    private static final int idxY = 49;
    private static final String map = "World\\Maps\\Azeroth\\Azeroth_"+idxX+"_"+idxY+".adt";
    //private static final String map = "World\\Maps\\Azeroth\\Azeroth_30_47.adt";
    //private static final String map = "world\\maps\\kalimdor\\kalimdor_30_11.adt";
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
    //private static final String m2Example = "world\\azeroth\\burningsteppes\\passivedoodads\\fallingembers\\fallingembers.m2";
    //private static final String m2Example = "world\\azeroth\\burningsteppes\\passivedoodads\\smoke\\ashtreesmoke01.m2";
    private static final WDT wdt = new WDT();
    private static final ADT adt = new ADT();
    private static final WMO wmo = new WMO();
    private static final Map<String, M2> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static MeshData meshData;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            manager = new MPQManager(DATA);
        } catch (MPQException ex) {
            logger.error(ex.getMessage());
        }
        //extractModel(m2Example, true);
        //extractAllModels(false);
        //extractWmo(wmoExample, true, true);
        //extractAllWMO(true, false);        
        //extractMap(map, true, true, true, MAX_HEIGHT, true, MAX_HEIGHT, true);
        //extractAllMaps(true, true, true, true, true);
        //extractAllWdt();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //extractAllTerrains();
                    adt.init(manager, map, idxX, idxY);
                    adt.setAddModels(true);
                    adt.setAddWMO(true);
                    adt.setAddLiquid(true);
                    /**
                     * if
                     * (adt.save3D("D:\\Mangos\\recastnavigation\\RecastDemo\\Bin\\Meshes\\terrain.obj",
                     * FileType3D.OBJ, Render3DType.COLLISION_TERRAIN, false,
                     * false)) { logger.info("File saved!"); } else {
                     * logger.info("Error!"); }
                     */
                                        
                    String file = ROOT + FilenameUtils.getBaseName(map) + ".obj";                    
                    String liquid = ROOT + FilenameUtils.getBaseName(map) + "_liquid.obj";
                    String pMeshFile = ROOT + FilenameUtils.getBaseName(map) + "_pmesh.obj";
                    String dMeshFile = ROOT + FilenameUtils.getBaseName(map) + "_dmesh.obj";                    
                    adt.save3D(file, FileType3D.OBJ, Render3DType.COLLISION_TERRAIN, false, false);
                    //adt.render3D(Render3DType.COLLISION_TERRAIN, cache);
                    int[] indices = new int[adt.getShapeMesh().faces.length * 3];
                    for (int i = 0; i < adt.getShapeMesh().faces.length; i++) {
                        for (int j = 0; j < adt.getShapeMesh().faces[i].length; j += 2) {
                            indices[i * 3 + (j / 2)] = adt.getShapeMesh().faces[i][j];
                        }
                    }
                    InputGeomProvider geometry = new SimpleInputGeomProvider(adt.getShapeMesh().getPoints().toArray(null), indices);
                                                           
                    RecastConfig cfg = new RecastConfig(RecastConstants.PartitionType.WATERSHED, RecastParameters.CELL_SIZE, RecastParameters.CELL_HEIGHT, 
                            RecastParameters.AGENT_HEIGHT, RecastParameters.AGENT_RADIUS, RecastParameters.AGENT_CLIMB, RecastParameters.AGENT_MAX_SLOPE_ANGLE, 
                            RecastParameters.REGION_MIN_AREA, RecastParameters.REGION_MERGE_AREA, RecastParameters.POLY_MAX_EDGE_LEN , RecastParameters.POLY_MAX_EDGE_ERROR, 
                            RecastParameters.POLY_VERTS_PER_POLYGON, RecastParameters.DETAIL_SAMPLE_DIST, RecastParameters.DETAIL_SAMPLE_MAX_ERROR, RecastParameters.VERTEX_PER_TILE, new AreaModification(0x1, 0x7));
                    RecastBuilderConfig bcfg = new RecastBuilderConfig(cfg, geometry.getMeshBoundsMin(), geometry.getMeshBoundsMax());
                    RecastBuilder rcBuilder = new RecastBuilder();
                    /**int[] twh = Recast.calcTileCount(geometry.getMeshBoundsMin(), geometry.getMeshBoundsMax(), cfg.cs, cfg.tileSize);                    
                    System.out.println(Arrays.toString(twh));*/

                    logger.info("Building RC Result.");
                    /**RecastBuilder.RecastBuilderResult[][] rcResult = rcBuilder.buildTiles(geometry, cfg, 8);                                           
                    logger.info("Build done.");
                    for(int i = 0; i < rcResult.length; i++) {
                        for (int j = 0; j < rcResult[i].length; j++) {
                            PolyMesh pMesh = rcResult[i][j].getMesh();
                            logger.info("Get Mesh done.");
                            PolyMeshDetail dMesh = rcResult[i][j].getMeshDetail();
                            logger.info("Get Mesh Details done.");
                            // Let's try to convert pMesh in OBJ file.                                        
                            Utils.savePolygonMesh(pMesh, pMeshFile);
                    
                            // Now dMesh in OBJ file.
                            Utils.savePolygonDetailMesh(dMesh, dMeshFile);
                        }
                    }  */                  
                    
                    NavMeshDataCreateParams params = new NavMeshDataCreateParams();                    
                    /**params.verts = pMesh.verts;
                    params.vertCount = pMesh.nverts;
                    params.polys = pMesh.polys;
                    params.polyAreas = pMesh.areas;
                    params.polyFlags = pMesh.flags;
                    params.polyCount = pMesh.npolys;                    
                    params.nvp = pMesh.nvp;
                    params.detailMeshes = dMesh.meshes;
                    params.detailVerts = dMesh.verts;
                    params.detailVertsCount = dMesh.nverts;
                    params.detailTris = dMesh.tris;
                    params.detailTriCount = dMesh.ntris;
                    params.walkableHeight = cfg.walkableHeight;
                    params.walkableRadius = cfg.walkableRadius;
                    params.walkableClimb = cfg.walkableClimb;
                    params.bmin = pMesh.bmin;
                    params.bmax = pMesh.bmax;
                    params.cs = cfg.cs;
                    params.ch = cfg.ch;
                    params.buildBvTree = true;                    */
                    
                    params.offMeshConVerts = new float[0];                    
                    /**params.offMeshConVerts[0] = 0.1f;
                    params.offMeshConVerts[1] = 0.2f;
                    params.offMeshConVerts[2] = 0.3f;
                    params.offMeshConVerts[3] = 0.4f;
                    params.offMeshConVerts[4] = 0.5f;
                    params.offMeshConVerts[5] = 0.6f;
                    params.offMeshConRad = new float[0];
                    //params.offMeshConRad[0] = 0.1f;
                    params.offMeshConDir = new int[0];
                    //params.offMeshConDir[0] = 1;
                    params.offMeshConAreas = new int[0];
                    //params.offMeshConAreas[0] = 2;
                    params.offMeshConFlags = new int[0];
                    //params.offMeshConFlags[0] = 12;
                    params.offMeshConUserID = new int[0];
                    //params.offMeshConUserID[0] = 0x4567;
                    params.offMeshConCount = 0;
                    params.tileX = 12;
                    params.tileY = 23;

                    logger.info("Building Nav Mesh MeshData.");
                    meshData = NavMeshBuilder.createNavMeshData(params);   
                    logger.info("Building Done.");                    
                    
                    FileOutputStream fos = new FileOutputStream("D:\\Downloads\\Test\\all_tiles_navmesh.bin");
                    MeshSetWriter writer = new MeshSetWriter();                               
                    logger.info("Creating Nav Mesh.");
                    NavMesh navMesh = new NavMesh(meshData, cfg.maxVertsPerPoly, 0);                                         
                    logger.info("Saving Nav Mesh.");
                    writer.write(fos, navMesh, ByteOrder.LITTLE_ENDIAN, true);
                    logger.info("NavMesh saved.");
                    fos.close();                    
                    NavMeshQuery query = new NavMeshQuery(navMesh);
                    QueryFilter filter = new QueryFilter();
                    FindRandomPointResult frpr = query.findRandomPoint(filter, new NavMeshQuery.FRand());
                    for (int i = 0; i < 1000; i++) {
                        frpr = query.findRandomPoint(filter, new NavMeshQuery.FRand());
                    }
                    //System.out.println(Arrays.toString(frpr.getRandomPt()));                    
                    //extractModel(m2Example, true);
                    //extractWdt(azeroth);
                    //extractWdt(kalimdor);
                    //extractWdt("world\\maps\\uldaman\\uldaman.wdt");
                    //extractAllWdt();
                    //extractAllTerrains();
                    System.out.println("done");*/
                } catch (Exception e) {
                    logger.error("Exception " + e.getMessage());
                    e.printStackTrace();
                }                
                
                primaryStage.close();
                Platform.exit();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    private static void extractAllWMO(boolean addModels, boolean saveToFile) {
        int total = manager.getListWMO().size();
        int done = 0;
        for (String path : manager.getListWMO()) {
            done++;
            logger.info("Extracting WMO... " + path);
            extractWmo(path, addModels, saveToFile);
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractWmo(String path, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "WMO\\" + FilenameUtils.removeExtension(path) + ".obj";
            wmo.init(manager, path);
            wmo.setAddModels(addModels);
            wmo.setModelCache(cache);
            if (saveToFile) {
                if (wmo.save3D(outputFile, FileType3D.OBJ, Render3DType.MODEL, true, false)) {
                    logger.info("WMO file saved succesfully !");
                } else {
                    logger.error("WMO file not saved !");
                }
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllModels(boolean saveToFile) {
        int total = manager.getListM2().size();
        int done = 0;
        for (String path : manager.getListM2()) {
            done++;
            logger.info("Extracting M2... " + path);
            extractModel(path, saveToFile);
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractModel(String path, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Models\\" + FilenameUtils.removeExtension(path) + ".obj";
            M2 model = new M2();
            model.init(manager, path);
            // Adding to cache.
            model.render3D(Render3DType.MODEL, null);
            cache.put(path, model);
            if (saveToFile) {
                if (model.save3D(outputFile, FileType3D.OBJ, Render3DType.MODEL, true, false)) {
                    logger.info("Model file saved succesfully !");
                } else {
                    logger.error("Model file couldn't be saved.");
                }
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllTerrains() {

        int total = manager.getListWDT().size();
        int done = 0;
        for (String wdtFile : manager.getListWDT()) {
            done++;
            try {
                logger.info("Extracting WDT... " + wdtFile);
                String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
                String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile);
                wdt.init(manager, wdtFile, true, idxX, idxY);
                for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                    for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                        if (wdt.hasTerrain(x, y)) {
                            extractMap(base + "_" + y + "_" + x + ".adt", true, true, true, true);
                        }
                    }
                }
                //wdt.save2D(outputFile + "_terrain.png", FileType2D.PNG, Render2DType.RENDER_TILEMAP_TERRAIN, 1920, 1080);                
                //wdt.save2D(outputFile + "_liquid_animated.png", FileType2D.PNG, Render2DType.RENDER_TILEMAP_LIQUID_ANIMATED);
            } catch (IOException | FileReaderException | MPQException ex) {
                logger.error(ex.getMessage());
            }
            logger.info("Done: " + done + " / Total: " + total);
        }
    }

    private static void extractAllMaps(boolean yUp, boolean addWMO, boolean addModels, boolean saveToFile) {
        for (String path : manager.getListADT()) {
            extractMap(path, yUp, addWMO, addModels, saveToFile);
        }

    }

    private static void extractMap(String path, boolean yUp, boolean addWMO, boolean addModels, boolean saveToFile) {
        try {
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(path) + ".obj";
            adt.init(manager, path, idxX, idxY);
            adt.setyUp(yUp);
            adt.setAddWMO(addWMO);
            adt.setAddModels(addModels);
            adt.setModelCache(cache);
            if (saveToFile) {
                if (adt.save3D(outputFile, FileType3D.OBJ, Render3DType.COLLISION_TERRAIN, false, false)) {
                    logger.info("Terrain file saved succesfully !");
                } else {
                    logger.error("Terrain file couldn't be saved.");
                }
            }
        } catch (IOException | FileReaderException | MPQException | ModelRendererException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static void extractAllWdt() {
        for (String path : manager.getListWDT()) {
            extractWdt(path);
        }

    }

    private static void extractWdt(String wdtFile) {

        try {
            logger.info("Extracting WDT... " + wdtFile);
            String base = FilenameUtils.getPath(wdtFile) + FilenameUtils.getBaseName(wdtFile);
            String outputFile = ROOT + "Maps\\" + FilenameUtils.removeExtension(wdtFile);
            wdt.init(manager, wdtFile, true, idxX, idxY);
            logger.debug("WDT file initialized.");
            for (int x = 0; x < WDT.MAP_TILE_SIZE; x++) {
                for (int y = 0; y < WDT.MAP_TILE_SIZE; y++) {
                    if (wdt.hasTerrain(x, y)) {
                        extractMap(base + "_" + y + "_" + x + ".adt", true, true, true, true);
                    }
                }
            }
            logger.debug("Rendering & saving WDT file.");
            /**
             * wdt.save2D(outputFile + "_terrain.png", FileType2D.PNG,
             * Render2DType.RENDER_TILEMAP_TERRAIN); wdt.save2D(outputFile +
             * "_liquid_animated.png", FileType2D.PNG,
             * Render2DType.RENDER_TILEMAP_LIQUID_ANIMATED);
             * wdt.save2D(outputFile + "_liquid_fishable.png", FileType2D.PNG,
             * Render2DType.RENDER_TILEMAP_LIQUID_FISHABLE);
             * wdt.save2D(outputFile + "_liquid_type.png", FileType2D.PNG,
             * Render2DType.RENDER_TILEMAP_LIQUID_TYPE);
             */
        } catch (IOException | FileReaderException | MPQException ex) {
            logger.error(ex.getMessage());
        }
    }
}
