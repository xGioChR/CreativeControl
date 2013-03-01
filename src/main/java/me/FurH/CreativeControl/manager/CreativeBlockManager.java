package me.FurH.CreativeControl.manager;

import me.FurH.Core.cache.CoreLRUCache;
import me.FurH.Core.location.LocationUtils;
import me.FurH.CreativeControl.CreativeControl;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockManager {
    private static CoreLRUCache<String, CreativeBlockData> cache;

    public CreativeBlockManager() {
        cache = new CoreLRUCache<String, CreativeBlockData>(CreativeControl.getMainConfig().cache_capacity);
    }

    public void unprotect(Block b) {
        CreativeControl.getDb().unprotect(b);
    }

    public void protect(Player p, Block b) {
        CreativeControl.getDb().protect(p, b);
    }

    
    public CreativeBlockData isprotected(Block block, boolean nodrop) {
        String key = LocationUtils.locationToString(block.getLocation());

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        CreativeBlockData data = CreativeControl.getDb().isprotected(block, nodrop);
        cache.put(key, data);

        return data;
    }
}