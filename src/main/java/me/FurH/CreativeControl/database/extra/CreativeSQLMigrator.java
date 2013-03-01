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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public final class CreativeSQLMigrator implements Runnable {
    private CreativeControl plugin;
    private String data;
    private Player p;
    public boolean lock = false;

    private Connection mysql;
    private Connection sqlite;
    
    private Connection from;
    private Connection to;
    
    public CreativeSQLMigrator(CreativeControl plugin, Player p, String data) {
        this.plugin = plugin;
        this.data = data;
        this.p = p;
        open();
    }
    
    public void open() {
        /*CreativeSQLDatabase db = CreativeControl.getDb();
        if (db.type == Type.MySQL) {
            mysql = db.connection;
        } else {
            mysql = db.getMySQLConnection();
        }
        
        if (db.type == Type.SQLite) {
            sqlite = db.connection;
        } else {
            sqlite = db.getSQLiteConnection();
        }

        db.loadDatabase("id INTEGER PRIMARY KEY AUTOINCREMENT", sqlite, false);
        db.loadDatabase("id INT AUTO_INCREMENT, PRIMARY KEY (id)", mysql, false);*/
    }

    @Override
    public void run() {
        lock = true;

        long startTimer = System.currentTimeMillis();
        long elapsedTime = 0;
        
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeMessages messages = CreativeControl.getMessages();

        if (data.equalsIgnoreCase("MySQL>SQLite") || data.equalsIgnoreCase("SQLite<MySQL")) {
            com.msg(p, messages.migrator_mysqlsqlite);
            from = mysql;
            to = sqlite;
        } else
        if (data.equalsIgnoreCase("MySQL<SQLite") || data.equalsIgnoreCase("SQLite>MySQL")) {
            com.msg(p, messages.migrator_sqlitemysql);
            from = sqlite;
            to = mysql;
        }
        
        CreativeSQLDatabase db = CreativeControl.getDb();

        PreparedStatement ps;
        double skip = 0;
        double sucess = 0;

        try {
            System.gc();
            ps = from.prepareStatement("SELECT * FROM `"+db.prefix+"blocks`");
            ps.execute();
            
            double total = 0;
            
            ResultSet counter = ps.getResultSet();
            while (counter.next()) {
                total++;
            }
            counter.close();

            ps.execute();
            ResultSet rs = ps.getResultSet();

            elapsedTime = (System.currentTimeMillis() - startTimer);
            com.msg(p, messages.migrator_loaded, total, elapsedTime);

            double done = 0;
            double process = 0;

            double last = 0;

            try {
                to.setAutoCommit(false);
                to.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit and commit the database, {0}", ex.getMessage());
            }
            
            while (rs.next()) {
                done++; //db.writes++;

                process = ((done / total) * 100.0D);

                if (process - last > 5) {
                    System.gc();
                    com.msg(p, messages.migrator_converted, done, total, String.format("%d", (int) process));
                    last = process;
                }

                Statement st = null;
                try {
                    st = to.createStatement();
                    st.execute("INSERT INTO `"+db.prefix+"blocks` (owner, location, type, allowed, time) VALUES "
                        + "('"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getLong("time")+"')");
                    sucess++;
                } catch (SQLException ex) {
                    com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                            "[TAG] Can't write in the database, {0}", ex.getMessage());
                } finally {
                    if (st != null) {
                        try {
                            st.close();
                        } catch (Exception ex) { }
                    }
                }

            }
            rs.close();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't read the MySQL database, {0}", ex.getMessage());
        }

        try {
            to.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set AutoCommit, {0}", ex.getMessage());
        }
        
        System.gc();

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(p, messages.migrator_done, sucess, skip, elapsedTime);
        lock = false;
    }
}
