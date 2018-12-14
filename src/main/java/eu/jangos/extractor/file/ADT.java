/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import java.awt.Color;
import eu.jangos.extractor.file.adt.chunk.MCIN;
import eu.jangos.extractor.file.adt.chunk.MCLQ;
import static eu.jangos.extractor.file.adt.chunk.MCLQ.LIQUID_FLAG_LENGTH;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.adt.chunk.Vector;
import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Warkdev
 */
public class ADT extends FileReader {

    private static final String HEADER_VERSION = "MVER";
    private static final String HEADER_MHDR = "MHDR";
    private static final String HEADER_MCIN = "MCIN";
    private static final String HEADER_MTEX = "MTEX";
    private static final String HEADER_MMDX = "MMDX";
    private static final String HEADER_MMID = "MMID";
    private static final String HEADER_MWMO = "MWMO";
    private static final String HEADER_MWID = "MWID";
    private static final String HEADER_MDDF = "MDDF";
    private static final String HEADER_MODF = "MODF";
    private static final String HEADER_MCNK = "MCNK";
    private static final String HEADER_MCVT = "MCVT";
    private static final String HEADER_MCNR = "MCNR";
    private static final String HEADER_MCLY = "MCLY";
    private static final String HEADER_MCRF = "MCRF";
    private static final String HEADER_MCSH = "MCSH";
    private static final String HEADER_MCAL = "MCAL";
    private static final String HEADER_MCLQ = "MCLQ";
    private static final String HEADER_MCSE = "MCSE";

    // Size as from which the offset calculation is made.
    private static final int GLOBAL_OFFSET = 0x14;

    public static final float TILE_SIZE = 533.33333f;
    public static final float CHUNK_SIZE = TILE_SIZE / 16.0f;
    public static final float UNIT_SIZE = CHUNK_SIZE / 8.0f;
    public static final float ZERO_POINT = 32.0f * TILE_SIZE;

    private static final int SIZE_LIQUID_MAP = 128;
    
    private ByteBuffer data;
    // Indicates whether this file has been initialized or not.
    private boolean init = false;

    private String fileName;

    private int version;
    private int mfboEnum;
    private int headerFlags;
    private int offsetMCIN;
    private int offsetMTEX;
    private int offsetMMDX;
    private int offsetMMID;
    private int offsetMWMO;
    private int offsetMWID;
    private int offsetMDDF;
    private int offsetMODF;
    private int offsetMFBO;
    private int offsetMH2O;
    private int offsetMTFX;

    @Override
    public void init(byte[] data, String filename) throws IOException, FileReaderException {
        init = false;
        this.fileName = filename;
        this.data = ByteBuffer.wrap(data);
        this.data.order(ByteOrder.LITTLE_ENDIAN);    
        
        // This is all what we need to read our file. Initialize the offset and check the version.
        readVersion(this.data);
        readHeader(this.data);
        init = true;
    }

    /**
     * Reading the ADT version. The ADT version is expected to be located at the
     * beginning of the ADT File under this form: 4 character indicating the
     * MVER chunk in reverse order. 4 bytes indicating the length of the MVER
     * chunk. 4 bytes indicating the version of the ADT File (18 in 1.12.x).
     * e.g.: REVM 04 00 00 00 12 00 00 00
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readVersion(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_VERSION);

        // We skip the size as we know it's 4.
        in.getInt();
        this.version = in.getInt();
    }

    /**
     * Reading the MHDR chunk of the ADT file. The MHDR chunk is expected to be
     * right after the MVER chunk. It contains offset towards the various chunks
     * hold in this ADT. e.g. : RDHM
     *
     * @param in
     * @throws IOException
     * @throws FileReaderException
     */
    private void readHeader(ByteBuffer in) throws FileReaderException {
        checkHeader(HEADER_MHDR);

        this.mfboEnum = in.getInt();
        this.headerFlags = in.getInt();
        this.offsetMCIN = in.getInt();
        this.offsetMTEX = in.getInt();
        this.offsetMMDX = in.getInt();
        this.offsetMMID = in.getInt();
        this.offsetMWMO = in.getInt();
        this.offsetMWID = in.getInt();
        this.offsetMDDF = in.getInt();
        this.offsetMODF = in.getInt();
        this.offsetMFBO = in.getInt();
        this.offsetMH2O = in.getInt();
        this.offsetMTFX = in.getInt();
    }

    private MCIN[] readMCIN() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }        

        this.data.position(GLOBAL_OFFSET + this.offsetMCIN);

        checkHeader(HEADER_MCIN);

        int size = this.data.getInt();
        MCIN index;
        MCIN[] chunkIndex = new MCIN[size / MCIN.getOBJECT_SIZE()];
        for (int i = 0; i < chunkIndex.length; i++) {
            index = new MCIN();
            index.setOffsetMCNK(this.data.getInt());
            index.setSize(this.data.getInt());
            index.setFlags(this.data.getInt());
            index.setAsyncId(this.data.getInt());
            chunkIndex[i] = index;
        }

        return chunkIndex;
    }

    public List<String> getTextures() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMTEX + GLOBAL_OFFSET, HEADER_MTEX);
    }

    public List<String> getModels() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMMDX + GLOBAL_OFFSET, HEADER_MMDX);
    }

    private List<Integer> getModelOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMMID + GLOBAL_OFFSET, HEADER_MMID);
    }

    public List<String> getWorldObjects() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readStringChunk(this.offsetMWMO + GLOBAL_OFFSET, HEADER_MWMO);
    }

    private List<Integer> getWorldObjectsOffsets() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        return readIntegerChunk(this.offsetMWID + GLOBAL_OFFSET, HEADER_MWID);
    }

    public List<MDDF> getDoodadPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MDDF> listPlacement = new ArrayList<>();

        this.data.position(this.offsetMDDF + GLOBAL_OFFSET);

        checkHeader(HEADER_MDDF);

        int size = this.data.getInt();
        int start = this.data.position();
        MDDF placement;
        while (this.data.position() - start < size) {
            placement = new MDDF();

            placement.setMmidEntry(this.data.getInt());
            placement.setUniqueId(this.data.getInt());
            placement.setX(this.data.getFloat());
            placement.setY(this.data.getFloat());
            placement.setZ(this.data.getFloat());
            placement.setOrX(this.data.getFloat());
            placement.setOrY(this.data.getFloat());
            placement.setOrZ(this.data.getFloat());
            placement.setScale(this.data.getShort());
            placement.setFlags(this.data.getShort());

            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MODF> getWorldObjectsPlacement() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MODF> listPlacement = new ArrayList<>();        
        this.data.position(this.offsetMODF + GLOBAL_OFFSET);

        checkHeader(HEADER_MODF);

        int size = this.data.getInt();
        int start = this.data.position();
        MODF placement;
        while (this.data.position() - start < size) {
            placement = new MODF();
            placement.read(this.data);            
            listPlacement.add(placement);
        }

        return listPlacement;
    }

    public List<MCNK> getMapChunks() throws FileReaderException {
        if (!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }

        List<MCNK> listMapChunks = new ArrayList<>();
        
        MCIN[] chunks = readMCIN();

        int size;
        int start;

        MCNK chunk;
        for (int i = 0; i < chunks.length; i++) {
            this.data.position(chunks[i].getOffsetMCNK());
            
            checkHeader(HEADER_MCNK);

            // We ignore size.
            this.data.getInt();

            chunk = new MCNK();

            chunk.setFlags(this.data.getInt());
            chunk.setIndexX(this.data.getInt());
            chunk.setIndexY(this.data.getInt());
            chunk.setNbLayers(this.data.getInt());
            chunk.setnDoodadRefs(this.data.getInt());
            chunk.setOffsetMCVT(this.data.getInt());
            chunk.setOffsetMCNR(this.data.getInt());
            chunk.setOffsetMCLY(this.data.getInt());
            chunk.setOffsetMCRF(this.data.getInt());
            chunk.setOffsetMCAL(this.data.getInt());
            chunk.setSizeAlpha(this.data.getInt());
            chunk.setOffsetMCSH(this.data.getInt());
            chunk.setSizeShadow(this.data.getInt());
            chunk.setAreadId(this.data.getInt());
            chunk.setnMapObjRefs(this.data.getInt());
            chunk.setHoles(this.data.getInt());
            for (int j = 0; j < 16; j++) {
                // Skipping low quality text map for now. (64 bytes)
                this.data.get();
            }
            chunk.setPredTex(this.data.getInt());
            chunk.setNoEffectDoodad(this.data.getInt());
            chunk.setOffsetMCSE(this.data.getInt());
            chunk.setnSndEmitters(this.data.getInt());
            chunk.setOffsetMCLQ(this.data.getInt());
            chunk.setSizeLiquid(this.data.getInt());
            chunk.setPosX(this.data.getFloat());
            chunk.setPosY(this.data.getFloat());
            chunk.setPosZ(this.data.getFloat());
            chunk.setOffsetMCCV(this.data.getInt());
            chunk.setOffsetMCLV(this.data.getInt());
            // Unused
            this.data.getInt();

            // Must now parse MCVT            
            checkHeader(HEADER_MCVT);

            // We ignore size.
            this.data.getInt();

            for (int j = 0; j < 145; j++) {
                chunk.getVertices().getPoints()[j] = this.data.getFloat();
            }

            // Must now parse MCNR
            checkHeader(HEADER_MCNR);

            // We ignore size.
            this.data.getInt();
            for (int j = 0; j < 145; j++) {
                chunk.getNormals().getPoints()[j] = new Vector((int) this.data.get(), (int) this.data.get(), (int) this.data.get());
            }

            // 13 unknown bytes at the end of normals:
            for (int j = 0; j < 13; j++) {
                this.data.get();
            }

            // Must now parse MCLY.
            checkHeader(HEADER_MCLY);

            // We ignore size.
            this.data.getInt();
            for (int j = 0; j < chunk.getNbLayers(); j++) {
                chunk.getTextureLayers()[j].setTextureId(this.data.getInt());
                chunk.getTextureLayers()[j].setFlags(this.data.getInt());
                chunk.getTextureLayers()[j].setOffsetinMCAL(this.data.getInt());
                chunk.getTextureLayers()[j].setEffectId(this.data.getInt());
            }

            // Must now parse MCRF.
            checkHeader(HEADER_MCRF);

            size = this.data.getInt();
            start = this.data.position();
            while (this.data.position() - start < size) {
                chunk.getMcrfList().add(this.data.getInt());
            }

            // Must now parse MCSH.
            checkHeader(HEADER_MCSH);

            size = this.data.getInt();
            for (int j = 0; j < size; j++) {
                this.data.get();
            }

            // Must now parse MCAL.
            checkHeader(HEADER_MCAL);

            size = this.data.getInt();
            for (int j = 0; j < size; j++) {
                this.data.get();
            }

            // Must now parse MCLQ.
            checkHeader(HEADER_MCLQ);

            size = chunk.getSizeLiquid() - 8;
            // Then we skip the "size field" as it's always 0.
            data.getInt();
            // Documentation is spread over several codebase, none really figuring out what it is properly.
            // Thanks for Mangos/CMangos codebase on which this is based.            
            if (size > 0) {                
                MCLQ liquid = new MCLQ();
                liquid.read(data);
                chunk.setLiquids(liquid);
            }

            // Must now parse MCSE.
            checkHeader(HEADER_MCSE);

            // Flag value not well documented.
            this.data.getInt();

            listMapChunks.add(chunk);
        }

        return listMapChunks;
    }    

    private String[][] getLiquidMap(boolean displayLiquidType) throws FileReaderException {
        List<MCNK> mapChunks = getMapChunks();

        String[][] liquids = new String[SIZE_LIQUID_MAP][SIZE_LIQUID_MAP];
        int idx = 0;
        int idy = 0;
        for (MCNK chunk : mapChunks) {
            MCLQ liquid = chunk.getLiquids();
            if (liquid == null) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                    }
                }
            } else {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        if (liquid.hasNoLiquid(i, j)) {
                            liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "N ";
                        } else if (liquid.hasLiquid(i, j)) {
                            if (liquid.isDark(i, j)) {
                                if (displayLiquidType) {
                                    String letter = "D";
                                    if (chunk.isRiver()) {
                                        letter += "R ";
                                    } else if (chunk.isOcean()) {
                                        letter += "O ";
                                    } else if (chunk.isMagma()) {
                                        letter += "M ";
                                    } else if (chunk.isSlime()) {
                                        letter += "S ";
                                    }
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = letter;
                                } else {
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "D ";
                                }
                            } else {
                                if (displayLiquidType) {
                                    String letter = "L";
                                    if (chunk.isRiver()) {
                                        letter += "R ";
                                    } else if (chunk.isOcean()) {
                                        letter += "O ";
                                    } else if (chunk.isMagma()) {
                                        letter += "M ";
                                    } else if (chunk.isSlime()) {
                                        letter += "S ";
                                    }
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = letter;
                                } else {
                                    liquids[LIQUID_FLAG_LENGTH * idx + i][LIQUID_FLAG_LENGTH * idy + j] = "L ";
                                }
                            }
                        }
                    }
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }

        return liquids;
    }

    public void saveLiquidMap(String pngPath, boolean displayLiquidType) throws FileReaderException, IOException {
        BufferedImage img = new BufferedImage(SIZE_LIQUID_MAP, SIZE_LIQUID_MAP, BufferedImage.TYPE_INT_RGB);
        int alpha = 100;
        int idx = 0;
        int idy = 0;
        List<MCNK> mapChunks = getMapChunks();
        for (MCNK chunk : mapChunks) {
            MCLQ liquid = chunk.getLiquids();
            if (liquid == null) {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < LIQUID_FLAG_LENGTH; i++) {
                    for (int j = 0; j < LIQUID_FLAG_LENGTH; j++) {
                        if (liquid.hasNoLiquid(i, j)) {
                            img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, Color.BLACK.getRGB());
                        } else if (liquid.hasLiquid(i, j)) {
                            if (liquid.isDark(i, j)) {
                                if (displayLiquidType) {                                    
                                    if (chunk.isRiver()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 112, 192, alpha).getRGB());
                                    } else if (chunk.isOcean()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 32, 96, alpha).getRGB());                                        
                                    } else if (chunk.isMagma()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(255, 192, 0, alpha).getRGB());
                                    } else if (chunk.isSlime()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(55, 86, 35, alpha).getRGB());
                                    }                                    
                                } else {
                                    img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 112, 192, alpha).getRGB());
                                }
                            } else {
                                if (displayLiquidType) {                                    
                                    if (chunk.isRiver()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                    } else if (chunk.isOcean()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(112, 48, 160, alpha).getRGB());
                                    } else if (chunk.isMagma()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(244, 176, 132, alpha).getRGB());
                                    } else if (chunk.isSlime()) {
                                        img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(169, 208, 142, alpha).getRGB());
                                    }                                    
                                } else {
                                    img.setRGB(LIQUID_FLAG_LENGTH * idx + i, LIQUID_FLAG_LENGTH * idy + j, new Color(0, 176, 240, alpha).getRGB());
                                }
                            }
                        }
                    }
                }
            }

            idx++;
            if (idx % 16 == 0) {
                idx = 0;
                idy++;
            }
        }
        
        File imgFile = new File(pngPath);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            imgFile.getParentFile().mkdirs();
        }
                
        ImageIO.write(img, "PNG", imgFile);
    }
    
    public void printLiquidMap(boolean displayLiquidType) throws FileReaderException {
        String[][] liquids = getLiquidMap(displayLiquidType);

        for (int i = 0; i < liquids.length; i++) {
            for (int j = 0; j < liquids[i].length; j++) {
                System.out.print(liquids[j][i]);
            }
            System.out.println();
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFilename() {
        return fileName;
    }

    public void setFilename(String filename) {
        this.fileName = filename;
    }

}
