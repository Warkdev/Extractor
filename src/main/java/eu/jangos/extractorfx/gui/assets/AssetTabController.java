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
package eu.jangos.extractorfx.gui.assets;

import eu.jangos.extractor.file.ModelRenderer;
import eu.jangos.extractorfx.gui.MainController;
import eu.jangos.extractorfx.rendering.Render2DType;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Warkdev
 */
public class AssetTabController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AssetTabController.class);
    
    @FXML
    private VBox root;
    
    @FXML
    private TextField tfSearch;
    
    @FXML
    private ListView<String> listView;
    private ObservableList<String> listItems = FXCollections.observableArrayList(); 
    private StringProperty selectedItem = new SimpleStringProperty();
    private FilteredList<String> filteredItems = new FilteredList<>(listItems, s -> true);
    private SortedList sortedItems = new SortedList(filteredItems);       
    
    @FXML
    private ContextMenu contextMenu;
    
    private MainController mediator = null;
    
    private ModelRenderer model = null;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.listView.setCellFactory(new StringCellFactory());
        this.sortedItems.setComparator(new StringCellComparator());
        this.listView.setItems(this.sortedItems);
        this.selectedItem.bind(this.listView.getSelectionModel().selectedItemProperty());
        this.tfSearch.textProperty().addListener(obs -> {
            String filter = tfSearch.getText();
            if (filter == null || filter.isEmpty()) {
                filteredItems.setPredicate(s -> true);
            } else {
                filteredItems.setPredicate(s -> FilenameUtils.getBaseName(s).contains(filter));
            }
        });
        
        this.listView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
    }    
    
    @FXML
    private void onSelectedItemAction(ActionEvent event) {
        logger.info(this.selectedItem.get());
    }

    @FXML
    private void onLiquid2DTypeRendering(ActionEvent event) {
        logger.info("2D Rendering Liquid type");        
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_LIQUID_TYPE, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onLiquid2DFishableRendering(ActionEvent event) {
        logger.info("2D Rendering Liquid Fishable");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_LIQUID_FISHABLE, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onLiquid2DAnimatedRendering(ActionEvent event) {
        logger.info("2D Rendering Liquid Animated");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_LIQUID_ANIMATED, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onLiquid2DHeightmapRendering(ActionEvent event) {
        logger.info("2D Rendering Liquid Heightmap");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_LIQUID_HEIGHTMAP, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onTerrain2DHeightmapRendering(ActionEvent event) {
        logger.info("2D Rendering Terrain Heightmap");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_TERRAIN_HEIGHTMAP, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onTerrain2DHolemapRendering(ActionEvent event) {
        logger.info("2D Rendering Terrain Holemap");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_TERRAIN_HOLEMAP, selectedItem.get(), model);
        }
    }
    
    @FXML
    private void onTerrain2DTerrainRendering(ActionEvent event) {
        logger.info("2D Rendering Terrain");
        if (this.mediator != null) {
            mediator.render2D(Render2DType.RENDER_TILEMAP_TERRAIN, selectedItem.get(), model);
        }
    }

    public void setMediator(MainController mediator) {
        this.mediator = mediator;
    }        

    public void setModel(ModelRenderer model) {
        this.model = model;
    }
    
    public StringProperty getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(StringProperty selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void clearListItems() {
        this.listItems.clear();
        this.listView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
    }
         
    public void addItems(List<String> items) {        
        if (items != null && !items.isEmpty()) {
            this.listItems.addAll(items);
            this.listView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> this.contextMenu.show(this.root, e.getScreenX(), e.getScreenY()));
        }
    }
}
