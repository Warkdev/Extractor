/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mangos.extractor.file.chunk;

/**
 *
 * @author Warkdev
 */
public class MCLY {
    private int textureId;
    private int flags;
    private int offsetinMCAL;
    private int effectId;

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getOffsetinMCAL() {
        return offsetinMCAL;
    }

    public void setOffsetinMCAL(int offsetinMCAL) {
        this.offsetinMCAL = offsetinMCAL;
    }

    public int getEffectId() {
        return effectId;
    }

    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }
    
    
}
