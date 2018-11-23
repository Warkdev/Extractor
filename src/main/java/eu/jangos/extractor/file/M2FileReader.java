/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2Array;
import eu.jangos.extractor.file.m2.M2CompBone;
import eu.jangos.extractor.file.m2.M2Loop;
import eu.jangos.extractor.file.m2.M2Sequence;
import eu.jangos.extractor.file.m2.M2SkinProfile;
import eu.jangos.extractor.file.m2.M2Vertex;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Warkdev
 */
public class M2FileReader {

    private static final String HEADER_MD20 = "MD20";
    private static final int SUPPORTED_VERSION = 256;

    private byte[] data;
    private int version;    
    private M2Array<Character> name;
    private int flagTiltX;
    private int flagTiltY;
    private int unk1;
    private M2Array<M2Loop> globalLoops;
    private M2Array<M2Sequence> sequences;
    private M2Array<Short> sequenceLookups;
    private M2Array<?> playableAnimationLookup;
    private M2Array<M2CompBone> bones;
    private M2Array<Short> keyBoneLookup;
    private M2Array<M2Vertex> vertices;
    private M2Array<M2SkinProfile> skinProfiles;
    
    
    public M2FileReader() {
        this.name = new M2Array<>();
        this.globalLoops = new M2Array<>();
        this.sequences = new M2Array<>();
        this.sequenceLookups = new M2Array<>();
        this.playableAnimationLookup = new M2Array<Object>();
        this.bones = new M2Array<>();
    }

    public void init(byte[] data) throws IOException, M2Exception {
        this.data = data;
        readFile();
    }

    private void readFile() throws M2Exception {
        ByteBuffer in = ByteBuffer.wrap(data);
        in.order(ByteOrder.LITTLE_ENDIAN);
        
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];
        int size = 0;

        in.get(header);

        sb = sb.append(new String(header));        

        if (!sb.toString().equals(HEADER_MD20)) {
            throw new M2Exception("Expected header " + HEADER_MD20 + ", received header: " + sb.toString());
        }

        // Version.
        int version = in.getInt();
        if(SUPPORTED_VERSION != version) {
            throw new M2Exception("Expected version " + SUPPORTED_VERSION + ", version found: "+ version);
        }
        
        this.name.read(in);
        this.flagTiltX = in.getInt();
        this.flagTiltY = in.getInt();
        this.unk1 = in.getInt();
        
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
