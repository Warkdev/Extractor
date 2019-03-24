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

module eu.jangos.extractorfx {
    requires logback.classic;
    requires logback.core;
    requires slf4j.api;    
    requires jzlib;
    requires pngtastic.pngtastic;
    requires org.apache.commons.compress;
    requires objenesis;
    requires org.tukaani.xz;
    requires obj;
    requires Utils;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires recast;
    requires detour;
    requires detour.crowd;
    requires detour.tile.cache;    
    requires detour.extras;
    requires gson;
    requires controlsfx;
    requires javafx.controls;        
    requires javafx.swing;
    requires javafx.fxml;  
    requires JMPQ3;    
    
    opens eu.jangos.extractorfx to javafx.fxml;
    opens eu.jangos.extractorfx.gui to javafx.fxml;
    opens eu.jangos.extractorfx.gui.assets to javafx.fxml;
    opens eu.jangos.extractorfx.gui.viewers to javafx.fxml;
    exports eu.jangos.extractorfx;
    exports eu.jangos.extractorfx.gui;
    exports eu.jangos.extractorfx.gui.assets;
    exports eu.jangos.extractorfx.gui.viewers;
}
