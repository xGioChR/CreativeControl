package me.FurH.CreativeControl.permissions;

import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativePermissions {

    private CreativePermissionsInterface handler;
    private Permission vault;

    public void setup() {
        Communicator com = CreativeControl.plugin.getCommunicator();
        PluginManager pm = Bukkit.getPluginManager();

        Plugin plugin = pm.getPlugin("GroupManager");
        if (plugin != null && plugin.isEnabled()) {
            handler = new CreativeGroupManager(plugin);
            com.log("[TAG] GroupManager hooked as permissions plugin");
        }

        plugin = pm.getPlugin("PermissionsEx");
        if (plugin != null && plugin.isEnabled()) {
            handler = new CreativePexManager();
            com.log("[TAG] PermissionsEx hooked as permissions plugin");
        }

        plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                vault = permissionProvider.getProvider();
                com.log("[TAG] Vault hooked as permissions plugin");
            }
        }
    }
    
    public boolean hasPerm(Player player, String node) {

        if (handler != null) {
            return handler.hasPerm(player, node);
        }

        return player.hasPermission(node);
    }
    
    public Permission getVault() {
        return vault;
    }
}
