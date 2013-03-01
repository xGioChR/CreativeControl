package me.FurH.CreativeControl.manager;

import org.bukkit.block.Block;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlocks {
    public Block block;
    public CreativeBlockData data;
    
    public CreativeBlocks(Block block, CreativeBlockData data) {
        this.block = block;
        this.data = data;
    }
}
