package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativeEditSession extends EditSession {

    private LocalPlayer player;

    public CreativeEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        super(world, maxBlocks);
        this.player = player;
    }

    public CreativeEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        super(world, maxBlocks, blockBag);
        this.player = player;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {

        if (!(world instanceof BukkitWorld)) {
            return super.rawSetBlock(pt, block);
        }

        World w = ((BukkitWorld) world).getWorld();

        CreativeWorldNodes config = CreativeControl.getWorldNodes(w);
        CreativeBlockManager manager = CreativeControl.getManager();
        
        int oldType = w.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        boolean success = super.rawSetBlock(pt, block);

        if (success) {
            if (!config.world_exclude && config.block_worledit) {
                int newType = block.getType();
                
                if (newType == 0 || oldType != 0) {
                    manager.unprotect(w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                }

                if (newType != 0) {
                    manager.protect(player.getName(), w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                }
            }
        }

        return success;
    }
}
