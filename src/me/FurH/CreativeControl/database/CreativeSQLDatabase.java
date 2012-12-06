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

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public final class CreativeSQLDatabase {
    private Map<String, PreparedStatement> cache = new ConcurrentHashMap<String, PreparedStatement>(15000);
    private final Queue<String> queue = new LinkedBlockingQueue();
    private final Lock lock = new ReentrantLock();
    public enum Type { MySQL, SQLite; }
    private CreativeControl plugin;
    private Connection connection;
    public String prefix = "cc_";
    private boolean typeBoolean;
    public double version = 1;
    public int writes = 0;
    public int reads = 0;
    public int fix = 0;
    public Type type;
        
    public int getQueue() {
        return queue.size();
    }
    
    public void clear() {
        cache.clear();
    }

    public CreativeSQLDatabase(CreativeControl plugin, boolean b) {
        CreativeMainConfig   config = CreativeControl.getMainConfig();
        this.typeBoolean = config.database_mysql;
        this.prefix = config.database_prefix;
        this.plugin = plugin;

        if (typeBoolean) {
            this.type = Type.MySQL;
        } else {
            this.type = Type.SQLite;
        }

        if (b) { open(); } else {
            close();
        }
    }
    
    /*
     * Open the connection to the database
     */
    public void open() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Connecting to the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Database...");
        if (type == Type.SQLite) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                com.error("[TAG] You don't have the required SQLite driver, {0}", ex, ex.getMessage());
                return;
            }

            File SQLite = new File(plugin.getDataFolder(), "database.db");
            try {
                SQLite.createNewFile();
            } catch (IOException ex) {
                com.error("[TAG] Failed to create the SQLite file, {0}", ex, ex.getMessage());
            }
            
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + SQLite.getAbsolutePath());
            } catch (SQLException ex) {
                com.error("[TAG] Failed set the SQLite connector, {0}", ex, ex.getMessage());
                return;
            }
        } else {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                com.error("[TAG] You don't have the required MySQL driver, {0}", ex, ex.getMessage());
                return;
            }

            CreativeMainConfig   config = CreativeControl.getMainConfig();
            String url = "jdbc:mysql://" + config.database_host + ":" + config.database_port + "/" + config.database_table +"?autoReconnect=true";
            try {
                connection = DriverManager.getConnection(url, config.database_user, config.database_pass);
            } catch (SQLException ex) {
                com.error("[TAG] Failed set the MySQL connector, {0}", ex, ex.getMessage());
                return;
            }
        }

        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                com.error("[TAG] Failed to commit the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            }
            queue();
            com.log("[TAG] "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connected Successfuly!");
            
            loadDatabase();
        } else {
            com.log("[TAG] Failed to open the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection!");
        }
    }
    
    /*
     * Close the connection to the database
     */
    public void close() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Closing the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection...");
        if (!queue.isEmpty()) {
            com.log("[TAG] Queue isn't empty! Running the remaining queue...");
            
            double process = 0;
            double done = 0;
            double total = queue.size();
            while (!queue.isEmpty()) {
                done++;
                String query = queue.poll();
                if (query == null) { continue; }
                
                process = ((done / total) * 100.0D);

                com.log("[TAG] Processed {0} of {1} querys, {2}%", done, total, process);
                
                executeQuery(query, true);
            }
        }

        try {
            cache.clear();
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            com.error("[TAG] Can't close the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection, {0}", ex, ex.getMessage());
        }
        
        if (connection == null) {
            com.log("[TAG] "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection closed successfuly!");
        }
    }

    /*
     * Fix the database connection
     */
    public void fix() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        if (fix == 3) {
            com.log("[TAG] Failed to fix the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" connection after 3 times, shutting down...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        } else {
            fix++;
            com.log("[TAG] The "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database is down, fixing...");
            close();
            open();
            if (isOk()) {
                com.log("[TAG] "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database is up and running!");
                fix = 0;
            } else {
                com.log("[TAG] Failed to fix the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" connection!");
            }
        }
    }

    /*
     * Check if the connection is ok
     */
    public boolean isOk() {
        if (connection == null) {
            return false;
        } else {
            try {
                if (connection.isClosed()) { 
                    return false; 
                } else
                if (connection.isReadOnly()) {
                    return false;
                }
            } catch (SQLException ex) {
                CreativeCommunicator com    = CreativeControl.getCommunicator();
                com.error("[TAG] Failed to check if the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" is up, {0}", ex, ex.getMessage());
                return false;
            }
        }
        return true;
    }
    
    /*
     * Load the database
     */
    private void loadDatabase() {
        createTables();
        createIndex();

        if (getVersion() == -1) {
            executeQuery("INSERT INTO `"+prefix+"internal` (version) VALUES ('"+version+"')", true);
        }
    }

    /*
     * Execute a query
     */
    public void executeQuery(String query) {
        executeQuery(query, false);
    }
    
    /*
     * return the database version
     */
    public double getVersion() {
        double ret = -1;
        try {
            PreparedStatement ps = prepare("SELECT version FROM `"+prefix+"internal`");
            ps.execute();

            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                reads++;
                ret = rs.getDouble("version");
            }
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            if (!isOk()) { fix(); }
        }
        return ret;
    }
    
    /*
     * Execute a query
     */
    public void executeQuery(final String query, boolean b) {
        if (b) {
            writes++;
            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ps.execute();
            } catch (SQLException ex) {
                CreativeCommunicator com    = CreativeControl.getCommunicator();
                com.error("[TAG] Can't write in the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}, Query: {1}", ex, ex.getMessage(), query);
            }
        } else {
            queue.add(query);
        }
    }
    
    /*
     * return a sql object
     */
    public ResultSet getQuery(String query) {
        ResultSet ret = null;
        try {
            PreparedStatement ps = prepare(query);
            ps.execute();

            reads++;
            
            ret = ps.getResultSet();
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            if (!isOk()) { fix(); }
        }
        return ret;
    }
    
    public boolean hasTable(String table) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `"+table+"` LIMIT '1'");
            ps.execute();
            
            ResultSet rs = ps.getResultSet();
            
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    /*
     * Queue
     */
    private void queue() {
        final CreativeMainConfig   config = CreativeControl.getMainConfig();
        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (queue.isEmpty() || !lock.tryLock()) { return; }
                long start = System.currentTimeMillis();
                int count = 0;
                while ((!queue.isEmpty()) && ((System.currentTimeMillis() - start < config.queue_time) || (count < config.queue_force))) {
                    String query = queue.poll();
                    if (query == null) { continue; }
                    executeQuery(query, true);
                    count++;
                }
                lock.unlock();
            }
        }, config.queue_delay * 20, config.queue_delay * 20);
    }
    
    /*
     * Create tables in the database
     */
    private void createTables() {
        Statement st = null;
        try {
            String auto = (type == Type.MySQL ? "id INT AUTO_INCREMENT, PRIMARY KEY (id)" : "id INTEGER PRIMARY KEY AUTOINCREMENT");
            st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_adventurer` ("+auto+", player VARCHAR(255), health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_survival` ("+auto+", player VARCHAR(255), health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"blocks` ("+auto+", owner VARCHAR(255), location VARCHAR(255), type INT, allowed VARCHAR(255), time VARCHAR(255));");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"regions` ("+auto+", name VARCHAR(255), start VARCHAR(255), end VARCHAR(255), type VARCHAR(255));");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_creative` ("+auto+", player VARCHAR(255), armor TEXT, inventory TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"friends` ("+auto+", player VARCHAR(255), friends TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"internal` (version INT);");
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] Can't create tables in the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            if (!isOk()) { fix(); }
        } finally {
            try {
                if (st != null) { st.close(); }
            } catch (SQLException ex) { }
        }
    }
    
    /*
     * Create indexes in the database
     */
    private void createIndex() {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate("CREATE INDEX `"+prefix+"adventurer` ON `"+prefix+"players_adventurer` (player)");
            st.executeUpdate("CREATE INDEX `"+prefix+"survival` ON `"+prefix+"players_survival` (player)");
            st.executeUpdate("CREATE INDEX `"+prefix+"creative` ON `"+prefix+"players_creative` (player)");
            st.executeUpdate("CREATE INDEX `"+prefix+"friendlist` ON `"+prefix+"friends` (player)");
            st.executeUpdate("CREATE INDEX `"+prefix+"version` ON `"+prefix+"internal` (version)");
            st.executeUpdate("CREATE INDEX `"+prefix+"block` ON `"+prefix+"blocks` (owner, location)");
            st.executeUpdate("CREATE INDEX `"+prefix+"location` ON `"+prefix+"blocks` (location)");
            st.executeUpdate("CREATE INDEX `"+prefix+"owner` ON `"+prefix+"blocks` (owner)");
            st.executeUpdate("CREATE INDEX `"+prefix+"type` ON `"+prefix+"blocks` (type)");
            st.executeUpdate("CREATE INDEX `"+prefix+"allowed` ON `"+prefix+"blocks` (allowed)");
        } catch (SQLException ex) {
        } finally {
            try {
                if (st != null) { st.close(); }
            } catch (SQLException ex) { }
        }
    }
    
    /*
     * Prepare and add the statement to the cache
     */
    public PreparedStatement prepare(String query) {
        if (cache.containsKey(query)) { 
            return cache.get(query); 
        } else {
            try {
                
                PreparedStatement ps = connection.prepareStatement(query);
                cache.put(query, ps);
                
                return ps;
            } catch (SQLException ex) {
                CreativeCommunicator com    = CreativeControl.getCommunicator();
                com.error("[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}, prepare: {1}", ex, ex.getMessage(), query);
                if (!isOk()) { fix(); }
            }
        }
        return null;
    }
}