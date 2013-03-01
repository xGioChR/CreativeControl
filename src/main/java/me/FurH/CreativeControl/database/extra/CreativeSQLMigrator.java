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
import java.util.ArrayList;
import java.util.List;
import me.FurH.Core.database.CoreSQLDatabase.type;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSQLMigrator implements Runnable {

    private CreativeControl plugin;
    public boolean lock = false;
    private String data;
    private Player p;
    private type type;
    
    private Connection to;

    public CreativeSQLMigrator(CreativeControl plugin, Player p, String data) {
        this.plugin = plugin;
        this.data = data;
        this.p = p;
    }
  
    @Override
    public void run() {
        if (lock) {
            System.out.println("Migrator Locked");
            return;
        }
        
        lock = true;
        long start = System.currentTimeMillis();

        Communicator com = plugin.getCommunicator();
        com.msg(p, "Initializing... ");

        CreativeSQLDatabase db = CreativeControl.getDb();

        if (data.equalsIgnoreCase(">SQLite")) {
            com.msg(p, "Connecting to the SQLite database...");
            try {
                to = db.getSQLiteConnection();
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            }
            type = type.SQLite;
        }

        if (data.equalsIgnoreCase(">MySQL")) {
            com.msg(p, "Connecting to MySQL database...");
            try {
                to = db.getMySQLConnection();
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            }
            type = type.MySQL;
        }

        com.msg(p, "Initializing database...");
        
        try {
            to.setAutoCommit(false);
            to.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to set AutoCommit state, " + ex.getMessage());
        }
        
        db.load(to, type);
        
        try {
            to.commit();
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to set AutoCommit state, " + ex.getMessage());
        }
        
        /* move regions table */
        move_regions();
        
        /* move players survival inventories */
        move_players_survival();
        
        /* move players creative inventories */
        move_players_creative();
        
        /* move players adventurer inventories */
        move_players_adventurer();

        /* move players ids */
        move_players();
        
        /* move server internal data */
        move_internal();
        
        /* move players friend list */
        move_friends();
        
        /* move blocks */
        List<String> tables = new ArrayList<String>();

        for (World world : Bukkit.getWorlds()) {
            tables.add(db.prefix+"blocks_" + world.getName());
        }
        
        for (String table : tables) {
             move_blocks(table);
        }
        
        /* done */
        com.msg(p, "All data moved in {0} ms", (System.currentTimeMillis() - start));

        lock = false;
    }
    
    public void move_blocks(String table) {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long blocks_start = System.currentTimeMillis();
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double blocks_size = 0;
        try {
            blocks_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + blocks_size);

        double blocks_process = 0;
        double blocks_done = 0;
        double blocks_last = 0;

        while (true) {

            blocks_process = ((blocks_done / blocks_size) * 100.0D);

            int row = 0;

            if (blocks_process - blocks_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", blocks_done, blocks_size, String.format("%d", (int) blocks_process));
                blocks_last = blocks_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) blocks_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (owner, x, y, z, type, allowed, time) VALUES (?, ?, ?, ?, ?, ?, ?);");

                    ps2.setInt(1, rs.getInt("owner"));
                    ps2.setInt(2, rs.getInt("x"));
                    ps2.setInt(3, rs.getInt("y"));
                    ps2.setInt(4, rs.getInt("z"));
                    ps2.setInt(5, rs.getInt("type"));
                    ps2.setString(6, rs.getString("allowed"));
                    ps2.setLong(7, rs.getLong("time"));
                    
                    ps2.execute();
                    ps2.close();

                    blocks_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }

        long survival_time = (System.currentTimeMillis() - blocks_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", survival_time);
    }
    
    public void move_regions() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long regions_start = System.currentTimeMillis();

        String table = db.prefix + "regions";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double regions_size = 0;
        try {
            regions_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + regions_size);

        double regions_process = 0;
        double regions_done = 0;
        double regions_last = 0;

        while (true) {

            regions_process = ((regions_done / regions_size) * 100.0D);

            int row = 0;

            if (regions_process - regions_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", regions_done, regions_size, String.format("%d", (int) regions_process));
                regions_last = regions_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) regions_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (name, start, end, type) VALUES (?, ?, ?, ?);");

                    ps2.setString(1, rs.getString("name"));
                    ps2.setString(2, rs.getString("start"));
                    ps2.setString(3, rs.getString("end"));
                    ps2.setString(4, rs.getString("type"));
                    
                    ps2.execute();
                    ps2.close();

                    regions_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long regions_time = (System.currentTimeMillis() - regions_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", regions_time);
    }
    
    public void move_players_survival() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long survival_start = System.currentTimeMillis();

        String table = db.prefix + "players_survival";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double survival_size = 0;
        try {
            survival_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + survival_size);

        double survival_process = 0;
        double survival_done = 0;
        double survival_last = 0;

        while (true) {

            survival_process = ((survival_done / survival_size) * 100.0D);

            int row = 0;

            if (survival_process - survival_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", survival_done, survival_size, String.format("%d", (int) survival_process));
                survival_last = survival_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) survival_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    
                    ps2.execute();
                    ps2.close();

                    survival_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long survival_time = (System.currentTimeMillis() - survival_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", survival_time);
    }
    
    public void move_players_creative() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long creative_start = System.currentTimeMillis();

        String table = db.prefix + "players_creative";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double creative_size = 0;
        try {
            creative_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + creative_size);

        double creative_process = 0;
        double creative_done = 0;
        double creative_last = 0;

        while (true) {

            creative_process = ((creative_done / creative_size) * 100.0D);

            int row = 0;

            if (creative_process - creative_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", creative_done, creative_size, String.format("%d", (int) creative_process));
                creative_last = creative_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) creative_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (player, armor, inventory) VALUES (?, ?, ?);");

                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setString(2, rs.getString("armor"));
                    ps2.setString(3, rs.getString("inventory"));
                    
                    ps2.execute();
                    ps2.close();

                    creative_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long creative_time = (System.currentTimeMillis() - creative_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", creative_time);
    }

    public void move_players_adventurer() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long adventurer_start = System.currentTimeMillis();
        
        String table = db.prefix + "players_adventurer";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double adventurer_size = 0;
        try {
            adventurer_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + adventurer_size);

        double adventurer_process = 0;
        double adventurer_done = 0;
        double adventurer_last = 0;

        while (true) {

            adventurer_process = ((adventurer_done / adventurer_size) * 100.0D);

            int row = 0;

            if (adventurer_process - adventurer_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", adventurer_done, adventurer_size, String.format("%d", (int) adventurer_process));
                adventurer_last = adventurer_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) adventurer_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    
                    ps2.execute();
                    ps2.close();

                    adventurer_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long adventurer_time = (System.currentTimeMillis() - adventurer_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", adventurer_time);
    }
    
    public void move_players() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long players_start = System.currentTimeMillis();
        
        String table = db.prefix + "players";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double players_size = 0;
        try {
            players_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + players_size);

        double players_process = 0;
        double players_done = 0;
        double player_last = 0;

        while (true) {

            players_process = ((players_done / players_size) * 100.0D);

            int row = 0;

            if (players_process - player_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", players_done, players_size, String.format("%d", (int) players_process));
                player_last = players_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) players_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (id, player) VALUES (?, ?);");

                    ps2.setInt(1, rs.getInt("id"));
                    ps2.setString(2, rs.getString("player"));
                    
                    ps2.execute();
                    ps2.close();

                    players_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long players_time = (System.currentTimeMillis() - players_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", players_time);
    }
    
    public void move_internal() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long internal_start = System.currentTimeMillis();
        
        String table = db.prefix + "internal";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double internal_size = 0;
        try {
            internal_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + internal_size);

        double internal_process = 0;
        double internal_done = 0;
        double internal_last = 0;

        while (true) {

            internal_process = ((internal_done / internal_size) * 100.0D);

            int row = 0;

            if (internal_process - internal_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", internal_done, internal_size, String.format("%d", (int) internal_process));
                internal_last = internal_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) internal_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (version) VALUES (?);");

                    ps2.setInt(1, rs.getInt("version"));
                    
                    ps2.execute();
                    ps2.close();

                    internal_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long internal_time = (System.currentTimeMillis() - internal_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", internal_time);
    }
    
    public void move_friends() {
        Communicator com = plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        long friends_start = System.currentTimeMillis();
        
        String table = db.prefix + "friends";
        
        /* move regions table */
        com.msg(p, "Moving table '"+table+"' ...");

        double friends_size = 0;
        try {
            friends_size = db.getTableCount(table);
        } catch (CoreMsgException ex) { } catch (CoreDbException ex) { }

        com.msg(p, "Table size: " + friends_size);

        double friends_process = 0;
        double friends_done = 0;
        double friends_last = 0;

        while (true) {

            friends_process = ((friends_done / friends_size) * 100.0D);

            int row = 0;

            if (friends_process - friends_last >= 5) {
                System.gc();
                com.msg(p, "{0} of ~{1} queries processed, {2}%", friends_done, friends_size, String.format("%d", (int) friends_process));
                friends_last = friends_process;
            }

            try {
                PreparedStatement ps = db.getQuery("SELECT * FROM `"+table+"` LIMIT " + (int) friends_done + ", " + 10000 + ";");
                ResultSet rs = ps.getResultSet();

                while (rs.next()) {
                    PreparedStatement ps2 = to.prepareStatement("INSERT INTO `"+table+"` (player, friends) VALUES (?, ?);");

                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setString(2, rs.getString("friends"));
                    
                    ps2.execute();
                    ps2.close();

                    friends_done++;
                    row++;
                }

                to.commit();

                rs.close();
                ps.close();

                if (row < 10000) {
                    break;
                }
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
                break;
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get statement result set, " + ex.getMessage());
                break;
            }
        }
        
        long friends_time = (System.currentTimeMillis() - friends_start);
        com.msg(p, "Table '" + table + "' moved in {0} ms", friends_time);
    }
}
