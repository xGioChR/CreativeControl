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

package me.FurH.CreativeControl.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.cache.CreativeFastCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockManager {
    
    public List<Block> getBlocks(Block b, boolean attach) {
        return CreativeNoDropManager.getBlocks(b, attach);
    }

    /*
     * return true if the block is protected
     */
    public boolean isProtected(Block b, boolean nodrop) {
        if (nodrop) {
            return CreativeNoDropManager.isProtected(b);
        }
        return getBlock(b) != null;
    }

    /*
     * return the array representation of the protection
     */
    public String[] getBlock(Block b) {
        return getBlock(b, false);
    }
    
    public String[] getDoor2(Block b) {
        return CreativeOwnBlockManager.getDoor2(b);
    }
    
    public String[] getDoor3(Block b) {
        return CreativeOwnBlockManager.getDoor3(b);
    }

    /*
     * return the array representation of the protection
     */
    private String[] getBlock(Block b, boolean force) {
        return CreativeOwnBlockManager.getBlock(b, force);
    }

    /*
     * return true if the player is allowed to modify that protection
     */
    public boolean isAllowed(Player p, String[] data) {
        return CreativeOwnBlockManager.isAllowed(p, data);
    }

    /*
     * return true if the player is the owner of that protection
     */
    public boolean isOwner(Player p, String owner) {
        return CreativeOwnBlockManager.isOwner(p, owner);
    }

    public void update(String location, String owner, String allowed) {
        CreativeOwnBlockManager.update(location, owner, allowed);
    }

    /*
     * cache the lasted protection
     */
    public int preCache() {
        CreativeMainConfig   config     = CreativeControl.getMainConfig();
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        
        CreativeFastCache    fast       = CreativeControl.getFastCache();
        CreativeBlockCache   slow      = CreativeControl.getSlowCache();
        int ret = 0;
        try {
            ResultSet rs = db.getQuery("SELECT id, owner, location, allowed FROM `"+db.prefix+"blocks` ORDER BY id DESC LIMIT " + config.cache_precache);
            while (rs.next()) {
                String location = rs.getString("location");
                Location loc = CreativeUtil.getLocation(location);
                if (loc != null) {
                    CreativeWorldNodes nodes = CreativeWorldConfig.get(loc.getWorld());
                    
                    if (nodes.block_ownblock) {
                        String owner = rs.getString("owner");
                        String allowed = rs.getString("allowed");

                        if (allowed != null || !"[]".equals(allowed) || !"".equals(allowed)) {
                            slow.add(location, new String[] { owner, allowed });
                        } else {
                            slow.add(location, new String[] { owner });
                        }
                    }

                    if (nodes.block_nodrop) {
                        fast.add(location);
                    }
                    
                    ret++;
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error("[TAG] Failed to get the block from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
        return ret;
    }

    public void addBlock(Player p, Block b, boolean fast) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(p.getName().toLowerCase(), b.getLocation(), b.getTypeId(), fast);
        }
    }
    
    public void addBlock(String player, Block b, boolean fast) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            addBlock(player.toLowerCase(), b.getLocation(), b.getTypeId(), fast);
        }
    }
    
    private void addBlock(String player, Location loc, int type, boolean fast) {
        CreativeSQLDatabase  db         = CreativeControl.getDb();
       
        CreativeFastCache    cfast       = CreativeControl.getFastCache();
        CreativeBlockCache   cslow      = CreativeControl.getSlowCache();
        
        String location = CreativeUtil.getLocation(loc);
        if (fast) {
            cfast.add(location);
        } else {
            cslow.add(location, new String[] { player });
        }
        
        db.executeQuery("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES ('"+player+"', '"+location+"', '"+type+"', '"+null+"', '"+System.currentTimeMillis()+"');");
    }

    public void delBlock(Block b, boolean fast) {
        if (isProtectable(b.getWorld(), b.getTypeId())) {
            delBlock(CreativeUtil.getLocation(b.getLocation()), fast);
        }
    }

    private void delBlock(String location, boolean fast) {
        CreativeFastCache    cfast       = CreativeControl.getFastCache();
        CreativeBlockCache   cslow      = CreativeControl.getSlowCache();
        
        CreativeSQLDatabase  db         = CreativeControl.getDb();

        if (!fast) {
            cslow.remove(location);
        }
        
        cfast.remove(location); //outch :c

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
        String[] data = getFullData(CreativeUtil.getLocation(block.getLocation()));
        
        if (data != null) {
            if (data[0].equalsIgnoreCase(args)) {
                delBlock(block, false);
            }
        }
    }

    public void delType(String args, Block block) {
        try {
            int type = Integer.parseInt(args);
            if (block.getTypeId() == type) {
                delBlock(block, false);
            }
        } catch (Exception ex) {
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            com.error("[TAG] {0} is not a valid number!", ex, args);
        }
    }
}