package me.FurH.CreativeControl.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
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
    private static HashSet<Integer> ids = new HashSet<Integer>();
    private int total = 0;

    public CreativeBlockData getBlock(Block b) {
        return getBlock(b, false);
    }

    public CreativeBlockData getBlock(Block b, boolean force) {
        if (!force && !CreativeControl.getManager().isProtectable(b.getWorld(), b.getTypeId())) {
            return null;
        }

        if (!ids.contains(b.getTypeId())) {
            return null;
        }
        
        CreativeBlockCache   cache      = CreativeControl.getCache();
        String location                 = CreativeUtil.getLocation(b.getLocation());
        CreativeSQLDatabase  db         = CreativeControl.getDb();

        if (cache.contains(location)) {
            CreativeBlockData data = cache.get(location);

            if (data ==  null) {
                data = new CreativeBlockData(null, 0, null);
            }

            return data;
        }

        CreativeBlockData ret = null;
        if (total > CreativeControl.getMainConfig().cache_precache) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT owner, type, allowed FROM `"+db.prefix+"blocks` WHERE location = '" + location + "';");
                rs = ps.getResultSet();
                
                if (rs.next()) {
                    
                    String owner = rs.getString("owner");
                    String allowed = rs.getString("allowed");
                    int type = rs.getInt("type");

                    HashSet<String> allowe = new HashSet<String>();
                    if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                        allowe = CreativeUtil.toStringHashSet(allowed, ", ");
                    }
                    
                    if (allowe.isEmpty()) {
                        allowe = null; //Dont store useless data
                    }
                    
                    ret = new CreativeBlockData(owner, type, allowe);
                }
            } catch (SQLException ex) {
                CreativeCommunicator com        = CreativeControl.getCommunicator();
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }/*
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ex) { }
                }*/
            }
        }

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

    public CreativeBlockData getFullData(String location) {    
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeBlockData ret = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT owner, allowed, type, time FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }/*
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }*/
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
        CreativeBlockData data = getFullData(CreativeUtil.getLocation(block.getLocation()));

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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] {0} is not a valid number!", args);
        }
    }

    public void update(String location, String owner, HashSet<String> allowed) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        
        Location loc = CreativeUtil.getLocation(location);        
        cache.add(location, new CreativeBlockData (owner, loc.getBlock().getTypeId(), allowed));

        if (allowed != null) {
            db.executeQuery("UPDATE `"+db.prefix+"blocks` SET `allowed` = '"+new ArrayList<String>(allowed)+"' WHERE `location` = '"+location+"';");
        } else {
            db.executeQuery("UPDATE `"+db.prefix+"blocks` SET `allowed` = '"+null+"' WHERE `location` = '"+location+"';");
        }
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

        db.executeQuery("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES ('"+player+"', '"+location+"', '"+type+"', '"+null+"', '"+System.currentTimeMillis()+"');");
    }
    
    public void delBlock(Block b) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            delBlock(CreativeUtil.getLocation(b.getLocation()));
        }
    }

    public void delBlock(String location) {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeControl.getCache().remove(location);
        db.executeQuery("blocks" + location);
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
    
    public int getTotal() {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        total = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT location FROM `"+db.prefix+"blocks` ORDER BY type DESC");
            rs = ps.getResultSet();
            
            while (rs.next()) {
                if (!db.locations.contains(rs.getString("location"))) {
                    total++;
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
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

        return total;
    }

    public int loadIds() {
        CreativeSQLDatabase  db         = CreativeControl.getDb();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT type, location FROM `"+db.prefix+"blocks` ORDER BY type DESC");
            rs = ps.getResultSet();
            
            while (rs.next()) {
                if (!db.locations.contains(rs.getString("location"))) {
                    int id = rs.getInt("type");
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
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

        return ids.size();
    }

    public int preCache() {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        CreativeBlockCache   cache      = CreativeControl.getCache();

        int ret = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT id, owner, location, allowed FROM `"+db.prefix+"blocks` ORDER BY id DESC LIMIT " + config.cache_precache);
            rs = ps.getResultSet();
            
            while (rs.next()) {
                String location = rs.getString("location");
                int type = rs.getInt("type");
                if (!db.locations.contains(location)) {
                    Location loc = CreativeUtil.getLocation(location);
                    if (loc != null) {
                        CreativeWorldNodes nodes = CreativeWorldConfig.get(loc.getWorld());
                        if (nodes.block_ownblock) {
                            String owner = rs.getString("owner");
                            String allowed = rs.getString("allowed");

                            HashSet<String> allowe = new HashSet<String>();
                            if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                                allowe = CreativeUtil.toStringHashSet(allowed, ", ");
                            }

                            if (allowe.isEmpty()) {
                                allowe = null; //Dont store useless data
                            }

                            CreativeBlockData data = new CreativeBlockData(owner, type, allowe);
                            cache.add(location, data);
                        } else
                        if (nodes.block_nodrop) {
                            CreativeBlockData data = new CreativeBlockData(null, type, null);
                            cache.add(location, data);
                        }
                        ret++;
                    }
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to get the block from the database, {0}", ex.getMessage());
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
        return ret;
    }
}
