package me.FurH.CreativeControl.integration;

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.data.CreativePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import uk.org.whoami.authme.events.RestoreInventoryEvent;
import uk.org.whoami.authme.events.StoreInventoryEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class AuthMe implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryStore(StoreInventoryEvent e) {
        if (e.isCancelled()) { return; }

        CreativeControl       plugin      = CreativeControl.getPlugin();
        CreativePlayerData    data        = CreativeControl.getPlayerData();
        final Player p = e.getPlayer();

        data.save(p, p.getGameMode());
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                onInventoryReset(p);
            }
        }, 3L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryRestore(RestoreInventoryEvent e) {
        if (e.isCancelled()) { return; }

        CreativeControl       plugin      = CreativeControl.getPlugin();
        final CreativePlayerData    data        = CreativeControl.getPlayerData();
        final Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                data.restore(p, p.getGameMode());
            }
        }, 2L);
    }
    
    public void onInventoryReset(Player p) {

        p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.AIR, 1)});
        p.getInventory().setContents(new ItemStack[] { new ItemStack(Material.AIR, 1)});
        
    }
}