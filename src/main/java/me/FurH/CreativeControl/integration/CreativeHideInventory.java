package me.FurH.CreativeControl.integration;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.internals.InternalManager;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativeHideInventory implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.data_hide) {
            try {
                InternalManager.getEntityPlayer(e.getPlayer(), true).hideInventory();
            } catch (CoreException ex) {
                CreativeControl.getPlugin().error(ex, "Failed to hide '" + e.getPlayer().getName() + "' inventory!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAuth(CreativeUniversalLogin e) {
        CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.data_hide) {
            try {
                InternalManager.getEntityPlayer(e.getPlayer(), true).unHideInventory();
            } catch (CoreException ex) {
                CreativeControl.getPlugin().error(ex, "Failed to restore '" + e.getPlayer().getName() + "' inventory!");
            }
        }
    }
}