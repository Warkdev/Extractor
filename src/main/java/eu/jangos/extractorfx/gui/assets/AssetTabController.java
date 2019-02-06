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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
    private TextField tfSearch;
    
    @FXML
    private ListView<String> listView;
    private ObservableList<String> listItems = FXCollections.observableArrayList(); 
    private StringProperty selectedItem = new SimpleStringProperty();
    private FilteredList<String> filteredItems = new FilteredList<>(listItems, s -> true);
    private SortedList sortedItems = new SortedList(filteredItems);
    
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
    }    
    
    @FXML
    private void onSelectedItemAction(ActionEvent event) {
        logger.info(this.selectedItem.get());
    }

    public StringProperty getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(StringProperty selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void clearListItems() {
        this.listItems.clear();
    }
         
    public void addItems(List<String> items) {
        this.listItems.addAll(items);
    }
}
