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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.cache.CoreLRUCache;
import me.FurH.Core.database.CoreSQLDatabase;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.Core.location.LocationUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.database.extra.CreativeSQLUpdater;
import me.FurH.CreativeControl.manager.CreativeBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        this.version = 2;
    }
    
    public void protect(Player player, Block block) {
        queue("INSERT INTO `"+prefix+"blocks_"+block.getWorld().getName()+"` (owner, x, y, z, type, allowed, time) VALUES ('"+getPlayerId(player.getName()) + "', '" + block.getX() + "', '" + block.getY() + "', '" + block.getZ() + "', '" + block.getTypeId() + "', '" + null + "', '" + System.currentTimeMillis() + "');");
    }
    
    public void unprotect(Block block) {
        queue("DELETE FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
    }
    
    public CreativeBlockData getFullData(Location block) {
        
        Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeBlockData data = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = getQuery("SELECT * FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");

            rs = ps.getResultSet();

            if (rs.next()) {
                data = new CreativeBlockData(getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "), Long.toString(rs.getLong("time")));
            } else if (CreativeSQLUpdater.lock) {
                ps = getQuery("SELECT * FROM `"+prefix+"blocks` WHERE location = "+LocationUtils.locationToString2(block)+"';");
                rs = ps.getResultSet();

                if (rs.next()) {
                    data = new CreativeBlockData(getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "), rs.getString("time"));
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
    
    public CreativeBlockData isprotected(Block block, boolean nodrop) {
        
        Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeBlockData data = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            if (nodrop) {
                ps = getQuery("SELECT owner, type, allowed FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
            } else {
                ps = getQuery("SELECT type FROM `"+prefix+"blocks_"+block.getWorld().getName()+"` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';");
            }

            rs = ps.getResultSet();
        
            if (rs.next()) {
                if (nodrop) {
                    data = new CreativeBlockData(getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                } else {
                    data = new CreativeBlockData(rs.getInt("type"));
                }
            } else if (CreativeSQLUpdater.lock) {
                ps = getQuery("SELECT owner, type, allowed FROM `"+prefix+"blocks` WHERE location = "+LocationUtils.locationToString2(block.getLocation())+"';");
                rs = ps.getResultSet();

                if (rs.next()) {
                    data = new CreativeBlockData(getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
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

        if (data != null && data.type != block.getTypeId()) {
            data = null;
        }
        
        return data;
    }
    
    public void load() {
        load(connection, type);
    }

    public void load(Connection connection, type type) {
        Communicator com = plugin.getCommunicator();
        
        try {
            /* player id table */
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"players` ({auto}, player VARCHAR(255));", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"players` table, " + ex.getCause().getMessage());
        }


        createIndex(connection, "CREATE INDEX `"+prefix+"names` ON `"+prefix+"players` (player);");
        try {
            /* players inventory */
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"players_adventurer` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"players_adventurer` table, " + ex.getCause().getMessage());
        }
        try {
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"players_survival` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"players_survival` table, " + ex.getCause().getMessage());
        }
        try {
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"players_creative` ({auto}, player INT, armor TEXT, inventory TEXT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"players_creative` table, " + ex.getCause().getMessage());
        }

        /* block data */
        for (World world : Bukkit.getWorlds()) {
            load(connection, world.getName(), type);
        }
        
        try {
            /* region data */
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"regions` ({auto}, name VARCHAR(255), start VARCHAR(255), end VARCHAR(255), type VARCHAR(255));", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"regions` table, " + ex.getCause().getMessage());
        }
        try {
            /* friends data */
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"friends` ({auto}, player INT, friends TEXT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"friends` table, " + ex.getCause().getMessage());
        }
        try {
            /* internal data */
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"internal` (version INT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"internal` table, " + ex.getCause().getMessage());
        }
    }

    public void load(Connection connection, String world, type type) {
        Communicator com = plugin.getCommunicator();
        
        if (connection == null) {
            connection = this.connection;
        }
        
        if (type == null) {
            type = this.type;
        }

        try {
            createTable(connection, "CREATE TABLE IF NOT EXISTS `"+prefix+"blocks_"+world+"` (owner INT, x INT, y INT, z INT, type INT, allowed VARCHAR(255), time BIGINT);", type);
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create `"+prefix+"blocks_"+world+"` table, " + ex.getCause().getMessage());
        }

        /* create the index */
        createIndex(connection, "CREATE INDEX `"+prefix+"block_"+world+"` ON `"+prefix+"blocks_"+world+"` (x, z, y);");
        createIndex(connection, "CREATE INDEX `"+prefix+"type_"+world+"` ON `"+prefix+"blocks_"+world+"` (type);");
        createIndex(connection, "CREATE INDEX `"+prefix+"owner_"+world+"` ON `"+prefix+"blocks_"+world+"` (owner);");
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