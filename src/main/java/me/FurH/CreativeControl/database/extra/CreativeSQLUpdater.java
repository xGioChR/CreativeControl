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
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
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
        int sucess = 0;
        
        CreativeSQLDatabase db = CreativeControl.getDb();
        PreparedStatement ps = null;
        PreparedStatement p1 = null;
        ResultSet rs = null;
        ResultSet counter = null;
        try {
            System.gc();
            double total = 0;
            
            p1 = db.getQuery("SELECT * FROM `CreativeControl` ORDER BY `id` DESC");
            counter = p1.getResultSet();
            
            while (counter.next()) {
                total++;
            }

            ps = db.getQuery("SELECT * FROM `CreativeControl` ORDER BY `id` DESC");
            rs = ps.getResultSet();

            elapsedTime = (System.currentTimeMillis() - startTimer);
            com.msg(p, messages.updater_loaded, total, elapsedTime);
            
            double done = 0;
            double process = 0;
            int skip = 0;
            double last = 0;

            /*try {
                db.connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit and commit the database, {0}", ex.getMessage());
            }*/

            while (rs.next()) {
                done++; //db.reads++;
                process = ((done / total) * 100.0D);

                if (process - last >= 5) {
                    System.gc();
                    com.msg(p, messages.updater_process, done, total, skip, String.format("%d", (int) process));
                    last = process;
                }

                try {
                    String owner = rs.getString("owner");
                    String world = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    int type = rs.getInt("type");
                    String allowed = null;
                    if (rs.getString("allowed") != null) {
                        allowed = rs.getString("allowed");
                    }
                    String time = rs.getString("time");

                    String StringLoc = world + ":" + x + ":" + y + ":" + z;
                    sucess++;
                    db.execute("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES ('"+owner+"', '"+StringLoc+"', '"+type+"', '"+allowed+"', '"+time+"')");
                } catch (Exception ex) {
                    com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                            "[TAG] Failed on update the database, {0}", ex.getMessage());
                    com.msg(p, messages.updater_checkfailed);
                    lock = false;
                }
            }

            rs.close();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to load protections, {0}", ex.getMessage());
            com.msg(p, messages.updater_loadfailed);
            lock = false;
        } finally {
            if (counter != null) {
                try {
                    counter.close();
                } catch (SQLException ex) { }
            }
            if (p1 != null) {
                try {
                    p1.close();
                } catch (SQLException ex) { }
            }
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

        db.execute("UPDATE `"+db.prefix+"internal` SET version = '"+db.version+"'");
        db.execute("ALTER TABLE `CreativeControl` RENAME TO `"+db.prefix+"old`");

        /*try {
            db.connection.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit, {0}", ex.getMessage());
        }*/
        
        System.gc();
        
        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.updater_done, sucess, elapsedTime);
        lock = false;
    }
}