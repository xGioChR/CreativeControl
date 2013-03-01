package me.FurH.CreativeControl.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.cache.CreativeLRUCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockManager {
    private static CreativeLRUCache<String, Integer> owners = new CreativeLRUCache<String, Integer>(Bukkit.getMaxPlayers() * 5);
    private static HashSet<Integer> ids = new HashSet<Integer>();
    private int total = 0;

    public String getPlayerName(int id) {
        String ret = null;
        
        if (owners.containsValue(id)) {
            return owners.getKey(id);
        }

        CreativeSQLDatabase db = CreativeControl.getDb();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db.getQuery("SELECT player FROM `"+db.prefix+"players` WHERE id = '" + id + "' LIMIT 1;");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                ret = rs.getString("player");
            }
        } catch (SQLException ex) {
            CreativeCommunicator com = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get player data from the database, {0}", ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }

        owners.put(ret, id);
        return ret;
    }
    
    public int getPlayerId(String player) {
        int ret = -1;
        
        if (owners.containsKey(player)) {
            return owners.get(player);
        }

        CreativeSQLDatabase db = CreativeControl.getDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = db.getQuery("SELECT id FROM `"+db.prefix+"players` WHERE player = '" + player + "' LIMIT 1;");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                ret = rs.getInt("id");
            }
        } catch (SQLException ex) {
            CreativeCommunicator com = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get player data from the database, {0}", ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }
        
        if (ret == -1) {
            db.execute("INSERT INTO `"+db.prefix+"players` (player) VALUES ('"+player+"');");
            return getPlayerId(player);
        }

        owners.put(player, ret);
        return ret;
    }

    public CreativeBlockData getBlock(Block b) {
        return getBlock(b, false);
    }

    public CreativeBlockData getBlock(Block b, boolean force) {
        if (!force && !CreativeControl.getManager().isProtectable(b.getWorld(), b.getTypeId())) {
            return null;
        }

        if (CreativeControl.getMainConfig().cache_dynamic && !ids.contains(b.getTypeId())) {
            return null;
        }

        CreativeBlockCache   cache      = CreativeControl.getCache();
        String               location   = CreativeUtil.getLocation(b.getLocation());
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeWorldNodes   config     = CreativeWorldConfig.get(b.getWorld());

        if (cache.contains(location)) {
            return cache.get(location);
        }

        CreativeBlockData ret = null;
        if (CreativeControl.getMainConfig().cache_dynamic && total > CreativeControl.getMainConfig().cache_precache) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                if (config.block_ownblock) {
                    ps = db.getQuery("SELECT owner, type, allowed FROM `{0}blocks_{1}` WHERE x = '{2}' AND z = '{3}' AND y = '{4}' LIMIT 1;", db.prefix, b.getWorld().getName(), b.getX(), b.getZ(), b.getY());
                } else
                if (config.block_nodrop) {
                    ps = db.getQuery("SELECT type FROM `{0}blocks_{1}` WHERE x = '{2}' AND z = '{3}' AND y = '{4}' LIMIT 1;", db.prefix, b.getWorld().getName(), b.getX(), b.getZ(), b.getY());
                }

                rs = ps.getResultSet();

                if (rs.next()) {
                    int type = rs.getInt("type");

                    if (config.block_ownblock) {
                        int ownerId = rs.getInt("owner");
                        
                        String allowed = rs.getString("allowed");
                        HashSet<String> allowe = new HashSet<String>();
                        if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                            allowe = CreativeUtil.toStringHashSet(allowed, ", ");
                        }
                        
                        if (allowe.isEmpty()) {
                            allowe = null; //Dont store useless data
                        }

                        ret = new CreativeBlockData(getPlayerName(ownerId), type, allowe);
                    } else
                    if (config.block_nodrop) {
                        ret = new CreativeBlockData(null, type, null);
                    }
                }
            } catch (SQLException ex) {
                CreativeCommunicator com        = CreativeControl.getCommunicator();
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }
            }
        }

        cache.add(location, ret);
        return ret;
    }

    public boolean isAllowed(Player p, Block b, CreativeBlockData data) {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativePlayerFriends friends = CreativeControl.getFriends();
        
        if (data == null) {
            return true;
        }
        
        if (b != null) {
            if (data.type != b.getTypeId()) {
                return true;
            }
        }
        
        if (isOwner(p, data.owner)) {
            return true;
        }
        
        if (data.allowed != null) {
            if (isAllowed(p, data.allowed)) {
                return true;
            }
        }
        
        if (config.config_friend) {
            HashSet<String> friend = friends.getFriends(data.owner);
            if (friend.contains(p.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(Player p, HashSet<String> allowed) {
        CreativeControl      plugin     = CreativeControl.getPlugin();

        if (plugin.hasPerm(p, "OwnBlock.isAllowed")) {
            return true;
        }

        if (allowed.contains(p.getName().toLowerCase())) {
            return true;
        }

        return false;
    }

    public List<CreativeBlocks> getBlocks(Block b, boolean cattach) {
        List<CreativeBlocks> blocks = new ArrayList<CreativeBlocks>();

        if (cattach) {
            HashSet<Block> attached = CreativeBlockMatcher.getAttached(b);
            for (Block attach : attached) {
                CreativeBlockData data = getBlock(attach);
                if (data != null) {
                    CreativeBlocks ob = new CreativeBlocks(attach, data);
                    blocks.add(ob);
                }
            }

            if (b.getTypeId() == 64 || b.getTypeId() == 71) {
                Block blockdown = b.getRelative(BlockFace.DOWN);
                if (blockdown.getTypeId() == 64 || blockdown.getTypeId() == 71) {
                    CreativeBlockData data = getBlock(blockdown);
                    if (data != null) {
                        CreativeBlocks ob = new CreativeBlocks(blockdown, data);
                        blocks.add(ob);
                    }
                }
            } else {
                CreativeBlockData data = getBlock(b);
                if (data != null) {
                    CreativeBlocks ob = new CreativeBlocks(b, data);
                    blocks.add(ob);
                } else {
                    Block blockup = b.getRelative(BlockFace.UP);
                    if (blockup.getTypeId() == 64 || blockup.getTypeId() == 71) {
                        data = getBlock(blockup);
                        if (data != null) {
                            CreativeBlocks ob = new CreativeBlocks(blockup, data);
                            blocks.add(ob);
                        }
                    }
                }
            }
        } else {
            CreativeBlockData data = getBlock(b);
            if (data != null) {
                CreativeBlocks ob = new CreativeBlocks(b, data);
                blocks.add(ob);
            }
        }
        return blocks;
    }

    public boolean isProtected(Block b) {
        return getBlock(b) != null;
    }

    public CreativeBlockData getFullData(Location loc) {    
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeBlockData ret = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT owner, type, allowed FROM `{0}blocks_{1}` WHERE x = '{2}' AND z = '{3}' AND y = '{4}' LIMIT 1;", db.prefix, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ(), loc.getBlockY());
            rs = ps.getResultSet();
            
            if (rs.next()) {
                String owner = rs.getString("owner");
                String allowed = rs.getString("allowed");
                int type = rs.getInt("type");
                String date = rs.getString("time");
                
                HashSet<String> allowe = new HashSet<String>();
                if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                    allowe = CreativeUtil.toStringHashSet(allowed, ", ");
                }

                if (allowe.isEmpty()) {
                    allowe = null; //Dont store useless data
                }

                ret = new CreativeBlockData (owner, type, allowe, date);
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }
        return ret;
    }

    public boolean isOwner(Player p, String owner) {
        CreativeControl      plugin     = CreativeControl.getPlugin();
        if (plugin.hasPerm(p, "OwnBlock.isOwner")) {
            return true;
        } 

        if (owner.equalsIgnoreCase(p.getName())) {
            return true;
        }
        return false;
    }

    public void delPlayer(String args, Block block) {
        CreativeBlockData data = getFullData(block.getLocation());

        if (data != null) {
            if (data.owner.equalsIgnoreCase(args)) {
                delBlock(block);
            }
        }
    }

    public void delType(String args, Block block) {
        try {
            int type = Integer.parseInt(args);
            ids.remove(type);
            if (block.getTypeId() == type) {
                delBlock(block);
            }
        } catch (Exception ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] {0} is not a valid number!", args);
        }
    }

    public void update(Location loc, String owner, HashSet<String> allowed) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        
        String location = CreativeUtil.getLocation(loc);
        cache.add(location, new CreativeBlockData (owner, loc.getBlock().getTypeId(), allowed));

        db.execute("UPDATE `"+db.prefix+"blocks_"+loc.getWorld().getName()+"` SET `allowed` = '"+null+"' WHERE x = '" + loc.getBlockX() + "' AND z = '" + loc.getBlockZ() + "' AND y = '" + loc.getBlockY() + "';");
    }

    public void addBlock(Player p, Block b, boolean nodrop) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(p.getName().toLowerCase(), b.getLocation(), b.getTypeId(), nodrop);
        }
    }

    public void addBlock(String player, Block b, boolean nodrop) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(player.toLowerCase(), b.getLocation(), b.getTypeId(), nodrop);
        }
    }

    private void addBlock(String player, Location loc, int type, boolean nodrop) {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeBlockCache   cache      = CreativeControl.getCache();

        String location = CreativeUtil.getLocation(loc);

        if (!nodrop) {
            cache.add(location, new CreativeBlockData (player, type, null));
        } else {
            cache.add(location, new CreativeBlockData (null, type, null));
        }
        
        ids.add(type);
        total++;
        
        db.queue("INSERT INTO `"+db.prefix+"blocks_"+loc.getWorld().getName()+"` (owner, x, y, z, type, allowed, time) VALUES ('"+getPlayerId(player)+"', '"+loc.getBlockX()+"', '"+loc.getBlockY()+"', '"+loc.getBlockZ()+"', '"+loc.getBlock().getTypeId()+"', '"+null+"', '"+System.currentTimeMillis()+"');");
    }
    
    public void delBlock(Block b) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            delBlock(b.getLocation());
        }
    }

    public void delBlock(Location loc) {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeControl.getCache().remove(CreativeUtil.getLocation(loc));
        
        db.queue("DELETE FROM `"+db.prefix+"blocks_"+loc.getWorld().getName()+"` WHERE x = '" + loc.getBlockX() + "' AND z = '" + loc.getBlockZ() + "' AND y = '" + loc.getBlockY() + "';");
        total--;
    }

    public boolean isProtectable(World w, int typeId) {
        CreativeWorldNodes wconfig = CreativeWorldConfig.get(w);
        if (wconfig.block_invert) {
            if (wconfig.block_exclude.contains(typeId)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (wconfig.block_exclude.contains(typeId)) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    public void setup() {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        int loaded = 0;
        World world = Bukkit.getWorlds().get(0);
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        CreativeMainConfig main = CreativeControl.getMainConfig();
        
        try {
            if (main.cache_dynamic) {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks_"+world.getName()+"` ORDER BY time DESC;");
            } else {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks_"+world.getName()+"` ORDER BY time DESC LIMIT {0};", main.cache_precache);
            }

            rs = ps.getResultSet();
            
            while (rs.next()) {
                int id = rs.getInt("type");
                if (!ids.contains(id)) {
                    ids.add(id);
                }
                total++;
                
                if (loaded < CreativeControl.getMainConfig().cache_precache) {
                    String location = world.getName() + ":" + rs.getInt("x") + ":" + rs.getInt("y") + ":" + rs.getInt("z");
                    if (config.block_ownblock) {
                        int ownerId = rs.getInt("owner");
                        
                        String allowed = rs.getString("allowed");
                        HashSet<String> allowe = new HashSet<String>();
                        if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                            allowe = CreativeUtil.toStringHashSet(allowed, ", ");
                        }
                        
                        if (allowe.isEmpty()) {
                            allowe = null; //Dont store useless data
                        }

                        CreativeBlockData ret = new CreativeBlockData(getPlayerName(ownerId), id, allowe);
                        CreativeControl.getCache().add(location, ret);
                    } else
                    if (config.block_nodrop) {
                        CreativeBlockData ret = new CreativeBlockData(null, id, null);
                        CreativeControl.getCache().add(location, ret);
                    }
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
        }
    }

    public int getTotal() {
        return total;
    }

    public int loadIds() {
        return ids.size();
    }

    public int preCache() {
        return CreativeControl.getCache().getSize();
    }
}