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

package me.FurH.CreativeControl.database.extra;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSQLCleanup implements Runnable {
    public boolean lock = false;
    private Player p;

    public CreativeSQLCleanup(Player p) {
        this.p = p;
    }
    
    @Override
    public void run() {
        lock = true;

        long startTimer = System.currentTimeMillis();
        long elapsedTime = 0;

        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeMessages messages = CreativeControl.getMessages();
        
        System.gc();
        com.msg(p, messages.updater_loading);
        HashSet<String[]> blocks = new HashSet<String[]>();
        
        /* Backup */ HashSet<String> backup = new HashSet<String>();

        CreativeSQLDatabase db = CreativeControl.getDb();
        CreativeBlockManager manager = CreativeControl.getManager();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks` ORDER BY `id` DESC");
            rs = ps.getResultSet();
            
            while (rs.next()) {
                db.reads++;
                manager.delBlock(rs.getString("location"));
                //blocks.add(new String[] { rs.getString("location"), Integer.toString(rs.getInt("type")) });
                backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                        + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
            }
            
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to load the protections, {0}", ex, ex.getMessage());
            com.msg(p, messages.updater_loadfailed);
            lock = false;
            return;
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

        //CreativeBlockManager manager = CreativeControl.getManager();
        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.updater_loaded, blocks.size(), elapsedTime);
        
        /* Backup Start */
        com.msg(p, messages.backup_generating);
        
        CreativeSQLBackup.backup(backup);
        System.gc();

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.backup_done, elapsedTime);
        /* Backup End */
        
        com.msg(p, messages.cleanup_searching);
        double corrupt = 0;
        double done = 0;
        double process = 0;
        
        double last = 0;
        
        try {
            db.connection.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit and commit the database, {0}.", ex, ex.getMessage());
        }

        HashSet<String> locations = new HashSet<String>();
        for (String[] string : blocks) {
            done++;
            process = ((done / blocks.size()) * 100.0D);
            
            if (process - last > 5) {
                System.gc();
                com.msg(p, messages.cleanup_process, done, blocks.size(), corrupt, String.format("%d", (int) process));
                last = process;
            }

            try {
                Location loc = CreativeUtil.getLocation(string[0]);

                if (loc == null) {
                    corrupt++;
                    manager.delBlock(string[0]);
                    continue;
                }

                Block b = loc.getBlock();
                
                int type = b.getTypeId();
                int dbtype = Integer.parseInt(string[1]);

                if (type != dbtype) {
                    //com.msg(p, messages.cleanup_corrupted, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), type, dbtype);
                    corrupt++;
                    manager.delBlock(b);
                } else
                if (!manager.isProtectable(b.getWorld(), type)) {
                    //com.msg(p, messages.cleanup_corrupted, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), type, dbtype);
                    corrupt++;
                    manager.delBlock(b);
                } else
                if (locations.contains(string[0])) {
                    //com.msg(p, messages.cleanup_duplicated, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    corrupt++;
                    manager.delBlock(b);
                } else {
                    locations.add(string[0]);
                }
            } catch (Exception ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to check the protection: {0}, {1}", ex, string[0], ex.getMessage());
            }
        }

        try {
            db.connection.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit, {0}.", ex, ex.getMessage());
        }
        
        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.cleanup_done, corrupt, blocks.size());
        lock = false;
    }
}