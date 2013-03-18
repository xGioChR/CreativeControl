package me.FurH.CreativeControl.permissions;

import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativePexManager implements CreativePermissionsInterface {
    
    private PermissionManager handler;

    @Override
    public boolean hasPerm(Player player, String node) {
        
        if (handler != null) {
            return handler.has(player, node);
        }
        
        return player.hasPermission(node);
    }
}
