/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor.file;

import eu.mangos.extractor.file.chunk.MCIN;
import eu.mangos.extractor.file.exception.ADTException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Warkdev
 */
public class ADTFile {

    private static final String HEADER_VERSION = "MVER";
    private static final String HEADER_MHDR = "MHDR";
    private static final String HEADER_MCIN = "MCIN";

    private byte[] data;

    private int version;
    private int offsetMCIN;
    private MCIN[] chunkIndex;
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
    
    public void init(byte[] data) throws IOException, ADTException {
        this.data = data;
        this.chunkIndex = new MCIN[256];
        readFile();
    }

    private void readFile() throws IOException, ADTException {
        ByteBuffer in = ByteBuffer.wrap(data);
        in.order(ByteOrder.LITTLE_ENDIAN);
        readVersion(in);
        readMHDR(in);
        readMCIN(in);
        readMTEX(in);
        readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
        //readNextHeader(in);
    }

    /**
     * Reading the ADT version. The ADT version is expected to be located at the beginning of the ADT File under this form:
     * 4 character indicating the MVER chunk in reverse order.
     * 4 bytes indicating the length of the MVER chunk.
     * 4 bytes indicating the version of the ADT File (18 in 1.12.x).
     * e.g.: REVM 04 00 00 00 12 00 00 00
     * @param in
     * @throws IOException
     * @throws ADTException 
     */
    private void readVersion(ByteBuffer in) throws ADTException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];                
        
        in.get(header);        
        
        sb = sb.append(new String(header)).reverse();
        if(!sb.toString().equals(HEADER_VERSION)){
            throw new ADTException("Expected header "+HEADER_VERSION+", received header: "+sb.toString());
        }
        
        // We skip the size as we know it's 4.
        in.getInt();
        this.version = in.getInt();        
    }
    
    /**
     * Reading the MHDR chunk of the ADT file. The MHDR chunk is expected to be right after the MVER chunk. It contains offset towards the 
     * various chunks hold in this ADT.
     * e.g. : RDHM 
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
        if(!sb.toString().equals(HEADER_MHDR)){
            throw new ADTException("Expected header "+HEADER_MHDR+", received header: "+sb.toString());
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
        if(!sb.toString().equals(HEADER_MCIN)){
            throw new ADTException("Expected header "+HEADER_MCIN+", received header: "+sb.toString());
        }
                
        int size = in.getInt();
        MCIN index;
        for(int i = 0; i < this.chunkIndex.length; i++) {
            index = new MCIN();
            index.setOffsetMCNK(in.getInt());
            index.setSize(in.getInt());
            index.setFlags(in.getInt());
            index.setAsyncId(in.getInt());
            chunkIndex[i] = index;
        }
        
    }
    
    private void readMTEX(ByteBuffer in) {
        
    }
    
    private void readNextHeader(ByteBuffer in) {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];        
        
        System.out.println(in.position());
        in.get(header);        
        
        sb = sb.append(new String(header)).reverse();
        System.out.println(sb.toString());
    }
}
