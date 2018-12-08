/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.jangos.extractor.file;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import javafx.geometry.Point3D;

/**
 *
 * @author Warkdev
 */
public class Vertex {
    private Vec3f position;
    private Vec3f normal;
    private Vec2f textCoord;
    private Vec3f color;    
    
    public Vec3f getPosition() {
        return position;
    }

    public void setPosition(Vec3f position) {
        this.position = position;
    }

    public void setPosition(Point3D point) {
        this.position = new Vec3f((float) point.getX(),(float) point.getY(), (float) point.getZ());
    }
    
    public Vec3f getNormal() {
        return normal;
    }

    public void setNormal(Vec3f normal) {
        this.normal = normal;
    }

    public Vec2f getTextCoord() {
        return textCoord;
    }

    public void setTextCoord(Vec2f textCoord) {
        this.textCoord = textCoord;
    }

    public Vec3f getColor() {
        return color;
    }

    public void setColor(Vec3f color) {
        this.color = color;
    }

    
}
