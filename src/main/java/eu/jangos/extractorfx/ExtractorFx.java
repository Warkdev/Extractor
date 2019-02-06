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
package eu.jangos.extractorfx;

import eu.jangos.extractorfx.gui.MainController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractorFx extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);        

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Starting application...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/eu/jangos/fxml/extractorfx/main/Main.fxml"));
        Parent root = loader.load();
        ((MainController) loader.getController()).setStage(stage);
        Scene scene = new Scene(root);                
        
        // Maximizing.
        stage.setMaximized(true);
        stage.setTitle("Extractor FX");

        stage.setScene(scene);
        stage.show();        
        logger.info("Application started!");
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
}
