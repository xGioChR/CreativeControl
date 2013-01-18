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

    public String[] getBlock(Block b) {
        return getBlock(b, false);
    }

    public String[] getBlock(Block b, boolean force) {
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
            String[] data = cache.get(location);

            if (data ==  null) {
                data = new String[] { "0" };
            }

            return data;
        }

        String[] ret = null;
        if (total > CreativeControl.getMainConfig().cache_precache) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT owner, allowed FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
                rs = ps.getResultSet();
                
                if (rs.next()) {
                    String owner = rs.getString("owner");
                    String allowed = rs.getString("allowed");
                    if (allowed != null || !"[]".equals(allowed) || !"".equals(allowed)) {
                        ret = new String[] { owner, allowed };
                    } else {
                        ret = new String[] { owner };
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

    public boolean isAllowed(Player p, String[] data) {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativePlayerFriends friends = CreativeControl.getFriends();
        
        if (data == null) {
            return true;
        }

        if (isOwner(p, data[0])) {
            return true;
        } else {
            if (data.length >= 1) {
                try {
                    if (isAllowed(p, data[1])) {
                        return true;
                    } else {
                        if (config.config_friend) {
                            HashSet<String> friend = friends.getFriends(data[0]);
                            if (friend.contains(p.getName().toLowerCase())) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    if (config.config_friend) {
                        HashSet<String> friend = friends.getFriends(data[0]);
                        if (friend.contains(p.getName().toLowerCase())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                if (config.config_friend) {
                    HashSet<String> friend = friends.getFriends(data[0]);
                    if (friend.contains(p.getName().toLowerCase())) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }
    
    private boolean isAllowed(Player p, String allowed) {
        CreativeControl      plugin     = CreativeControl.getPlugin();
        if (plugin.hasPerm(p, "OwnBlock.Bypass")) {
            return true;
        } else {
            if (allowed != null && !"[]".equals(allowed) && !"".equals(allowed) && !"null".equals(allowed)) {
                if (CreativeUtil.toStringHashSet(allowed, ", ").contains(p.getName().toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public List<CreativeBlockData> getBlocks(Block b, boolean cattach) {
        List<CreativeBlockData> blocks = new ArrayList<CreativeBlockData>();

        if (cattach) {
            HashSet<Block> attached = CreativeBlockMatcher.getAttached(b);
            for (Block attach : attached) {
                String[] data = getBlock(attach);
                if (data != null) {
                    blocks.add(new CreativeBlockData(attach, data));
                }
            }

            if (b.getTypeId() == 64 || b.getTypeId() == 71) {
                Block blockdown = b.getRelative(BlockFace.DOWN);
                if (blockdown.getTypeId() == 64 || blockdown.getTypeId() == 71) {
                    String[] data = getBlock(blockdown);
                    if (data != null) {
                        blocks.add(new CreativeBlockData(blockdown, data));
                    }
                }
            } else {
                String[] data = getBlock(b);
                if (data != null) {
                    blocks.add(new CreativeBlockData(b, data));
                } else {
                    Block blockup = b.getRelative(BlockFace.UP);
                    if (blockup.getTypeId() == 64 || blockup.getTypeId() == 71) {
                        data = getBlock(blockup);
                        if (data != null) {
                            blocks.add(new CreativeBlockData(blockup, data));
                        }
                    }
                }
            }
        } else {
            String[] data = getBlock(b);
            if (data != null) {
                blocks.add(new CreativeBlockData(b, data));
            }
        }
        return blocks;
    }

    public boolean isProtected(Block b) {
        return getBlock(b) != null;
    }

    public String[] getFullData(String location) {    
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        String[] ret = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT owner, allowed, type, time FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                String owner = rs.getString("owner");
                String allowed = rs.getString("allowed");
                String type = Integer.toString(rs.getInt("type"));
                String date = rs.getString("time");
                ret = new String[] { owner, allowed, type, date };
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
        if (plugin.hasPerm(p, "OwnBlock.Bypass")) {
            return true;
        } else {
            if (owner.equalsIgnoreCase(p.getName())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void delPlayer(String args, Block block) {
        String[] data = getFullData(CreativeUtil.getLocation(block.getLocation()));

        if (data != null) {
            if (data[0].equalsIgnoreCase(args)) {
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

    public void update(String location, String owner, String allowed) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        if (allowed != null && !"".equals(allowed) && !"[]".equals(allowed)) {
            cache.add(location, new String[] { owner, allowed });
            db.executeQuery("UPDATE `"+db.prefix+"blocks` SET `allowed` = '"+allowed+"' WHERE `location` = '"+location+"';");
        } else {
            cache.add(location, new String[] { owner });
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
            cache.add(location, new String[] { player });
        } else {
            cache.add(location);
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
                    ids.add(id);
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
                if (!db.locations.contains(location)) {
                    Location loc = CreativeUtil.getLocation(location);
                    if (loc != null) {
                        CreativeWorldNodes nodes = CreativeWorldConfig.get(loc.getWorld());
                        if (nodes.block_ownblock) {
                            String owner = rs.getString("owner");
                            String allowed = rs.getString("allowed");

                            if (allowed != null || !"[]".equals(allowed) || !"".equals(allowed)) {
                                cache.add(location, new String[] { owner, allowed });
                            } else {
                                cache.add(location, new String[] { owner });
                            }
                        } else
                        if (nodes.block_nodrop) {
                            cache.add(location);
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
