/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
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

import me.FurH.Core.CorePlugin;
import me.FurH.Core.configuration.Configuration;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.inventory.InventoryStack;
import me.FurH.CreativeControl.CreativeControl;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMainConfig extends Configuration {

    public CreativeMainConfig(CorePlugin plugin) {
        super(plugin);
    }
    
    public String          database_type     = "SQLite";
    public String          database_host     = "localhost";
    public String          database_port     = "3306";
    public String          database_user     = "root";
    public String          database_pass     = "123";
    public String          database_table    = "minecraft";
    public String          database_prefix   = "crcr_";

    public boolean         perm_enabled      = false;
    public boolean         perm_keep         = false;
    public String          perm_creative     = "CreativeGroup";
    public boolean         perm_ophas        = true;

    public int             queue_threadds    = 1;
    public double          queue_speed       = 0.1;

    public int             cache_capacity    = 15000;
    public int             cache_precache    = 10000;

    public boolean         config_single     = false;
    public boolean         config_conflict   = false;
    public boolean         config_friend     = false;

    public boolean         updater_enabled   = true;

    public boolean         selection_usewe   = false;
    public int             selection_tool    = 369;
    
    public boolean         events_move       = false;
    public boolean         events_misc       = false;

    public boolean         data_inventory    = true;
    public boolean         data_status       = true;
    public boolean         data_teleport     = false;
    public boolean         data_survival     = false;
    public boolean         data_glitch       = false;
    
    public ItemStack       armor_helmet      = null;
    public ItemStack       armor_chest       = null;
    public ItemStack       armor_leggs       = null;
    public ItemStack       armor_boots       = null;
    
    public boolean         com_quiet         = false;
    public boolean         com_debugcons     = false;
    public boolean         com_debugstack    = true;

    public void load() {
        
        database_type    = getString("Database.Type");
        database_host    = getString("Database.host");
        database_port    = getString("Database.port");
        database_user    = getString("Database.user");
        database_pass    = getString("Database.pass");
        database_table   = getString("Database.database");
        database_prefix  = getString("Database.prefix");

        perm_enabled     = getBoolean("Permissions.ChangeGroups");
        perm_creative    = getString("Permissions.CreativeGroup");
        perm_keep        = getBoolean("Permissions.KeepCurrentGroup");
        perm_ophas       = getBoolean("Permissions.OpHasPerm");

        queue_threadds   = getInteger("Queue.Threads");
        queue_speed      = getDouble("Queue.Speed");
        
        cache_capacity   = getInteger("Cache.MaxCapacity");
        cache_precache   = getInteger("Cache.PreCache");
        
        config_single    = getBoolean("Configurations.Single");
        config_conflict  = getBoolean("Configurations.Conflict");
        config_friend    = getBoolean("Configurations.FriendSystem");
        
        updater_enabled  = getBoolean("Updater.Enabled");
                
        selection_usewe  = getBoolean("Selection.UseWorldEdit");
        
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            selection_usewe = false;
        }
        
        selection_tool   = getInteger("Selection.Tool");
        
        events_move      = getBoolean("Events.PlayerMove");
        events_misc      = getBoolean("Events.MiscProtection");

        data_inventory   = getBoolean("PlayerData.Inventory");
        data_status      = getBoolean("PlayerData.Status");
        data_teleport    = getBoolean("PlayerData.Teleport");
        data_survival    = getBoolean("PlayerData.SetSurvival");
        data_glitch      = getBoolean("PlayerData.FallGlitch");
        
        try {

            armor_helmet     = InventoryStack.getItemStackFromString(getString("CreativeArmor.Helmet"));
            armor_chest      = InventoryStack.getItemStackFromString(getString("CreativeArmor.Chestplate"));
            armor_leggs      = InventoryStack.getItemStackFromString(getString("CreativeArmor.Leggings"));
            armor_boots      = InventoryStack.getItemStackFromString(getString("CreativeArmor.Boots"));

        } catch (CoreException ex) {
            CreativeControl.getPlugin().error(ex, "Failed to handle the creative armor settings!");
        }

        com_quiet        = getBoolean("Communicator.Quiet");
        com_debugcons    = getBoolean("Debug.Console");
        com_debugstack   = getBoolean("Debug.Stack");
        
        updateConfig();
    }
}