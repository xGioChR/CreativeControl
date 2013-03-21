package me.FurH.CreativeControl.permissions;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeGroupManager implements CreativePermissionsInterface {

    private GroupManager groupManager;

    public CreativeGroupManager(Plugin manager) {
        this.groupManager = (GroupManager) manager;
    }

    @Override
    public boolean hasPerm(Player player, String node) {
        AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(player);

        if (handler == null) {
            return player.hasPermission(node);
        }

        return handler.has(player, node);
    }
}
