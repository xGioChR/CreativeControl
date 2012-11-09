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
 
package me.FurH.CreativeControl.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeCommunicator.Type;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMainConfig {
    public boolean         database_mysql    = false;
    public String          database_host     = "localhost";
    public String          database_port     = "3306";
    public String          database_user     = "root";
    public String          database_pass     = "123";
    public String          database_table    = "minecraft";
    public String          database_prefix   = "cc_";

    public long            queue_time        = 200;
    public int             queue_force       = 20;
    public int             queue_delay       = 5;

    public int             cache_capacity    = 15000;
    public int             cache_precache    = 10000;

    public boolean         config_single     = false;
    public boolean         config_conflict   = false;
    public boolean         config_friend     = false;
    
    public boolean         updater_enabled   = true;
        
    public boolean         perm_ophas        = false;
    
    public boolean         selection_usewe   = false;
    public int             selection_tool    = 378;
    
    public boolean         events_move       = false;
    public boolean         events_misc       = false;
    
    public boolean         data_inventory    = true;
    public boolean         data_status       = true;
    public boolean         data_teleport     = false;

    public boolean         com_quiet         = false;
    public boolean         com_debugcons     = false;
    public boolean         com_debugstack    = true;
    
    public void load() {
        database_mysql   = getBoolean("Database.MySQL");
        database_host    = getString("Database.host");
        database_port    = getString("Database.port");
        database_user    = getString("Database.user");
        database_pass    = getString("Database.pass");
        database_table   = getString("Database.database");
        database_prefix  = getString("Database.prefix");
        
        queue_time       = getLong("Queue.time");
        queue_force      = getInteger("Queue.force");
        queue_delay      = getInteger("Queue.delay");
        
        cache_capacity   = getInteger("Cache.MaxCapacity");
        cache_precache   = getInteger("Cache.PreCache");
        
        config_single    = getBoolean("Configurations.Single");
        config_conflict  = getBoolean("Configurations.Conflict");
        config_friend    = getBoolean("Configurations.FriendSystem");
        
        updater_enabled  = getBoolean("Updater.Enabled");
        
        perm_ophas       = getBoolean("Permissions.OpHasPerm");
        
        selection_usewe   = getBoolean("Selection.UseWorldEdit");
        selection_tool    = getInteger("Selection.Tool");
        
        events_move      = getBoolean("Events.PlayerMove");
        events_misc      = getBoolean("Events.MiscProtection");
        
        data_inventory   = getBoolean("PlayerData.Inventory");
        data_status      = getBoolean("PlayerData.Status");
        data_teleport    = getBoolean("PlayerData.Teleport");
        
        com_quiet        = getBoolean("Communicator.Quiet");
        com_debugcons    = getBoolean("Communicator.Debug.Console");
        com_debugstack   = getBoolean("Communicator.Debug.Stack");
    }
    
    /*
     * return a Boolean from the settings file
     */
    private boolean getBoolean(String node) {
        return Boolean.parseBoolean(getSetting(node));
    }

    /*
     * return a String from the settings file
     */
    private String getString(String node) {
        return getSetting(node);
    }

    /*
     * return a Long from the settings file
     */
    private long getLong(String node) {
        return Long.parseLong(getSetting(node));
    }
    
    /*
     * return a Integer from the settings file
     */
    private int getInteger(String node) {
        return Integer.parseInt(getSetting(node));
    }
        
    /*
     * return an Object from the Settings file
     */
    private String getSetting(String node) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder(), "settings.yml");
        if (!dir.exists()) { CreativeUtil.ccFile(plugin.getResource("settings.yml"), dir); }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            if (!config.contains(node)) {
                InputStream resource = plugin.getResource("settings.yml");
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.get(node));
                    com.log("[TAG] Settings file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log("[TAG] Can't get setting node: {0}, contact the developer.", Type.SEVERE, node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error("[TAG] Can't update the settings file: {0}", ex, ex.getMessage());
                }
            }
        } catch (IOException e) {
            com.error("[TAG] Can't load the settings file: {0}", e, e.getMessage());
        } catch (InvalidConfigurationException ex) {
            com.error("[TAG] Can't load the settings file: {0}", ex, ex.getMessage());
            com.log("[TAG] You have a broken node in your settings file at: {0}", node);
        }
        
        String value = config.getString(node);
        if (value == null || "".equals(value)) {
            com.log(CreativeControl.tag + " You have a missing setting node at: {0}", Type.SEVERE, node);
            value = node;
        }
        
        return value;
    }
}