package me.FurH.CreativeControl.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.FurH.Core.cache.CoreLRUCache;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.Core.location.LocationUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    
    public CoreLRUCache<String, CreativeBlockData> getCache() {
        return cache;
    }

    public boolean isAllowed(Player p, CreativeBlockData data) {
        
        if (data.owner.equalsIgnoreCase(p.getName())) {
            return true;
        }
        
        if (CreativeControl.plugin.hasPerm(p, "OwnBlock.Bypass")) {
            return true;
        }
        
        if (data != null && data.allowed.contains(p.getName())) {
            return true;
        }
        
        CreativeMainConfig config = CreativeControl.getMainConfig();

        if (config.config_friend) {
            CreativePlayerFriends friends = CreativeControl.getFriends();
            return friends.getFriends(data.owner).contains(p.getName());
        }

        return false;
    }
    
    public void unprotect(Block b) {
        if (isprotectable(b.getWorld(), b.getTypeId())) {
            cache.remove(LocationUtils.locationToString(b.getLocation()));

            CreativeControl.getDb().unprotect(b);
        }
    }

    public void protect(Player p, Block b) {
        if (isprotectable(b.getWorld(), b.getTypeId())) {

            CreativeBlockData data = new CreativeBlockData(p.getName(), b.getTypeId(), null);
            cache.put(LocationUtils.locationToString(b.getLocation()), data);

            CreativeControl.getDb().protect(p, b);
        }
    }
    
    public int preCache() {
        
        CreativeSQLDatabase db = CreativeControl.getDb();
        Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeMainConfig config = CreativeControl.getMainConfig();
        
        int worlds = Bukkit.getWorlds().size();
        int pass = 0;
        int count = 0;

        int each = (int) Math.floor(config.cache_precache / worlds);

        try {
            List<World> worldsx = new ArrayList<World>();
            worldsx.addAll(Bukkit.getWorlds());

            Collections.reverse(worldsx);

            for (World world : worldsx) {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks_"+world.getName() + "` ORDER BY 'time' DESC LIMIT "+each+";");
                ResultSet rs = ps.getResultSet();

                pass++;

                boolean nodrop = CreativeControl.getWorldNodes(world).block_nodrop;
                int ran = 0;

                while (rs.next()) {
                    CreativeBlockData data = null;

                    if (!nodrop) {
                        data = new CreativeBlockData(db.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                    } else {
                        data = new CreativeBlockData(rs.getInt("type"));
                    }
                    
                    cache.put(LocationUtils.locationToString(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), world.getName()), data);
                    ran++;
                }
                
                ran++;
                
                if (ran < each) {
                    if ((worlds - pass) > 0) {
                        each = (int) Math.floor((config.cache_precache - pass) / (worlds - pass));
                    } else {
                        each = ((config.cache_precache - pass));
                    }
                }
                
                rs.close();
                ps.close();
            }
            
            worldsx.clear();
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to add protections to cache, {0}", ex.getMessage());
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } catch (CoreMsgException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        }
        
        return count;
    }
    
    public int getTotal() {
        CreativeSQLDatabase db = CreativeControl.getDb();
        Communicator com = CreativeControl.plugin.getCommunicator();
        
        int total = 0;
        
        for (World world : Bukkit.getWorlds()) {
            try {
                total += db.getTableCount(db.prefix+"blocks_"+world.getName());
            } catch (CoreMsgException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            }
        }
        
        return total;
    }
    
    public CreativeBlockData isprotected(Block block, boolean nodrop) {
        
        if (!isprotectable(block.getWorld(), block.getTypeId())) {
            return null;
        }
        
        String key = LocationUtils.locationToString(block.getLocation());

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        CreativeBlockData data = CreativeControl.getDb().isprotected(block, nodrop);
        cache.put(key, data);

        return data;
    }
    
    public CreativeBlockData getFullData(Location location) {
        return CreativeControl.getDb().getFullData(location);
    }
    
    public boolean isprotectable(World world, int typeId) {
        CreativeWorldNodes nodes = CreativeControl.getWorldNodes(world);

        if (nodes.block_invert) {
            return nodes.block_exclude.contains(typeId);
        } else {
            return !nodes.block_exclude.contains(typeId);
        }
    }

    public void clear() {
        cache.clear();
    }
}