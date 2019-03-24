/*
 * Copyright 2019 Warkdev.
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

import eu.jangos.extractor.file.exception.MPQException;
import eu.jangos.extractor.file.mpq.MPQManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Warkdev
 */
public class CLI extends Application {

    private static final Logger logger = LoggerFactory.getLogger(CLI.class);
    
    private static final String WOW_PATH = "wowpath";
    private static MPQManager manager;        
    
    private static String path="";
    
    @Override
    public void start(Stage stage) throws Exception {     
        logger.info("Starting application..");
        
        path = getParameters().getNamed().get(WOW_PATH);
        if(path == null || path.isEmpty()) {
            logger.error("Missing or empty path provided.");
            printHelp();
            stage.close();
            Platform.exit();            
            return;
        }
        
        try {
            logger.info("Initializing MPQ Manager..");
            manager = new MPQManager(path);
            logger.info("MPQManager initialized!");
        } catch (MPQException mpqEx) {
            logger.error("Error while initializing the MPQ Manager: ");
            logger.error(mpqEx.getMessage());
        }
        
        logger.info("Application will now shutdown");
        stage.close();
        Platform.exit();
    }
    
    public static void main(String[] args) throws IOException {        
        launch(args);
    }
    
    private static void printHelp() {
        logger.info("Usage of this CLI: ");
        logger.info("--wowpath=my-wow-path: Path to the WoW Data Directory.");                
    }
}
