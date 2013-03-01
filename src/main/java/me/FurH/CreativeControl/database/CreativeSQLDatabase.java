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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeLRUCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.monitor.CreativePerformance;
import me.FurH.CreativeControl.monitor.CreativePerformance.Event;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 */
public final class CreativeSQLDatabase {
    private Map<String, PreparedStatement> cache = Collections.synchronizedMap(new CreativeLRUCache<String, PreparedStatement>(15000));
    private Queue<String> queue = new LinkedBlockingQueue<String>();
    private AtomicBoolean lock = new AtomicBoolean(false);
    public enum type { MySQL, SQLite, H2; }
    private Connection connection;
    public String prefix = "cc_";
    public type type;

    public double version = 1;
    private int writes = 0;
    private int reads = 0;
    private int fix = 0;
    
    public int getQueue() {
        return queue.size();
    }
    
    public int getReads() {
        return reads;
    }
    
    public int getWrites() {
        return writes;
    }

    /*
     * Open the connection to the database
     */
    public void connect() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();

        com.log("[TAG] Connecting to the {0} Database...", type);
        CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.database_mysql) {
            type = type.MySQL;
        } else {
            type = type.SQLite;
        }

        if (type == type.SQLite) {
            connection = getSQLiteConnection();
        } else {
            connection = getMySQLConnection();
        }

        if (connection != null) {
            try {
                connection.setAutoCommit(false);
                connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to commit the {0} database, {1}", type, ex.getMessage());
            }
            
            queue();
            garbage();
            
            com.log("[TAG] {0} Connected Successfuly!", type);

            load();
        }
    }

    public Connection getSQLiteConnection() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] You don't have the required {0} driver, {1}", type, ex.getMessage());
        }
        
        File SQLite = new File(CreativeControl.getPlugin().getDataFolder(), "database.db");
        try {
            SQLite.createNewFile();
        } catch (IOException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to create the SQLite file, {0}", ex.getMessage());
        }
        
        try {
            return connection = DriverManager.getConnection("jdbc:sqlite:" + SQLite.getAbsolutePath());
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed open the {0} connection, {1}", type, ex.getMessage());
        }

        return null;
    }
    
    public Connection getMySQLConnection() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] You don't have the required {0} driver, {1}", type, ex.getMessage());
        }

        CreativeMainConfig   config = CreativeControl.getMainConfig();
        String url = "jdbc:mysql://" + config.database_host + ":" + config.database_port + "/" + config.database_table +"?autoReconnect=true";

        try {
            return connection = DriverManager.getConnection(url, config.database_user, config.database_pass);
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed open the {0} connection, {1}", type, ex.getMessage());
        }

        return null;
    }
    
    public void disconnect(boolean fix) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Closing the {0} connection...", type);

        if (!fix) {
            lock.set(true);

            if (!queue.isEmpty()) {
                com.log("[TAG] Queue isn't empty! Running the remaining queue...");

                double process = 0;
                double done = 0;
                double total = queue.size();

                double last = 0;

                try {
                    connection.commit();
                } catch (SQLException ex) {
                    com.error(Thread.currentThread(), ex, "[TAG] Failed to commit the database, {0}", ex.getMessage());
                }

                while (!queue.isEmpty()) {
                    done++;

                    String query = queue.poll();
                    if (query == null) { continue; }

                    process = ((done / total) * 100.0D);

                    if (process - last > 5) {
                        System.gc();
                        com.log("[TAG] Processed {0} of {1} queries, {2}%", done, total, String.format("%d", (int) process));
                        last = process;
                    }

                    execute(query);
                }

                try {
                    connection.commit();
                } catch (SQLException ex) {
                    com.error(Thread.currentThread(), ex, "[TAG] Failed to commit the database, {0}", ex.getMessage());
                }

                System.gc();
            }
        }

        try {
            if (connection != null) {
                connection.commit();
                
                connection.close();

                if (connection.isClosed()) {
                    com.log("[TAG] {0} connection closed successfuly!", type);
                }
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Can't close the {0} connection, {1}", type, ex.getMessage());
        }
    }
    
    public void fix() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        if (fix >= 3) {
            com.log("[TAG] Failed to fix the {0} connection after 3 attempts, shutting down...");
            Bukkit.getPluginManager().disablePlugin(CreativeControl.getPlugin());
            return;
        }
        
        fix++;
        com.log("[TAG] The {0} database is down, reconnecting...", type);

        disconnect(true);
        connect();

        if (isOk()) {
            com.log("[TAG] {0} database is now up and running!", type);
            fix = 0;
        } else {
            com.log("[TAG] Failed to fix the {0} connection!, attempt {1} of 3.", type, fix);
        }
    }
    
    public boolean isOk() {
        if (connection == null) {
            return false;
        }
        
        try {
            if (connection.isClosed()) {
                return false;
            } else 
            if (connection.isReadOnly()) {
                return false;
            } else if (type == type.MySQL) {
                if (connection.isValid(30)) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Failed to check if the {0} is up, {1}", type, ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    public void load() {
        Statement st = null;
        try {
            st = connection.createStatement();
            
            /* player id table */
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players` (id INT AUTO_INCREMENT, PRIMARY KEY (id), player VARCHAR(255));");
            try {
                st.executeUpdate("CREATE INDEX `"+prefix+"names` ON `"+prefix+"players` (player);");
            } catch (SQLException ex) { }
                        
            /* players inventory */
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_adventurer` (id INT AUTO_INCREMENT, PRIMARY KEY (id), player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_survival` (id INT AUTO_INCREMENT, PRIMARY KEY (id), player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"players_creative` (id INT AUTO_INCREMENT, PRIMARY KEY (id), player INT, armor TEXT, inventory TEXT);");

            /* block data */
            for (World world : Bukkit.getWorlds()) {
                load(world.getName());
            }

            /* region data */
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"regions` (id INT AUTO_INCREMENT, PRIMARY KEY (id), name VARCHAR(255), start VARCHAR(255), end VARCHAR(255), type VARCHAR(255));");
            
            /* friends data */
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"friends` (id INT AUTO_INCREMENT, PRIMARY KEY (id), player INT, friends TEXT);");
            
            /* internal data */
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"internal` (version INT);");
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't create tables in the {0} database, {1}", type, ex.getMessage());
            if (!isOk()) { fix(); }
        } finally {
            try {
                if (st != null) { 
                    st.close(); 
                }
            } catch (SQLException ex) { }
        }
    }

    public void load(String world) {
        Statement st = null;
        try {
            st = connection.createStatement();
            /* load the world blocks table */
            //st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"blocks_"+world+"` (id INT AUTO_INCREMENT, PRIMARY KEY (id), owner INT, cx INT, cz INT, x INT, y INT, z INT, type INT, allowed VARCHAR(255), time VARCHAR(255));");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `"+prefix+"blocks_"+world+"` (owner INT, x INT, y INT, z INT, type INT, allowed VARCHAR(255), time BIGINT);");
            /* create the index */
            try {
                st.executeUpdate("CREATE INDEX `"+prefix+"block_"+world+"` ON `"+prefix+"blocks_"+world+"` (x, z, y);");
                st.executeUpdate("CREATE INDEX `"+prefix+"type_"+world+"` ON `"+prefix+"blocks_"+world+"` (type);");
                //st.executeUpdate("CREATE INDEX `"+prefix+"chunk_"+world+"` ON `"+prefix+"blocks_"+world+"` (cx, cz)");
                st.executeUpdate("CREATE INDEX `"+prefix+"owner_"+world+"` ON `"+prefix+"blocks_"+world+"` (owner);");
            } catch (SQLException ex) { }
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't create world table in the {0} database, {1}", type, ex.getMessage());
            if (!isOk()) { fix(); }
        } finally {
            try {
                if (st != null) { 
                    st.close(); 
                }
            } catch (SQLException ex) { }
        }
    }
    
    public double getVersion() {
        double ret = -1;
        
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = prepare("SELECT version FROM `"+prefix+"internal`;");            
            rs = ps.executeQuery();

            if (rs.next()) {
                reads++;
                ret = rs.getDouble("version");
            }

        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't read the {0} database, {1}", type, ex.getMessage());
            if (!isOk()) { fix(); }
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }
        return ret;
    }
    
    /*
     * Execute a query
     */
    public void queue(String query) {
        queue.add(query);
    }

    public void execute(String query, Object...objects) {
        double start = System.currentTimeMillis();
        
        if (objects != null && objects.length > 0) {
            query = MessageFormat.format(query, objects);
        }

        Statement st = null;
        PreparedStatement ps = null;

        writes++;
        try {
            ps = connection.prepareStatement(query);
            ps.execute();
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't write in the {0} database, {1}, Query: {2}", type, ex.getMessage(), query);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
        }

        CreativePerformance.update(Event.SQLWrite, (System.currentTimeMillis() - start));
    }
    
    /*
     * return a sql object
     */
    public PreparedStatement getQuery(String query, Object...objects) {
        double start = System.currentTimeMillis();
        
        if (objects != null && objects.length > 0) {
            query = MessageFormat.format(query, objects);
        }

        try {
            PreparedStatement ps = prepare(query);

            try {
                ps.execute();
            } catch (SQLException ex) {
                ps = connection.prepareStatement(query);
                ps.execute();
            }
            
            reads++;
            
            return ps;
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't read the {0} database, {1}, Query: {2}", type, ex.getMessage(), query);
            if (!isOk()) { fix(); }
        }
        
        CreativePerformance.update(Event.SQLRead, (System.currentTimeMillis() - start));
        return null;
    }
    
    public boolean hasTable(String table) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = prepare("SELECT * FROM `"+table+"` LIMIT 1;");
            rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException ex) {
            return false;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
        }
    }

    private void queue() {
        final CreativeMainConfig   config = CreativeControl.getMainConfig();

        final long each = config.queue_each;
        final int limit = config.queue_count;
        final int sleep = config.queue_sleep;

        Thread t = new Thread() {
            @Override
            public void run() {
                int count = 0;
                while (!lock.get()) {
                    try {
                        if (queue.isEmpty()) {
                            commit();
                            sleep(sleep);
                            continue;
                        }

                        String query = queue.poll();

                        if (query == null) {
                            commit();
                            sleep(sleep);
                            continue;
                        }

                        if (count >= limit) {
                            commit();
                            count = 0;
                            sleep(sleep);
                        }

                        count++;
                        execute(query);

                        sleep(each);
                    } catch (Exception ex) { }
                }
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("CreativeControl Database Task");
        t.start();
    }
    
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread(), ex, "[TAG] Can't create world table in the {0} database, {1}", type, ex.getMessage());
            if (!isOk()) { fix(); }
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
                com.error(Thread.currentThread(), ex, "[TAG] Can't read the {0} database, {1}, prepare: {2}", type, ex.getMessage(), query);
                if (!isOk()) { fix(); }
            }
        }
        return null;
    }
    
    public void garbage() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(CreativeControl.getPlugin(), new Runnable() {
            final List<String> keys = new ArrayList<String>(cache.keySet());
            @Override
            public void run() {

                for (String query : keys) {
                    PreparedStatement ps = cache.get(query);

                    try {
                        ps.close();
                    } catch (SQLException ex) { }
                    
                    ps = null;
                }

                cache.clear();
                keys.clear();
            }
        }, 900 * 20, 900 * 20);
    }
}