/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import eu.jangos.extractor.file.adt.chunk.MCIN;
import eu.jangos.extractor.file.adt.chunk.MCNK;
import eu.jangos.extractor.file.adt.chunk.MDDF;
import eu.jangos.extractor.file.adt.chunk.MODF;
import eu.jangos.extractor.file.adt.chunk.Vector;
import eu.jangos.extractor.file.exception.ADTException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class ADTFileReader {

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
    private static final String HEADER_MFBO = "MFBO";
    private static final String HEADER_MH2O = "MH2O";
    private static final String HEADER_MTFX = "MTFX";
    
    // Size as from which the offset calculation is made.
    private static final int GLOBAL_OFFSET = 0x14;
    
    public static final float TILE_SIZE = 533.33333f;
    public static final float CHUNK_SIZE = TILE_SIZE / 16.0f;
    public static final float UNIT_SIZE = CHUNK_SIZE / 8.0f;
    public static final float ZERO_POINT = 32.0f * TILE_SIZE;

    private ByteBuffer data;
    // Indicates whether this file has been initialized or not.
    private boolean init = false;
    
    private String filename;
    
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

    public ADTFileReader() {
    }

    public void init(byte[] data, String filename) throws IOException, ADTException {
        init = false;
        this.filename = filename;
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
     * @throws ADTException
     */
    private void readVersion(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];
        
        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_VERSION)) {
            throw new ADTException("Expected header " + HEADER_VERSION + ", received header: " + sb.toString());
        }

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
     * @throws ADTException
     */
    private void readHeader(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];               
        
        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MHDR)) {
            throw new ADTException("Expected header " + HEADER_MHDR + ", received header: " + sb.toString());
        }

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

    private MCIN[] readMCIN() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];
        
        this.data.position(GLOBAL_OFFSET + this.offsetMCIN);                
        
        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MCIN)) {
            throw new ADTException("Expected header " + HEADER_MCIN + ", received header: " + sb.toString());
        }

        int size = this.data.getInt();        
        MCIN index;
        MCIN[] chunkIndex = new MCIN[size/MCIN.getOBJECT_SIZE()];
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

    public List<String> getTextures() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        return readStringChunk(this.offsetMTEX, HEADER_MTEX);        
    }

    public List<String> getModels() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        return readStringChunk(this.offsetMMDX, HEADER_MMDX);
    }

    private List<Integer> getModelOffsets() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        return readIntegerChunk(this.offsetMMID, HEADER_MMID);
    }

    public List<String> getWorldObjects() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        return readStringChunk(this.offsetMWMO, HEADER_MWMO);        
    }

    private List<Integer> getWorldObjectsOffsets() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        return readIntegerChunk(this.offsetMWID, HEADER_MWID);
    }

    public List<MDDF> getDoodadPlacement() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        List<MDDF> listPlacement = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.position(this.offsetMDDF + GLOBAL_OFFSET);
        
        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MDDF)) {
            throw new ADTException("Expected header " + HEADER_MDDF + ", received header: " + sb.toString());
        }

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

    public List<MODF> getWorldObjectsPlacement() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        List<MODF> listPlacement = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.position(this.offsetMODF + GLOBAL_OFFSET);
        
        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MODF)) {
            throw new ADTException("Expected header " + HEADER_MODF + ", received header: " + sb.toString());
        }

        int size = this.data.getInt();
        int start = this.data.position();
        MODF placement;
        while (this.data.position() - start < size) {
            placement = new MODF();

            placement.setMwidEntry(this.data.getInt());
            placement.setUniqueId(this.data.getInt());
            placement.setX(this.data.getFloat());
            placement.setY(this.data.getFloat());
            placement.setZ(this.data.getFloat());
            placement.setOrX(this.data.getFloat());
            placement.setOrY(this.data.getFloat());
            placement.setOrZ(this.data.getFloat());
            placement.setLowerBoundX(this.data.getFloat());
            placement.setLowerBoundY(this.data.getFloat());
            placement.setLowerBoundZ(this.data.getFloat());
            placement.setUpperBoundX(this.data.getFloat());
            placement.setUpperBoundX(this.data.getFloat());
            placement.setUpperBoundX(this.data.getFloat());
            placement.setFlags(this.data.getShort());
            placement.setDoodadSet(this.data.getShort());
            placement.setNameSet(this.data.getShort());
            placement.setPadding(this.data.getShort());

            listPlacement.add(placement);
        }
        
        return listPlacement;
    }

    public List<MCNK> getMapChunks() throws ADTException {
        if(!init) {
            throw new ADTException("ADT file has not been initialized, please use init(data) function to initialize your ADT file !");
        }
        
        List<MCNK> listMapChunks = new ArrayList<>();
        
        StringBuilder sb;
        byte[] header = new byte[4];
        MCIN[] chunks = readMCIN();
                        
        int size;
        int start;        
        
        MCNK chunk;
        for (int i = 0; i < chunks.length; i++) {            
            this.data.position(chunks[i].getOffsetMCNK());
            sb = new StringBuilder();
            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCNK)) {
                throw new ADTException("Expected header " + HEADER_MCNK + ", received header: " + sb.toString());
            }

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
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCVT)) {
                throw new ADTException("Expected header " + HEADER_MCVT + ", received header: " + sb.toString());
            }

            // We ignore size.
            this.data.getInt();

            for (int j = 0; j < 145; j++) {
                chunk.getVertices().getPoints()[j] = this.data.getFloat();
            }

            // Must now parse MCNR
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCNR)) {
                throw new ADTException("Expected header " + HEADER_MCNR + ", received header: " + sb.toString());
            }

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
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCLY)) {
                throw new ADTException("Expected header " + HEADER_MCLY + ", received header: " + sb.toString());
            }

            // We ignore size.
            this.data.getInt();
            for (int j = 0; j < chunk.getNbLayers(); j++) {
                chunk.getTextureLayers()[j].setTextureId(this.data.getInt());
                chunk.getTextureLayers()[j].setFlags(this.data.getInt());
                chunk.getTextureLayers()[j].setOffsetinMCAL(this.data.getInt());
                chunk.getTextureLayers()[j].setEffectId(this.data.getInt());
            }

            // Must now parse MCRF.
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCRF)) {
                throw new ADTException("Expected header " + HEADER_MCRF + ", received header: " + sb.toString());
            }

            size = this.data.getInt();
            start = this.data.position();
            while (this.data.position() - start < size) {
                chunk.getMcrfList().add(this.data.getInt());
            }

            // Must now parse MCSH.
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCSH)) {
                throw new ADTException("Expected header " + HEADER_MCSH + ", received header: " + sb.toString());
            }

            size = this.data.getInt();
            for (int j = 0; j < size; j++) {
                this.data.get();
            }

            // Must now parse MCAL.
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCAL)) {
                throw new ADTException("Expected header " + HEADER_MCAL + ", received header: " + sb.toString());
            }

            size = this.data.getInt();
            for (int j = 0; j < size; j++) {
                this.data.get();
            }

            // Must now parse MCLQ.
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCLQ)) {
                throw new ADTException("Expected header " + HEADER_MCLQ + ", received header: " + sb.toString());
            }

            size = chunk.getSizeLiquid() - 4;
            for (int j = 0; j < size; j++) {
                this.data.get();
            }

            // Must now parse MCSE.
            sb = new StringBuilder();
            header = new byte[4];

            this.data.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCSE)) {
                throw new ADTException("Expected header " + HEADER_MCSE + ", received header: " + sb.toString());
            }
            
            // Flag value not well documented.
            this.data.getInt();
            
            listMapChunks.add(chunk);
        }
        
        return listMapChunks;
    }

    private List<String> readStringChunk(int offset, String expectedHeader) throws ADTException {
        List<String> stringList = new ArrayList<>();
                        
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.position(offset + GLOBAL_OFFSET);
        
        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new ADTException("Expected header " + expectedHeader + ", received header: " + sb.toString());
        }

        int size = this.data.getInt();
        int start = this.data.position();
        while (this.data.position() - start < size) {
            stringList.add(readString(this.data));
        }
        
        return stringList;
    }

    private List<Integer> readIntegerChunk(int offset, String expectedHeader) throws ADTException {
        List<Integer> intList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.position(offset + GLOBAL_OFFSET);
        
        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new ADTException("Expected header " + expectedHeader + ", received header: " + sb.toString());
        }

        int size = this.data.getInt();
        int start = this.data.position();
        while (this.data.position() - start < size) {
            intList.add(this.data.getInt());
        }
        
        return intList;
    }
    
    private String readString(ByteBuffer in) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
}
