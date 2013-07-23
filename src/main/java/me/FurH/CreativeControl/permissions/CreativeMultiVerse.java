package me.FurH.CreativeControl.permissions;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativeMultiVerse implements CreativePermissionsInterface {

    private MVPermissions ph;

    public CreativeMultiVerse(Plugin plugin) {
        ph = ((MultiverseCore)plugin).getMVPerms();
    }

    @Override
    public boolean hasPerm(Player player, String node) {
        return ph.hasPermission(player, node, false);
    }
}