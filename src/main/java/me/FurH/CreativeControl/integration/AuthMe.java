package me.FurH.CreativeControl.integration;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import uk.org.whoami.authme.events.LoginEvent;
import uk.org.whoami.authme.events.ProtectInventoryEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class AuthMe implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryProtect(ProtectInventoryEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLogin(LoginEvent e) {
        CreativeUniversalLogin event = new CreativeUniversalLogin(e.getPlayer());
        Bukkit.getPluginManager().callEvent(event);
    }
}