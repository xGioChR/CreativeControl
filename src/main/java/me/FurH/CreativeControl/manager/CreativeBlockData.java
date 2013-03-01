package me.FurH.CreativeControl.manager;

import java.util.HashSet;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockData {
    public String owner;
    public int type;
    public HashSet<String> allowed;
    public String date;
    
    public CreativeBlockData(int type) {
        this.type = type;
    }
    
    public CreativeBlockData(String owner, int type, HashSet<String> allowed) {
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
    }
    
    public CreativeBlockData(String owner, int type, HashSet<String> allowed, String date) {
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
        this.date = date;
    }
}
