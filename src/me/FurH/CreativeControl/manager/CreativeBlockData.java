package me.FurH.CreativeControl.manager;

import org.bukkit.block.Block;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockData {
    private Block block;
    private String[] data;

    public CreativeBlockData(Block block, String[] data) {
        this.block = block;
        this.data = data;
    }
    
    public Block getBlock() {
        return block;
    }
    
    public String[] getData() {
        return data;
    }
}
