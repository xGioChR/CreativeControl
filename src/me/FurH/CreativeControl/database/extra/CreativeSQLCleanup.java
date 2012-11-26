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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.database.CreativeBlockManager;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSQLCleanup extends Thread {
    public boolean lock = false;
    private Player p;

    public CreativeSQLCleanup(Player p) {
        this.p = p;
    }
    
    @Override
    public void run() {
        lock = true;
        Thread t = CreativeSQLCleanup.currentThread();
        t.setName("CreativeControl Cleanup Thread");

        long startTimer = System.currentTimeMillis();
        long elapsedTime = 0;
        t.setPriority(Thread.MAX_PRIORITY);

        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeMessages messages = CreativeControl.getMessages();
        
        com.msg(p, messages.updater_loading);
        List<String[]> blocks = new ArrayList<String[]>();
        
        /* Backup */ List<String> backup = new ArrayList<String>();

        CreativeSQLDatabase db = CreativeControl.getDb();
        try {
            PreparedStatement ps = db.prepare("SELECT * FROM `"+db.prefix+"blocks` ORDER BY `id` DESC");
            ps.execute();

            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                db.reads++;
                blocks.add(new String[] { rs.getString("location"), Integer.toString(rs.getInt("type")) });
                backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                        + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
            }
            
            rs.close();
        } catch (SQLException ex) {
            com.error("[TAG] Failed to load the protections, {0}", ex, ex.getMessage());
            com.msg(p, messages.updater_loadfailed);
            lock = false;
            t.interrupt();
        }

        CreativeBlockManager manager = CreativeControl.getManager();
        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.updater_loaded, blocks.size(), elapsedTime);
        
        /* Backup Start */
        com.msg(p, messages.backup_generating);
        
        CreativeSQLBackup.backup(backup);

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.backup_done, elapsedTime);
        /* Backup End */
        
        com.msg(p, messages.cleanup_searching);
        double corrupt = 0;
        double done = 0;
        double process = 0;

        HashSet<String> locations = new HashSet<String>();
        for (String[] string : blocks) {
            done++;
            process = ((done / blocks.size()) * 100.0D);

            if (process % 5 == 0) {
                com.msg(p, messages.cleanup_process, done, blocks.size(), corrupt, process);
            }

            try {
                Location loc = CreativeUtil.getLocation(string[0]);
                Block b = loc.getBlock();
                
                int type = b.getTypeId();
                int dbtype = Integer.parseInt(string[1]);

                if (type != dbtype) {
                    com.msg(p, messages.cleanup_corrupted, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), type, dbtype);
                    corrupt++;
                    manager.delBlock(b);
                } else
                if (!manager.isProtectable(b.getWorld(), type)) {
                    com.msg(p, messages.cleanup_corrupted, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), type, dbtype);
                    corrupt++;
                    manager.delBlock(b);
                } else
                if (locations.contains(string[0])) {
                    com.msg(p, messages.cleanup_duplicated, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    corrupt++;
                    manager.delBlock(b);
                } else {
                    locations.add(string[0]);
                }
            } catch (Exception ex) {
                com.error("[TAG] Failed to check the protection: {0}, {1}", ex, string[0], ex.getMessage());
            }
        }

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.cleanup_done, corrupt, blocks.size());
        lock = false;
        t.interrupt();
    }
}