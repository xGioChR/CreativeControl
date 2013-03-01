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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.cache.CoreLRUCache;
import me.FurH.Core.database.CoreSQLDatabase;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.manager.CreativeBlockData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public final class CreativeSQLDatabase extends CoreSQLDatabase {
    private static CoreLRUCache<String, Integer> owners = new CoreLRUCache<String, Integer>(Bukkit.getMaxPlayers() * 5);

    public CreativeSQLDatabase(CorePlugin plugin, String prefix, String engine, String database_host, String database_port, String database_table, String database_user, String database_pass) {
        super(plugin, prefix, engine, database_host, database_port, database_table, database_user, database_pass);
    }
    
    public void protect(Player player, Block block) {
        queue("INSERT INTO `"+prefix+"blocks_"+block.getWorld().getName()+"` (owner, x, y, z, type, allowed, time) VALUES ('"+getPlayerId(player.getName()) + "', '" + block.getX() + "', '" + block.getY() + "', '" + block.getZ() + "', '" + block.getTypeId() + "', '" + null + "', '" + System.currentTimeMillis() + "');");
    }
    
    public void unprotect(Block block) {
        queue("DELETE FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
    }
    
    public CreativeBlockData isprotected(Block block, boolean nodrop) {
        
        Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeBlockData data = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            if (!nodrop) {
                ps = getQuery("SELECT owner, x, y, z, type, allowd FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
            } else {
                ps = getQuery("SELECT x, y, z, type FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
            }

            rs = ps.getResultSet();
        
            if (rs.next()) {
                if (!nodrop) {
                    data = new CreativeBlockData(getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                } else {
                    data = new CreativeBlockData(rs.getInt("type"));
                }
            }

        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get block from database, " + ex.getMessage());
        } catch (CoreMsgException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) { }
            }
        }
        
        return data;
    }

    public void load() {
        try {
            /* player id table */
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"players` ({auto}, player VARCHAR(255));");
        } catch (CoreDbException ex) {
            Logger.getLogger(CreativeSQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }


        createIndex("CREATE INDEX `"+prefix+"names` ON `"+prefix+"players` (player);");
        try {
            /* players inventory */
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"players_adventurer` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
        } catch (CoreDbException ex) {
            Logger.getLogger(CreativeSQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"players_survival` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
        } catch (CoreDbException ex) {
            Logger.getLogger(CreativeSQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"players_creative` ({auto}, player INT, armor TEXT, inventory TEXT);");
        } catch (CoreDbException ex) {
            Logger.getLogger(CreativeSQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* block data */
        for (World world : Bukkit.getWorlds()) {
            load(world.getName());
        }
        
        try {
            /* region data */
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"regions` ({auto}, name VARCHAR(255), start VARCHAR(255), end VARCHAR(255), type VARCHAR(255));");
        } catch (CoreDbException ex) {
            plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        try {
            /* friends data */
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"friends` ({auto}, player INT, friends TEXT);");
        } catch (CoreDbException ex) {
            plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        try {
            /* internal data */
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"internal` (version INT);");
        } catch (CoreDbException ex) {
            plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
    }

    public void load(String world) {

        try {
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"blocks_"+world+"` (owner INT, x INT, y INT, z INT, type INT, allowed VARCHAR(255), time BIGINT);");
        } catch (CoreDbException ex) {
            plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }

        /* create the index */
        createIndex("CREATE INDEX `"+prefix+"block_"+world+"` ON `"+prefix+"blocks_"+world+"` (x, z, y);");
        createIndex("CREATE INDEX `"+prefix+"type_"+world+"` ON `"+prefix+"blocks_"+world+"` (type);");
        createIndex("CREATE INDEX `"+prefix+"owner_"+world+"` ON `"+prefix+"blocks_"+world+"` (owner);");
    }
    
    public String getPlayerName(int id) {
        String ret = null;
        
        if (owners.containsValue(id)) {
            return owners.getKey(id);
        }
        
        Communicator com = CreativeControl.plugin.getCommunicator();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = getQuery("SELECT player FROM `"+prefix+"players` WHERE id = '" + id + "' LIMIT 1;");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                ret = rs.getString("player");
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get player data from the database, {0}", ex.getMessage());
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
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

        Communicator com = CreativeControl.plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = getQuery("SELECT id FROM `"+prefix+"players` WHERE player = '" + player + "' LIMIT 1;");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                ret = rs.getInt("id");
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get player data from the database, {0}", ex.getMessage());
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }
        
        if (ret == -1) {

            try {
                execute("INSERT INTO `"+prefix+"players` (player) VALUES ('"+player+"');");
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            }

            return getPlayerId(player);
        }
        
        owners.put(player, ret);
        return ret;
    }
}