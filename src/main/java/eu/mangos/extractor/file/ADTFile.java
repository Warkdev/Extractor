/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor.file;

import eu.mangos.extractor.file.chunk.MCIN;
import eu.mangos.extractor.file.chunk.MCNK;
import eu.mangos.extractor.file.chunk.MDDF;
import eu.mangos.extractor.file.chunk.MODF;
import eu.mangos.extractor.file.chunk.Vector;
import eu.mangos.extractor.file.exception.ADTException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class ADTFile {

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

    private static final float TILE_SIZE = 533.33333f;
    private static final float CHUNK_SIZE = TILE_SIZE / 16.0f;
    private static final float UNIT_SIZE = CHUNK_SIZE / 8.0f;
    private static final float ZERO_POINT = 32.0f * TILE_SIZE;

    private byte[] data;

    private int version;
    private int offsetMCIN;
    private MCIN[] chunkIndex;
    private int offsetMTEX;
    private List<String> listTextures;
    private int offsetMMDX;
    private List<String> listDoodad;
    private int offsetMMID;
    private List<Integer> listDoodadLookup;
    private int offsetMWMO;
    private List<String> listWMOFiles;
    private int offsetMWID;
    private List<Integer> listWMOLookup;
    private int offsetMDDF;
    private List<MDDF> listMDDF;
    private int offsetMODF;
    private List<MODF> listMODF;
    private List<MCNK> listMapChunks;
    private int offsetMFBO;
    private int offsetMH2O;
    private int offsetMTFX;

    public ADTFile() {
    }

    public void init(byte[] data) throws IOException, ADTException {
        this.data = data;
        this.chunkIndex = new MCIN[256];
        this.listTextures = new ArrayList<>();
        this.listDoodad = new ArrayList<>();
        this.listDoodadLookup = new ArrayList<>();
        this.listWMOFiles = new ArrayList<>();
        this.listWMOLookup = new ArrayList<>();
        this.listMDDF = new ArrayList<>();
        this.listMODF = new ArrayList<>();
        this.listMapChunks = new ArrayList<>();
        readFile();
    }

    private void readFile() throws IOException, ADTException {
        ByteBuffer in = ByteBuffer.wrap(data);
        in.order(ByteOrder.LITTLE_ENDIAN);
        readVersion(in);
        readMHDR(in);
        readMCIN(in);
        readMTEX(in);
        readMMDX(in);
        readMMID(in);
        readMWMO(in);
        readMWID(in);
        readMDDF(in);
        readMODF(in);
        readMCNK(in);
        readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
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
    private void readMHDR(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];
        int size = 0;

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MHDR)) {
            throw new ADTException("Expected header " + HEADER_MHDR + ", received header: " + sb.toString());
        }

        size = in.getInt();
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
        // Size of MHDR chunk + size of MVER chunk is 72+12. That's the start of MCIN chunk.
        in.position(84);
    }

    private void readMCIN(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MCIN)) {
            throw new ADTException("Expected header " + HEADER_MCIN + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        MCIN index;
        for (int i = 0; i < this.chunkIndex.length; i++) {
            index = new MCIN();
            index.setOffsetMCNK(in.getInt());
            index.setSize(in.getInt());
            index.setFlags(in.getInt());
            index.setAsyncId(in.getInt());
            chunkIndex[i] = index;
        }

    }

    private void readMTEX(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MTEX)) {
            throw new ADTException("Expected header " + HEADER_MTEX + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        while (in.position() - start < size) {
            this.listTextures.add(readString(in));
        }
    }

    private void readMMDX(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MMDX)) {
            throw new ADTException("Expected header " + HEADER_MMDX + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        while (in.position() - start < size) {
            this.listDoodad.add(readString(in));
        }
    }

    private void readMMID(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MMID)) {
            throw new ADTException("Expected header " + HEADER_MMID + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        while (in.position() - start < size) {
            this.listDoodadLookup.add(in.getInt());
        }
    }

    private void readMWMO(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MWMO)) {
            throw new ADTException("Expected header " + HEADER_MWMO + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        while (in.position() - start < size) {
            this.listWMOFiles.add(readString(in));
        }
    }

    private void readMWID(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MWID)) {
            throw new ADTException("Expected header " + HEADER_MWID + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        while (in.position() - start < size) {
            this.listWMOLookup.add(in.getInt());
        }
    }

    private void readMDDF(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MDDF)) {
            throw new ADTException("Expected header " + HEADER_MDDF + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        MDDF placement;
        while (in.position() - start < size) {
            placement = new MDDF();

            placement.setMmidEntry(in.getInt());
            placement.setUniqueId(in.getInt());
            placement.setX(in.getFloat());
            placement.setY(in.getFloat());
            placement.setZ(in.getFloat());
            placement.setOrX(in.getFloat());
            placement.setOrY(in.getFloat());
            placement.setOrZ(in.getFloat());
            placement.setScale(in.getShort());
            placement.setFlags(in.getShort());

            this.listMDDF.add(placement);
        }
    }

    private void readMODF(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(HEADER_MODF)) {
            throw new ADTException("Expected header " + HEADER_MODF + ", received header: " + sb.toString());
        }

        int size = in.getInt();
        int start = in.position();
        MODF placement;
        while (in.position() - start < size) {
            placement = new MODF();

            placement.setMwidEntry(in.getInt());
            placement.setUniqueId(in.getInt());
            placement.setX(in.getFloat());
            placement.setY(in.getFloat());
            placement.setZ(in.getFloat());
            placement.setOrX(in.getFloat());
            placement.setOrY(in.getFloat());
            placement.setOrZ(in.getFloat());
            placement.setLowerBoundX(in.getFloat());
            placement.setLowerBoundY(in.getFloat());
            placement.setLowerBoundZ(in.getFloat());
            placement.setUpperBoundX(in.getFloat());
            placement.setUpperBoundX(in.getFloat());
            placement.setUpperBoundX(in.getFloat());
            placement.setFlags(in.getShort());
            placement.setDoodadSet(in.getShort());
            placement.setNameSet(in.getShort());
            placement.setPadding(in.getShort());

            this.listMODF.add(placement);
        }
    }

    private void readMCNK(ByteBuffer in) throws ADTException {
        StringBuilder sb;
        byte[] header = new byte[4];

        int size;
        int start;

        MCNK chunk;
        for (int i = 0; i < 256; i++) {            
            sb = new StringBuilder();
            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCNK)) {
                throw new ADTException("Expected header " + HEADER_MCNK + ", received header: " + sb.toString());
            }

            // We ignore size.
            in.getInt();

            chunk = new MCNK();

            chunk.setFlags(in.getInt());
            chunk.setIndexX(in.getInt());
            chunk.setIndexY(in.getInt());
            chunk.setNbLayers(in.getInt());
            chunk.setnDoodadRefs(in.getInt());
            chunk.setOffsetMCVT(in.getInt());
            chunk.setOffsetMCNR(in.getInt());
            chunk.setOffsetMCLY(in.getInt());
            chunk.setOffsetMCRF(in.getInt());
            chunk.setOffsetMCAL(in.getInt());
            chunk.setSizeAlpha(in.getInt());
            chunk.setOffsetMCSH(in.getInt());
            chunk.setSizeShadow(in.getInt());
            chunk.setAreadId(in.getInt());
            chunk.setnMapObjRefs(in.getInt());
            chunk.setHoles(in.getInt());
            for (int j = 0; j < 16; j++) {
                // Skipping low quality text map for now. (64 bytes)
                in.get();
            }
            chunk.setPredTex(in.getInt());
            chunk.setNoEffectDoodad(in.getInt());
            chunk.setOffsetMCSE(in.getInt());
            chunk.setnSndEmitters(in.getInt());
            chunk.setOffsetMCLQ(in.getInt());
            chunk.setSizeLiquid(in.getInt());
            chunk.setPosX(in.getFloat() * (-1.0f) + ZERO_POINT);
            chunk.setPosY(in.getFloat());
            chunk.setPosZ(in.getFloat() * (-1.0f) + ZERO_POINT);
            chunk.setOffsetMCCV(in.getInt());
            chunk.setOffsetMCLV(in.getInt());
            // Unused
            in.getInt();

            // Must now parse MCVT
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCVT)) {
                throw new ADTException("Expected header " + HEADER_MCVT + ", received header: " + sb.toString());
            }

            // We ignore size.
            in.getInt();

            for (int j = 0; j < 17; j++) {
                for (int k = 0; k < ((j % 2 != 0) ? 8 : 9); k++) {
                    Vector vector = new Vector();
                    float h, posX, posZ;
                    posX = j * UNIT_SIZE;
                    posZ = k * 0.5f * UNIT_SIZE;
                    h = in.getFloat();
                    if (k % 2 != 0) {
                        posX += UNIT_SIZE * 0.5f;
                    }
                    vector.setX(chunk.getPosX() + posX);
                    vector.setY(chunk.getPosY() + h);
                    vector.setZ(chunk.getPosZ() + posZ);
                    chunk.getVertices().getPoints()[j] = vector;
                }
            }

            // Must now parse MCNR
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCNR)) {
                throw new ADTException("Expected header " + HEADER_MCNR + ", received header: " + sb.toString());
            }

            // We ignore size.
            in.getInt();
            for (int j = 0; j < 145; j++) {
                chunk.getNormals().getPoints()[j] = new Vector((int) in.get(), (int) in.get(), (int) in.get());
            }

            // 13 unknown bytes at the end of normals:
            for (int j = 0; j < 13; j++) {
                in.get();
            }

            // Must now parse MCLY.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCLY)) {
                throw new ADTException("Expected header " + HEADER_MCLY + ", received header: " + sb.toString());
            }

            // We ignore size.
            in.getInt();
            for (int j = 0; j < chunk.getNbLayers(); j++) {
                chunk.getTextureLayers()[j].setTextureId(in.getInt());
                chunk.getTextureLayers()[j].setFlags(in.getInt());
                chunk.getTextureLayers()[j].setOffsetinMCAL(in.getInt());
                chunk.getTextureLayers()[j].setEffectId(in.getInt());
            }

            // Must now parse MCRF.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCRF)) {
                throw new ADTException("Expected header " + HEADER_MCRF + ", received header: " + sb.toString());
            }

            size = in.getInt();
            start = in.position();
            while (in.position() - start < size) {
                chunk.getMcrfList().add(in.getInt());
            }

            // Must now parse MCSH.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCSH)) {
                throw new ADTException("Expected header " + HEADER_MCSH + ", received header: " + sb.toString());
            }

            size = in.getInt();
            for (int j = 0; j < size; j++) {
                in.get();
            }

            // Must now parse MCAL.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCAL)) {
                throw new ADTException("Expected header " + HEADER_MCAL + ", received header: " + sb.toString());
            }

            size = in.getInt();
            for (int j = 0; j < size; j++) {
                in.get();
            }

            // Must now parse MCLQ.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCLQ)) {
                throw new ADTException("Expected header " + HEADER_MCLQ + ", received header: " + sb.toString());
            }

            size = chunk.getSizeLiquid() - 4;
            for (int j = 0; j < size; j++) {
                in.get();
            }

            // Must now parse MCSE.
            sb = new StringBuilder();
            header = new byte[4];

            in.get(header);

            sb = sb.append(new String(header)).reverse();
            if (!sb.toString().equals(HEADER_MCSE)) {
                throw new ADTException("Expected header " + HEADER_MCSE + ", received header: " + sb.toString());
            }
            
            // Flag value not well documented.
            in.getInt();
            
            this.listMapChunks.add(chunk);
        }
    }

    private void readNextHeader(ByteBuffer in) {
        if(in.remaining() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        in.get(header);

        sb = sb.append(new String(header)).reverse();
        System.out.println(sb.toString());
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
}
