package me.FurH.CreativeControl.manager;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativeBlockLimit {
    
    private long expire;
    private int placed = 0;
    
    public CreativeBlockLimit() {
        expire = System.currentTimeMillis() + 60000;
    }
    
    public boolean isExpired() {
        return expire < System.currentTimeMillis();
    }
    
    public void increment() {
        placed++;
    }
    
    public int getPlaced() {
        return placed;
    }

    public void reset() {
        expire = System.currentTimeMillis() + 60000;
        placed = 0;
    }
}