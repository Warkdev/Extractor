/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file.adt.chunk;

import java.nio.ByteBuffer;

/**
 *
 * @author Warkdev
 */
public class MCNR {    
    private Vector[] points = new Vector[145];

    public void read(ByteBuffer in) {
        for (int j = 0; j < points.length; j++) {
            this.points[j] = new Vector((int) in.get(), (int) in.get(), (int) in.get());
        }
    }
    
    public Vector[] getPoints() {
        return points;
    }

    public void setPoints(Vector[] points) {
        this.points = points;
    }
    
    
}
