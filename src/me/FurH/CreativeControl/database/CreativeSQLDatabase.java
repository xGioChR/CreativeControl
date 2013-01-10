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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.monitor.CreativePerformance;
import me.FurH.CreativeControl.monitor.CreativePerformance.Event;
import me.FurH.CreativeControl.util.CreativeCommunicator;

/**
 *
 * @author FurmigaHumana
 */
public final class CreativeSQLDatabase {
    private Map<String, PreparedStatement> cache = new ConcurrentHashMap<String, PreparedStatement>(15000);
    public final Queue<String> queue = new LinkedBlockingQueue<String>();
    public final AtomicBoolean lock = new AtomicBoolean(false);
    public enum Type { MySQL, SQLite; }
    public boolean emergency = false;
    private CreativeControl plugin;
    public Connection connection;
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
    
    public int getReads() {
        return reads;
    }
    
    public int getWrites() {
        return writes;
    }

    public int getSize() {
        return cache.size();
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
        
        if (type == Type.SQLite) {
            connection = getSQLiteConnection();
        } else {
            connection = getMySQLConnection();
        }

        if (connection != null) {
            try {
                connection.setAutoCommit(false);
                connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to commit the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            }
            queue();
            com.log("[TAG] "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connected Successfuly!");

            loadDatabase();
        } else {
            com.log("[TAG] Failed to open the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection!");
        }
    }
    
    public Connection getSQLiteConnection() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Connecting to the SQLite Database...");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] You don't have the required SQLite driver, {0}", ex, ex.getMessage());
            return null;
        }

        File SQLite = new File(plugin.getDataFolder(), "database.db");
        try {
            SQLite.createNewFile();
        } catch (IOException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to create the SQLite file, {0}", ex, ex.getMessage());
        }

        try {
            Connection sqlite = DriverManager.getConnection("jdbc:sqlite:" + SQLite.getAbsolutePath());
            return sqlite;
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed set the SQLite connector, {0}", ex, ex.getMessage());
            return null;
        }
    }
    
    public Connection getMySQLConnection() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Connecting to the MySQL Database...");
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] You don't have the required MySQL driver, {0}", ex, ex.getMessage());
            return null;
        }

        CreativeMainConfig   config = CreativeControl.getMainConfig();
        String url = "jdbc:mysql://" + config.database_host + ":" + config.database_port + "/" + config.database_table +"?autoReconnect=true";
        try {
            Connection mysql = DriverManager.getConnection(url, config.database_user, config.database_pass);
            return mysql;
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed set the MySQL connector, {0}", ex, ex.getMessage());
            return null;
        }
    }
    
    /*
     * Close the connection to the database
     */
    public void close() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        com.log("[TAG] Closing the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection...");
        
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit and commit the database, {0}.", ex, ex.getMessage());
            }

            List<String> queries = new ArrayList<String>();
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

                if (!query.startsWith("blocks")) {
                    executeQuery(query, true, true);
                } else {
                    queries.add(query.substring(6)); //DELETE queries are slow
                }
            }

            if (!queries.isEmpty()) {
                writeToFile(queries);
            }

            try {
                connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit, {0}.", ex, ex.getMessage());
            }

            System.gc();
        }
        
        try {
            cache.clear();
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't close the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection, {0}", ex, ex.getMessage());
        }
        
        if (connection == null) {
            com.log("[TAG] "+(type == Type.SQLite ? "SQLite" : "MySQL")+" Connection closed successfuly!");
        }
    }
    
    /*
     * load flatfiles with what was left of the queue
     */
    public HashSet<String> locations = new HashSet<String>();
    public void loadFiles() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        
        File data = new File(plugin.getDataFolder() + File.separator + "queue", "");
        if (!data.exists()) { return; }
        
        com.log("[TAG] Importing queue from file...");
        for (File file : data.listFiles()) {
            if (file.getName().endsWith(".sql")) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        queue.add("blocks"+line+"");
                        locations.add(line);
                    }

                    reader.close();
                    file.delete();
                } catch (Exception ex) {
                    com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                            "[TAG] Failed to import queue, {0}.", ex, ex.getMessage());
                }
            }
        }
    }

    /*
     * writes what is left of DELETE queries to a flatfile, it is too slow.
     */
    public void writeToFile(List<String> queries) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        
        File data = new File(plugin.getDataFolder() + File.separator + "queue", "");
        if (!data.exists()) { data.mkdirs(); }
        
        data = new File(data.getAbsolutePath(), "queue-"+System.currentTimeMillis()+"-.sql");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                com.log("Failed to create new log file, {0} .", e.getMessage());
            }
        }

        String l = System.getProperty("line.separator");
        try {
            FileWriter fw = new FileWriter(data, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            for (String s : queries) {
                bw.write(s + l);
            }
            
        } catch (IOException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to dump queue, {0}.", ex, ex.getMessage());
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to check if the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" is up, {0}", ex, ex.getMessage());
                return false;
            }
        }
        return true;
    }
    
    /*
     * Load the database
     */
    private void loadDatabase() {
        String auto = (type == Type.MySQL ? "id INT AUTO_INCREMENT, PRIMARY KEY (id)" : "id INTEGER PRIMARY KEY AUTOINCREMENT");
        loadDatabase(auto, connection, true);
    }

    public void loadDatabase(String auto, Connection connection, boolean version) {
        createTables(auto, connection);
        createIndex(connection);
        
        loadFiles();

        if (version) {
            if (getVersion() == -1) {
                executeQuery("INSERT INTO `"+prefix+"internal` (version) VALUES ('"+version+"')", true);
            }
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
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = prepare("SELECT version FROM `"+prefix+"internal`");
            ps.execute();
            
            rs = ps.getResultSet();
            if (rs.next()) {
                reads++;
                ret = rs.getDouble("version");
            }

        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
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
    public void executeQuery(final String query, boolean b) {
        executeQuery(query, b, false);
    }
    
    public void executeQuery(String query, boolean b, boolean agres) {
        if (b) {
            double start = System.currentTimeMillis();
            
            Statement st = null;
            PreparedStatement ps = null;
            
            writes++;
            try {

                if (query.startsWith("blocks")) {
                    String location = query.substring(6);
                    query = "DELETE FROM `"+prefix+"blocks` WHERE location = '"+location+"'";
                }

                if (!agres) {
                    ps = connection.prepareStatement(query);
                    ps.execute();
                } else {
                    st = connection.createStatement();
                    st.execute(query);
                }
            } catch (SQLException ex) {
                CreativeCommunicator com    = CreativeControl.getCommunicator();
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Can't write in the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}, Query: {1}", ex, ex.getMessage(), query);
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
        } else {
            queue.add(query);
        }
    }
    
    /*
     * return a sql object
     */
    public ResultSet getQuery(String query) {
        double start = System.currentTimeMillis();
        
        ResultSet ret = null;
        try {
            PreparedStatement ps = prepare(query);
            ps.execute();

            reads++;
            
            ret = ps.getResultSet();
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}, Query: {1}", ex, ex.getMessage(), query);
            if (!isOk()) { fix(); }
        }
        
        CreativePerformance.update(Event.SQLRead, (System.currentTimeMillis() - start));
        return ret;
    }
    
    public boolean hasTable(String table) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `"+table+"` LIMIT 1");
            ps.execute();
            
            ResultSet rs = ps.getResultSet();

            if (rs.next()) {
                return true;
            }
            
            rs.close();
            ps.close();
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }

    /*
     * Queue
     */
    private void queue() {
        final CreativeMainConfig   config = CreativeControl.getMainConfig();
        final CreativeCommunicator com    = CreativeControl.getCommunicator();
        
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
                            connection.commit();
                            Thread.sleep(sleep);
                            continue;
                        }

                        String query = queue.poll();

                        if (query == null) {
                            connection.commit();
                            Thread.sleep(sleep);
                            continue;
                        }
                        
                        if (queue.size() >= 15000) {
                            if (!emergency) {
                                emergency = true;
                                CreativeEmergency emergency = new CreativeEmergency();
                                emergency.loadup();
                                com.log("[TAG]: Extra queue thread enabled");
                            }
                        }
                        
                        if (count >= limit) {
                            connection.commit();
                            count = 0;
                            Thread.sleep(sleep);
                        }

                        count++;
                        executeQuery(query, true, true);

                        Thread.sleep(each);
                    } catch (Exception ex) { }
                }
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("CreativeControl Database Task");
        t.start();
    }
    
    /*
     * Create tables in the database
     */
    public void createTables(String auto, Connection connection) {
        Statement st = null;
        try {
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't create tables in the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}", ex, ex.getMessage());
            if (!isOk()) { fix(); }
        } finally {
            try {
                if (st != null) { 
                    st.close(); 
                }
            } catch (SQLException ex) { }
        }
    }
    
    /*
     * Create indexes in the database
     */
    public void createIndex(Connection connection) {
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
                if (st != null) { 
                    st.close(); 
                }
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Can't read the "+(type == Type.SQLite ? "SQLite" : "MySQL")+" database, {0}, prepare: {1}", ex, ex.getMessage(), query);
                if (!isOk()) { fix(); }
            }
        }
        return null;
    }
}