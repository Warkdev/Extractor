/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBox;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2Array;
import eu.jangos.extractor.file.m2.M2Attachment;
import eu.jangos.extractor.file.m2.M2Camera;
import eu.jangos.extractor.file.m2.M2Color;
import eu.jangos.extractor.file.m2.M2CompBone;
import eu.jangos.extractor.file.m2.M2Event;
import eu.jangos.extractor.file.m2.M2Light;
import eu.jangos.extractor.file.m2.M2Loop;
import eu.jangos.extractor.file.m2.M2Material;
import eu.jangos.extractor.file.m2.M2Particle;
import eu.jangos.extractor.file.m2.M2Ribbon;
import eu.jangos.extractor.file.m2.M2Sequence;
import eu.jangos.extractor.file.m2.M2SkinProfile;
import eu.jangos.extractor.file.m2.M2Texture;
import eu.jangos.extractor.file.m2.M2TextureTransform;
import eu.jangos.extractor.file.m2.M2TextureWeight;
import eu.jangos.extractor.file.m2.M2Vertex;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class M2FileReader {

    private static final String HEADER_MD20 = "MD20";
    private static final int SUPPORTED_VERSION = 256;

    // Control variable.
    private boolean init = false;
    
    // Magic & version.
    private ByteBuffer data;
    private int version;
    
    // Header info.
    private M2Array<Character> name;
    private int globalFlags;
    private M2Array<M2Loop> globalLoops;
    private M2Array<M2Sequence> sequences;
    private M2Array<Short> sequenceLookups;
    private M2Array<?> playableAnimationLookup;
    private M2Array<M2CompBone> bones;
    private M2Array<Short> keyBoneLookup;
    private M2Array<M2Vertex> vertices;
    private M2Array<M2SkinProfile> skinProfiles;
    private M2Array<M2Color> colors;
    private M2Array<M2Texture> textures;
    private M2Array<M2TextureWeight> textureWeights;
    private M2Array<?> unknown;
    private M2Array<M2TextureTransform> textureTransforms;
    private M2Array<Short> replacableTextureLookup;
    private M2Array<M2Material> materials;
    private M2Array<Short> boneLookupTable;
    private M2Array<Short> textureLookupTable;
    private M2Array<Short> texUnitLookupTable;
    private M2Array<Short> transparencyLookupTable;
    private M2Array<Short> textureTransformsLookupTable;
    private CAaBox boundingBox;
    private float boundingSphereRadius;
    private CAaBox collisionBox;
    private float collisionSphereRadius;
    private M2Array<Short> collisionTriangles;
    private M2Array<Vec3f> collisionVertices;
    private M2Array<Vec3f> collisionNormals;
    private M2Array<M2Attachment> attachments;
    private M2Array<Short> attachmentLookupTable;
    private M2Array<M2Event> events;
    private M2Array<M2Light> lights;
    private M2Array<M2Camera> cameras;
    private M2Array<Short> cameraLookupTable;
    private M2Array<M2Ribbon> ribbonEmitters;
    private M2Array<M2Particle> particleEmitters;
    private M2Array<Short> textureCombinerCombos;    
    private List<M2Vertex> listVertices;    
    private List<M2SkinProfile> listSkinProfiles;
    
    public M2FileReader() {
        this.name = new M2Array<>();
        this.globalLoops = new M2Array<>();
        this.sequences = new M2Array<>();
        this.sequenceLookups = new M2Array<>();
        this.playableAnimationLookup = new M2Array<Object>();
        this.keyBoneLookup = new M2Array<>();
        this.vertices = new M2Array<>();
        this.bones = new M2Array<>();
        this.skinProfiles = new M2Array<>();
        this.colors = new M2Array<>();
        this.textures = new M2Array<>();
        this.textureWeights = new M2Array<>();
        this.unknown = new M2Array<Object>();
        this.textureTransforms = new M2Array<>();
        this.replacableTextureLookup = new M2Array<>();
        this.materials = new M2Array<>();
        this.boneLookupTable = new M2Array<>();
        this.textureLookupTable = new M2Array<>();
        this.texUnitLookupTable = new M2Array<>();
        this.transparencyLookupTable = new M2Array<>();
        this.textureTransformsLookupTable = new M2Array<>();
        this.boundingBox = new CAaBox();
        this.boundingSphereRadius = 0;
        this.collisionBox = new CAaBox();
        this.collisionSphereRadius = 0;
        this.collisionTriangles = new M2Array<>();
        this.collisionVertices = new M2Array<>();
        this.collisionNormals = new M2Array<>();
        this.attachments = new M2Array<>();
        this.attachmentLookupTable = new M2Array<>();
        this.events = new M2Array<>();
        this.lights = new M2Array<>();
        this.cameras = new M2Array<>();
        this.cameraLookupTable = new M2Array<>();
        this.ribbonEmitters = new M2Array<>();
        this.particleEmitters = new M2Array<>();
        this.textureCombinerCombos = new M2Array<>();    
        
        // Caching objects.
        this.listVertices = new ArrayList<>();
        this.listSkinProfiles = new ArrayList<>();
    }

    public void init(byte[] data) throws IOException, M2Exception {
        init = false;
        this.data = ByteBuffer.wrap(data);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        clear();
        readHeader();     
        init = true;
    }

    private void readHeader() throws M2Exception {                
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];        

        this.data.get(header);

        sb = sb.append(new String(header));

        if (!sb.toString().equals(HEADER_MD20)) {
            throw new M2Exception("Expected header " + HEADER_MD20 + ", received header: " + sb.toString());
        }

        // Version.
        int version = this.data.getInt();
        if (SUPPORTED_VERSION != version) {
            throw new M2Exception("Expected version " + SUPPORTED_VERSION + ", version found: " + version);
        }

        // Reading now all offsets & size of chunks data in the M2.
        this.name.read(this.data);
        this.globalFlags = this.data.getInt();
        this.globalLoops.read(this.data);
        this.sequences.read(this.data);
        this.sequenceLookups.read(this.data);
        this.playableAnimationLookup.read(this.data);
        this.bones.read(this.data);
        this.keyBoneLookup.read(this.data);
        this.vertices.read(this.data);
        this.skinProfiles.read(this.data);
        this.colors.read(this.data);
        this.textures.read(this.data);
        this.textureWeights.read(this.data);
        this.unknown.read(this.data);
        this.textureTransforms.read(this.data);
        this.replacableTextureLookup.read(this.data);
        this.materials.read(this.data);
        this.boneLookupTable.read(this.data);
        this.textureLookupTable.read(this.data);
        this.texUnitLookupTable.read(this.data);
        this.transparencyLookupTable.read(this.data);
        this.textureTransformsLookupTable.read(this.data);        
        this.boundingBox.read(this.data);
        this.boundingSphereRadius = this.data.getFloat();
        this.collisionBox.read(this.data);
        this.collisionSphereRadius = this.data.getFloat();
        this.collisionTriangles.read(this.data);
        this.collisionVertices.read(this.data);
        this.collisionNormals.read(this.data);
        this.attachments.read(this.data);
        this.attachmentLookupTable.read(this.data);
        this.events.read(this.data);
        this.lights.read(this.data);
        this.cameras.read(this.data);
        this.cameraLookupTable.read(this.data);
        this.ribbonEmitters.read(this.data);
        this.particleEmitters.read(this.data);
        this.textureCombinerCombos.read(this.data);        
    }

    public List<M2Vertex> getVertices() throws M2Exception {
        if(!init) {
            throw new M2Exception("M2 file has not been initialized, please use init(data) function to initialize your M2 file !");
        }
        
        // Prefer to use cached objects as they will be re-used many times.
        if(this.listVertices.size() > 0) {
            return this.listVertices;
        }
                
        M2Vertex vertex;
        
        this.data.position(this.vertices.getOffset());
        for(int i = 0; i < this.vertices.getSize(); i++) {
            vertex = new M2Vertex();
            vertex.read(this.data);            
            this.listVertices.add(vertex);
        }
        
        return this.listVertices;
    }
    
    public List<M2SkinProfile> getSkins() throws M2Exception {
        if(!init) {
            throw new M2Exception("M2 file has not been initialized, please use init(data) function to initialize your M2 file !");
        }
        
        // Prefer to use cached objects as they will be re-used many times.
        if(this.listSkinProfiles.size() > 0) {
            return this.listSkinProfiles;
        }
                
        M2SkinProfile skin;
        
        this.data.position(this.skinProfiles.getOffset());            
        for(int i = 0; i < this.skinProfiles.getSize(); i++) {
            skin = new M2SkinProfile();
            skin.read(this.data);
            this.listSkinProfiles.add(skin);
        }
        
        return this.listSkinProfiles;
    }
    
    public List<Short> getTextureLookup() {
        List<Short> listTextureLookup = new ArrayList<>();
        
        this.data.position(this.textureLookupTable.getOffset());
        for(int i = 0; i < this.textureLookupTable.getSize(); i++) {
            listTextureLookup.add(this.data.getShort());
        }
        
        return listTextureLookup;
    }

    private void clear() {
        this.listVertices.clear();
        this.listSkinProfiles.clear();
    }
}
