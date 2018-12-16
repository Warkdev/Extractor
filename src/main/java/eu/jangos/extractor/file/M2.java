/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import com.sun.javafx.geom.Vec3f;
import eu.jangos.extractor.file.common.CAaBox;
import eu.jangos.extractor.file.exception.FileReaderException;
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
public class M2 extends FileReader {

    private static final String HEADER_MD20 = "MD20";
    private static final int SUPPORTED_VERSION = 256;
    
    // Magic & version.
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
    
    public M2() {
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

    @Override
    public void init(byte[] data, String filename) throws IOException, FileReaderException {
        super.init = false;
        super.filename = filename;
        super.data = ByteBuffer.wrap(data);
        super.data.order(ByteOrder.LITTLE_ENDIAN);
        clear();
        readHeader();     
        super.init = true;
    }

    private void readHeader() throws FileReaderException {  
        checkHeader(HEADER_MD20, false);        

        // Version.
        int version = super.data.getInt();
        if (SUPPORTED_VERSION != version) {
            throw new M2Exception("Expected version " + SUPPORTED_VERSION + ", version found: " + version);
        }

        // Reading now all offsets & size of chunks data in the M2.
        this.name.read(super.data);
        this.globalFlags = super.data.getInt();
        this.globalLoops.read(super.data);
        this.sequences.read(super.data);
        this.sequenceLookups.read(super.data);
        this.playableAnimationLookup.read(super.data);
        this.bones.read(super.data);
        this.keyBoneLookup.read(super.data);
        this.vertices.read(super.data);
        this.skinProfiles.read(super.data);
        this.colors.read(super.data);
        this.textures.read(super.data);
        this.textureWeights.read(super.data);
        this.unknown.read(super.data);
        this.textureTransforms.read(super.data);
        this.replacableTextureLookup.read(super.data);
        this.materials.read(super.data);
        this.boneLookupTable.read(super.data);
        this.textureLookupTable.read(super.data);
        this.texUnitLookupTable.read(super.data);
        this.transparencyLookupTable.read(super.data);
        this.textureTransformsLookupTable.read(super.data);        
        this.boundingBox.read(super.data);
        this.boundingSphereRadius = super.data.getFloat();
        this.collisionBox.read(super.data);
        this.collisionSphereRadius = super.data.getFloat();
        this.collisionTriangles.read(super.data);
        this.collisionVertices.read(super.data);
        this.collisionNormals.read(super.data);
        this.attachments.read(super.data);
        this.attachmentLookupTable.read(super.data);
        this.events.read(super.data);
        this.lights.read(super.data);
        this.cameras.read(super.data);
        this.cameraLookupTable.read(super.data);
        this.ribbonEmitters.read(super.data);
        this.particleEmitters.read(super.data);
        this.textureCombinerCombos.read(super.data);        
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
        
        super.data.position(this.vertices.getOffset());
        for(int i = 0; i < this.vertices.getSize(); i++) {
            vertex = new M2Vertex();
            vertex.read(super.data);            
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
        
        super.data.position(this.skinProfiles.getOffset());            
        for(int i = 0; i < this.skinProfiles.getSize(); i++) {
            skin = new M2SkinProfile();
            skin.read(super.data);
            this.listSkinProfiles.add(skin);
        }
        
        return this.listSkinProfiles;
    }
    
    public List<Short> getTextureLookup() {
        List<Short> listTextureLookup = new ArrayList<>();
        
        super.data.position(this.textureLookupTable.getOffset());
        for(int i = 0; i < this.textureLookupTable.getSize(); i++) {
            listTextureLookup.add(super.data.getShort());
        }
        
        return listTextureLookup;
    }

    private void clear() {
        this.listVertices.clear();
        this.listSkinProfiles.clear();
    }
}
