package me.FurH.CreativeControl.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.ProtectInventoryEvent;
import fr.xephi.authme.events.SessionEvent;

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
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSession(SessionEvent e) {
        if (!e.isCancelled()) {
            
            Player player = Bukkit.getPlayerExact(e.getPlayerAuth().getNickname());
            
            if (player == null) {
                return;
            }
            
            CreativeUniversalLogin event = new CreativeUniversalLogin(player);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
}