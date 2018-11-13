/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import eu.mangos.extractor.file.ADTFile;
import eu.mangos.extractor.file.exception.ADTException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

/**
 *
 * @author Warkdev
 */
public class Extractor {
    
    public static void main(String[] args) {
       
        String PATH = "D:\\Downloads\\WOW-NOSTALGEEK\\WOW-NOSTALGEEK\\Data\\patch.MPQ";
        String map = "World\\Maps\\Azeroth\\Azeroth_31_49.adt";
        String file = "D:\\Downloads\\Azeroth_31_49.adt";
        ADTFile adt = new ADTFile();
        File mpq = new File(PATH);        
        ByteArrayOutputStream output = new ByteArrayOutputStream();        
        try {
            JMpqEditor editor = new JMpqEditor(mpq, MPQOpenOption.READ_ONLY);
            /**for(String file : editor.getFileNames()) {
                if(file.endsWith("adt")) {
                    System.out.println(file);
                }
            }*/
            //editor.extractFile(map, new File(file));
            adt.init(editor.extractFileAsBytes(map));            
            
            
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ADTException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
