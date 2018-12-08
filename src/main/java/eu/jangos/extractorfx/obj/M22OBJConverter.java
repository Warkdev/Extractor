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
package eu.jangos.extractorfx.obj;

import eu.jangos.extractor.file.M2FileReader;
import eu.jangos.extractor.file.exception.M2Exception;
import eu.jangos.extractor.file.m2.M2SkinProfile;
import eu.jangos.extractor.file.m2.M2SkinSection;
import eu.jangos.extractor.file.m2.M2Vertex;
import eu.jangos.extractorfx.obj.exception.ConverterException;
import java.util.List;

/**
 *
 * @author Warkdev
 */
public class M22OBJConverter extends ModelConverter {

    private M2FileReader reader;

    public M22OBJConverter(M2FileReader reader) {        
        super();
        this.reader = reader;                
    }

    /**
     * Convert the corresponding M2 to its OBJ definition.
     *
     * @param view There are 4 views in the M2 file of the same model. Use
     * number 1 to 4 to extract the view you want.
     * @throws ConverterException
     */
    public void convert(int view) throws ConverterException {
        if (this.reader == null) {
            throw new ConverterException("M2FileReader is null");
        }

        if (view < 1 || view > 4) {
            throw new ConverterException("View number must be between 1 and 4");
        }
        view--;        

        this.mesh.getPoints().clear();
        this.mesh.getNormals().clear();
        this.mesh.getTexCoords().clear();
        this.mesh.getFaces().clear();
                
        try {
            // Converting M2Vertex to Vertex.
            for (M2Vertex v : reader.getVertices()) {                                            
                mesh.getPoints().addAll(v.getPosition().x, v.getPosition().y, v.getPosition().z);
                mesh.getNormals().addAll(v.getNormal().x, v.getNormal().y, v.getNormal().z);
                mesh.getTexCoords().addAll(v.getTexCoords()[0].x, v.getTexCoords()[0].y);                                
            }            
            
            List<M2SkinProfile> listSkins = reader.getSkins();
            for (int i = 0; i < listSkins.get(view).getSubMeshes().size(); i++) {
                List<M2SkinSection> listSkinSections = listSkins.get(view).getSubMeshes();
                List<Short> listIndices = listSkins.get(view).getIndices();
                
                int face = listSkins.get(view).getSubMeshes().get(i).getIndexStart();
                
                while(face < listSkinSections.get(i).getIndexStart() + listSkinSections.get(i).getIndexCount()) {
                    this.mesh.getFaces().addAll(listIndices.get(face + 2), listIndices.get(face + 2), listIndices.get(face + 2) ,
                                listIndices.get(face + 1), listIndices.get(face + 1), listIndices.get(face + 1),
                                listIndices.get(face), listIndices.get(face), listIndices.get(face));
                    face += 3;
                }                             
            }
        } catch (M2Exception ex) {
            throw new ConverterException("Error while eading the M2 content " + ex.getMessage());
        }
    }

    public M2FileReader getReader() {
        return reader;
    }

    public void setReader(M2FileReader reader) {
        this.reader = reader;
    }    

    @Override
    public void convert() throws ConverterException {
        convert(1);
    }

}
