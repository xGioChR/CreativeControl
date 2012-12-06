/*
 * Copyright (C) 2011-2012 FurmigaHumana.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.FurH.CreativeControl.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import Me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockManager {
    
    /*
     * return true if the block is protected
     */
    public boolean isProtected(Block b) {
        return getBlock(b) != null;
    }

    /*
     * return the array representation of the protection
     */
    public String[] getBlock(Block b) {
        return getBlock(b, false);
    }
    
    /*
     * return a simple protected door
     */
    public String[] getDoor2(Block b) {
        String[] data = getBlock(b);
        
        if (data == null) {
            Block blockdown = b.getRelative(BlockFace.DOWN);
            if (blockdown.getTypeId() == 64 || blockdown.getTypeId() == 71) {
                data = getBlock(blockdown);
                if (data != null) {
                    return data;
                }
            }
        }
        
        return data;
    }
    
    public String[] getDoor3(Block b) {
        Block blockup = b.getRelative(BlockFace.UP);
        String[] data = null;
                
        if (blockup.getTypeId() == 64 || blockup.getTypeId() == 71) {
            data = getBlock(blockup);
        }
        
        return data;
    }

    /*
     * return a expencive protected door
     */
    private String[] getDoor(Block b) { //Expensive unrequired check
        
        Block blockup = b.getRelative(BlockFace.UP);
        if (blockup.getTypeId() == 64 || blockup.getTypeId() == 71) {
            String[] data = getBlock(blockup);
            if (data != null) {
                return data;
            }
        } else 
        if (b.getTypeId() == 64 || b.getTypeId() == 71) {
            Block blockdown = b.getRelative(BlockFace.DOWN);
            String[] data = getBlock(blockdown);
            if (data != null) {
                return data;
            }
        }
        
        return null;
    }
    
    /*
     * return the array representation of the protection
     */
    private String[] getBlock(Block b, boolean force) {
        if (!force && !isProtectable(b.getWorld(), b.getTypeId())) {
            return null;
        }

        CreativeBlockCache   cache      = CreativeControl.getCache();
        String location = CreativeUtil.getLocation(b.getLocation());
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        String[] ret = cache.get(location);
        if (ret == null) {
            try {
                ResultSet rs = db.getQuery("SELECT owner, allowed FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
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
                com.error("[TAG] Failed to get the block from the database, {0}", ex, ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            }
        }
                
        return ret;
    }
    
    /*
     * cache the lasted protection
     */
    public int preCache() {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        int ret = 0;
        try {
            ResultSet rs = db.getQuery("SELECT id, owner, location, allowed FROM `"+db.prefix+"blocks` ORDER BY id DESC LIMIT " + config.cache_precache);
            while (rs.next()) {
                String location = rs.getString("location");
                String owner = rs.getString("owner");
                String allowed = rs.getString("allowed");
                
                if (allowed != null || !"[]".equals(allowed) || !"".equals(allowed)) {
                    cache.add(location, new String[] { owner, allowed });
                } else {
                    cache.add(location, new String[] { owner });
                }
                ret++;
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error("[TAG] Failed to get the block from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
        return ret;
    }

    /*
     * return true if the player is allowed to modify that protection
     */
    public boolean isAllowed(Player p, String[] data) {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativePlayerFriends friends = CreativeControl.getFriends();

        if (isOwner(p, data[0])) {
            return true;
        } else {
            if (data.length > 0) {
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

    /*
     * return true if the player is the owner of that protection
     */
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
    
    /*
     * return true if the player is allowed to use that protection
     */
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

    public void update(String location, String owner, String allowed) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        if (allowed != null && !"".equals(allowed) && !"[]".equals(allowed)) {
            cache.replace(location, new String[] { owner, allowed });
            db.executeQuery("UPDATE `"+db.prefix+"blocks` SET `allowed` = '"+allowed+"' WHERE `location` = '"+location+"';");
        } else {
            cache.replace(location, new String[] { owner });
            db.executeQuery("UPDATE `"+db.prefix+"blocks` SET `allowed` = '"+null+"' WHERE `location` = '"+location+"';");
        }
    }

    public void addBlock(Player p, Block b) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(p.getName().toLowerCase(), CreativeUtil.getLocation(b.getLocation()), b.getTypeId());
        }
    }
    
    public void addBlock(String player, Block b) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(player.toLowerCase(), CreativeUtil.getLocation(b.getLocation()), b.getTypeId());
        }
    }
    
    private void addBlock(String player, String location, int type) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        cache.add(location, new String[] { player });
        db.executeQuery("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES ('"+player+"', '"+location+"', '"+type+"', '"+null+"', '"+System.currentTimeMillis()+"');");
    }

    public void delBlock(Block b) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            delBlock(CreativeUtil.getLocation(b.getLocation()));
        }
    }
    
    public void delBlock(Block b, String[] data) {
        delBlock(b);
    }
    
    private void delBlock(String location) {
        CreativeBlockCache   cache      = CreativeControl.getCache();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        cache.remove(location);
        db.executeQuery("DELETE FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
    }

    public String[] getFullData(String location) {    
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        String[] ret = null;
        try {
            ResultSet rs = db.getQuery("SELECT owner, allowed, type, time FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
            if (rs.next()) {
                String owner = rs.getString("owner");
                String allowed = rs.getString("allowed");
                String type = Integer.toString(rs.getInt("type"));
                String date = rs.getString("time");
                ret = new String[] { owner, allowed, type, date };
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error("[TAG] Failed to get the block from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
        return ret;
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

    public void delPlayer(String args, Block block) {
        String[] data = getBlock(block);
        
        if (data != null) {
            if (data[0].equalsIgnoreCase(args)) {
                delBlock(block);
            }
        }
    }

    public void delType(String args, Block block) {
        try {
            int type = Integer.parseInt(args);
            if (block.getTypeId() == type) {
                delBlock(block);
            }
        } catch (Exception ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error("[TAG] {0} is not a valid number!", ex, args);
        }
    }
}