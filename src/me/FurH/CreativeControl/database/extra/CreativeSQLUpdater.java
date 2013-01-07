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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSQLUpdater {
    public boolean lock = false;
    private Player p;
    
    public void loadup() {
        CreativeSQLDatabase db = CreativeControl.getDb();
        if (db.hasTable("CreativeControl") || db.hasTable("creativecontrol")) {
            start();
        }
    }
    
    public CreativeSQLUpdater(Player p) {
        this.p = p;
    }

    public void start() {
        lock = true;

        long startTimer = System.currentTimeMillis();
        long elapsedTime = 0;

        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeMessages messages = CreativeControl.getMessages();
        
        com.msg(p, messages.updater_loading);

        HashSet<String> locations = new HashSet<String>();
        HashSet<String[]> blocks = new HashSet<String[]>();

        CreativeSQLDatabase db = CreativeControl.getDb();
        try {
            ResultSet rs = db.getQuery("SELECT * FROM `CreativeControl` ORDER BY `id` DESC");

            while (rs.next()) {
                db.reads++;
                blocks.add(new String[] { rs.getInt("id")+"" , rs.getString("owner"), rs.getString("world"), rs.getInt("x")+"", rs.getInt("y")+"", rs.getInt("z")+"", rs.getInt("type")+"", rs.getString("allowed"), rs.getString("time")+""});
            }

            rs.close();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to load protections, {0}", ex, ex.getMessage());
            com.msg(p, messages.updater_loadfailed);
            lock = false;
        }

        try {
            ResultSet rs = db.getQuery("SELECT location, type FROM `"+db.prefix+"blocks` ORDER BY `id` DESC");

            while (rs.next()) {
                db.reads++;
                locations.add(rs.getString("location"));
            }
            rs.close();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to load protections, {0}", ex, ex.getMessage());
            com.msg(p, messages.updater_loadfailed);
            lock = false;
        }

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.updater_loaded, blocks.size(), elapsedTime);

        double done = 0;
        double process = 0;
        int skip = 0;
        int sucess = 0;
        
        double last = 0;
        
        try {
            db.connection.setAutoCommit(false);
            db.connection.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit and commit the database, {0}.", ex, ex.getMessage());
        }

        for (String[] string : blocks) {
            done++;
            process = ((done / blocks.size()) * 100.0D);

            if (process - last >= 5) {
                com.msg(p, messages.updater_process, done, blocks.size(), skip, String.format("%d", (int) process));
                last = process;
            }

            try {
                String owner = string[1];
                String world = string[2];
                int x = Integer.parseInt(string[3]);
                int y = Integer.parseInt(string[4]);
                int z = Integer.parseInt(string[5]);
                int type = Integer.parseInt(string[6]);
                String allowed = null;
                if (string[7] != null) {
                    allowed = string[7];
                }
                String time = string[8];

                String StringLoc = world + ":" + x + ":" + y + ":" + z;
                if (!locations.contains(StringLoc)) {
                    locations.add(StringLoc);
                    sucess++;
                    db.executeQuery("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES ('"+owner+"', '"+StringLoc+"', '"+type+"', '"+allowed+"', '"+time+"')", true);
                } else {
                    //com.msg(p, messages.updater_duplicated, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    skip++;
                }
            } catch (Exception ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed on update the database, {0} .", ex, ex.getMessage());
                com.msg(p, messages.updater_checkfailed);
                lock = false;
            }
        }

        db.executeQuery("UPDATE `"+db.prefix+"internal` SET version = '"+db.version+"'", true);
        db.executeQuery("ALTER TABLE `CreativeControl` RENAME TO `"+db.prefix+"old`", true);

        try {
            db.connection.setAutoCommit(true);
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit, {0}.", ex, ex.getMessage());
        }
        
        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.updater_done, sucess, elapsedTime);
        lock = false;
    }
}
