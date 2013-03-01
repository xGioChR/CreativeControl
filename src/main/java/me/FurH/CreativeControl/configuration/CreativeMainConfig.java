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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMainConfig {
    private Map<String, CreativeFileInfo> cache = new HashMap<String, CreativeFileInfo>();
    
    public CreativeMainConfig() {
        CreativeWorldConfig.setConfig(this);
    }
    
    public boolean         database_mysql    = false;
    public String          database_host     = "localhost";
    public String          database_port     = "3306";
    public String          database_user     = "root";
    public String          database_pass     = "123";
    public String          database_table    = "minecraft";
    public String          database_prefix   = "cc_";

    public boolean         perm_enabled      = false;
    public String          perm_from         = "MemberSurvival";
    public String          perm_to           = "MemberCreative";

    public long            queue_each        = 100;
    public int             queue_count       = 300;
    public int             queue_sleep       = 5000;

    public int             cache_capacity    = 15000;
    public int             cache_precache    = 10000;
    public boolean         cache_dynamic     = false;

    public boolean         config_single     = false;
    public boolean         config_conflict   = false;
    public boolean         config_friend     = false;
    public boolean         config_auto       = true;

    public boolean         updater_enabled   = true;
        
    public boolean         perm_ophas        = false;
    
    public boolean         selection_usewe   = false;
    public int             selection_tool    = 369;
    
    public boolean         events_move       = false;
    public boolean         events_misc       = false;
    
    public boolean         data_inventory    = true;
    public boolean         data_status       = true;
    public boolean         data_teleport     = false;
    public boolean         data_survival     = false;
    
    public boolean         perfm_monitor     = true;

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
        
        perm_enabled     = getBoolean("Permissions.Enabled");
        perm_from        = getString("Permissions.MoveFrom");
        perm_to          = getString("Permissions.MoveTo");
    
        queue_each       = getLong("Queue.each");
        queue_count      = getInteger("Queue.count");
        queue_sleep      = getInteger("Queue.sleep");
        
        cache_capacity   = getInteger("Cache.MaxCapacity");
        cache_precache   = getInteger("Cache.PreCache");
        cache_dynamic    = getBoolean("Cache.DynamicBlockType");
        
        config_single    = getBoolean("Configurations.Single");
        config_conflict  = getBoolean("Configurations.Conflict");
        config_friend    = getBoolean("Configurations.FriendSystem");
        config_auto      = getBoolean("Configurations.AutoWrite");
        
        updater_enabled  = getBoolean("Updater.Enabled");
        
        perm_ophas       = getBoolean("Permissions.OpHasPerm");
        
        selection_usewe  = getBoolean("Selection.UseWorldEdit");
        selection_tool   = getInteger("Selection.Tool");
        
        events_move      = getBoolean("Events.PlayerMove");
        events_misc      = getBoolean("Events.MiscProtection");
        
        data_inventory   = getBoolean("PlayerData.Inventory");
        data_status      = getBoolean("PlayerData.Status");
        data_teleport    = getBoolean("PlayerData.Teleport");
        data_survival    = getBoolean("PlayerData.SetSurvival");
        
        perfm_monitor    = getBoolean("Monitor.Enabled");

        com_quiet        = getBoolean("Communicator.Quiet");
        com_debugcons    = getBoolean("Debug.Console");
        com_debugstack   = getBoolean("Debug.Stack");
    }

    public void updateConfig() {
        CreativeControl plugin = CreativeControl.getPlugin();
        File file = null;
        InputStream source = null;

        for (String key : cache.keySet()) {
            CreativeFileInfo info = cache.get(key);
            if (info.needUpdate) {
                file = new File(info.dir);
                source = plugin.getResource(info.fileName);
                updateConfig(file, source);
            }
        }
    }

    public void updateConfig(File file, InputStream source) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        BufferedWriter writer = null;

        try {
            Scanner scanner = new Scanner(source);

            List<String> lines = new ArrayList<String>();
            String section = "";

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                boolean comment = false;

                if (line.replaceAll(" ", "").startsWith("#")) {
                    comment = true;
                }

                if (!comment && line.endsWith(":")) {
                    section = line.replaceAll(" ", "").replaceAll(":", "");
                }

                if (!comment && !section.isEmpty() && line.contains(":") && !line.endsWith(":")) {
                    String node = section + "." + line.substring(0, line.lastIndexOf(':')).replaceAll(" ", "");

                    HashMap<String, String> nodes = cache.get(file.getName()).values;

                    String value = nodes.get(node);

                    if (value.contains(".")) {
                        value = value.replaceAll("\\.", "{DOT}");
                    }

                    if (value == null) {
                        try {
                            YamlConfiguration rsconfig = new YamlConfiguration();
                            rsconfig.load(source);
                            node = rsconfig.getString(node);
                        } catch (Exception ex) { }
                    }
                    
                    if (value == null) {
                        com.log("[TAG] Can't update setting node: {0}, contact the developer.", node);
                        continue;
                    }
                    
                    if ("".equals(value) || value.isEmpty()) {
                        value = "''";
                    }

                    line = node + ": " + value;

                    String space = "";
                    String[] split = line.split("\\.");

                    while (space.length() < split.length - 1) {
                        space += "  ";
                    }

                    line = space += split[ split.length - 1 ];
                }

                lines.add(line);
            }
            scanner.close();

            Path path = Paths.get(file.getAbsolutePath());
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

            for (String line : lines) {

                if (line.contains("{DOT}")) {
                    writer.write(line.replaceAll("\\{DOT}", "."));
                } else {
                    writer.write(line);
                }
  
                writer.newLine();
            }

        } catch (IOException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to update settings file: {0}", ex.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to update settings file[2]: {0}", ex.getMessage());
            }
        }
    }
    
    /*
     * return a Boolean from the settings file
     */
    protected boolean getBoolean(World w, String node) {
        return Boolean.parseBoolean(getSetting(node, w));
    }
    
    /*
     * return a Integer from the settings file
     */
    protected int getInteger(World w, String node) {
        return Integer.parseInt(getSetting(node, w));
    }
    
    /*
     * return a Boolean from the settings file
     */
    protected boolean getBoolean(String node) {
        return Boolean.parseBoolean(getSetting(node));
    }

    /*
     * return a String from the settings file
     */
    protected String getString(String node) {
        return getSetting(node);
    }

    /*
     * return a Long from the settings file
     */
    protected long getLong(String node) {
        return Long.parseLong(getSetting(node));
    }
    
    /*
     * return a Integer from the settings file
     */
    protected int getInteger(String node) {
        return Integer.parseInt(getSetting(node));
    }
    
    /*
     * return a String HashSet from the settings file
     */
    protected HashSet<String> getStringList(World w, String node) {
        return CreativeUtil.toStringHashSet(getSetting(node, w).replaceAll(" ", ""), ",");
    }

    /*
     * return a Integer HashSet from the settings file
     */
    protected HashSet<Integer> getIntegerList(World w, String node) {
        return CreativeUtil.toIntegerHashSet(getSetting(node, w).replaceAll(" ", ""), ",");
    }
        
    /*
     * return an Object from the Settings file
     */
    private String getSetting(String node, World w) {
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder() + File.separator + "worlds", w != null ? w.getName() + ".yml" : "world.yml");
        if (!dir.exists()) { CreativeUtil.ccFile(plugin.getResource("world.yml"), dir); }
        
        return getSetting(dir, node);
    }
    
    private String getSetting(String node) {
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder(), "settings.yml");
        if (!dir.exists()) { CreativeUtil.ccFile(plugin.getResource("settings.yml"), dir); }
        
        return getSetting(dir, node);
    }
    
    private String getSetting(File dir, String node) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        CreativeFileInfo info = new CreativeFileInfo();
        if (cache.containsKey(dir.getName())) {
            info = cache.get(dir.getName());
        }
        
        info.fileName = dir.getName();
        info.dir = dir.getAbsolutePath();

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            
            if (!config.contains(node)) {
                info.needUpdate = true; /* file update required */

                InputStream resource = plugin.getResource(dir.getName());
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.get(node));
                    com.log("[TAG] Settings file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log("[TAG] Can't get setting node: {0}, contact the developer.", node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error(Thread.currentThread(), ex, "[TAG] Can't update the settings file: {0}, node: {1}", ex.getMessage(), node);
                }
            }
        } catch (IOException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Can't load the settings file: {0}, node {1}", ex.getMessage(), node);
        } catch (InvalidConfigurationException ex) {
            com.log("[TAG] You have a broken node in your settings file at: {0}, {1}", node, ex.getMessage());
            info.needUpdate = true; /* broken node */
        }
        
        String value = config.getString(node);
        if (value == null) {
            com.log("[TAG] You have a missing setting node at: {0}", node);
            value = node;
            info.needUpdate = true; /* broken node */
        }

        info.values.put(node, value);
        cache.put(dir.getName(), info);

        return value;
    }
    
    private class CreativeFileInfo {
        public HashMap<String, String> values = new HashMap<String, String>();
        public boolean needUpdate = false;
        public String fileName = null;
        public String dir = null;
    }
}