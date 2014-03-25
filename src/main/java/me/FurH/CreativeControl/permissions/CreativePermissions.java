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

package me.FurH.CreativeControl.permissions;

import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePermissions {

    private Permission vault;
    private CreativePermissionsInterface handler;

    public void setup() {
        Communicator com = CreativeControl.plugin.getCommunicator();
        PluginManager pm = Bukkit.getPluginManager();

        Plugin plugin = pm.getPlugin("Vault");
        if (plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                vault = permissionProvider.getProvider();
                com.log("[TAG] Vault hooked as permissions plugin");
            }
        }

        plugin = pm.getPlugin("Multiverse-Core");
        if (plugin != null && plugin.isEnabled()) {
            handler = new CreativeMultiVerse(plugin);
            com.log("[TAG] MultiVerse hooked as permissions bridge!");
            return;
        }
        
        plugin = pm.getPlugin("GroupManager");
        if (plugin != null && plugin.isEnabled()) {
            handler = new CreativeGroupManager(plugin);
            com.log("[TAG] GroupManager hooked as permissions plugin");
        }
    }

    public boolean hasPerm(Player player, String node) {
        CreativeMainConfig config = CreativeControl.getMainConfig();

        if (player.isOp() && config.perm_ophas) {
            return true;
        }

        if (handler != null) {
            return handler.hasPerm(player, node);
        }
        
        return player.hasPermission(node);
    }
    
    public Permission getVault() {
        return vault;
    }
}
