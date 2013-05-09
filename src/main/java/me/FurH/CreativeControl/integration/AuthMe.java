package me.FurH.CreativeControl.integration;

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.data.CreativePlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import uk.org.whoami.authme.events.ResetInventoryEvent;
import uk.org.whoami.authme.events.RestoreInventoryEvent;
import uk.org.whoami.authme.events.StoreInventoryEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class AuthMe implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryRestore(RestoreInventoryEvent e) {
        if (e.isCancelled()) { return; }

        CreativePlayerData    data        = CreativeControl.getPlayerData();
        Player p = e.getPlayer();

        System.out.println("RESTORE: " + p.getGameMode().name());
        
        data.save(p, p.getGameMode());
        onInventoryReset(p);
        
        e.setCancelled(true);
    }
    
    public void onInventoryReset(Player p) {

        p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.AIR, 1)});
        p.getInventory().setContents(new ItemStack[] { new ItemStack(Material.AIR, 1)});
        
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryStore(StoreInventoryEvent e) {
        if (e.isCancelled()) { return; }
        
        CreativePlayerData    data        = CreativeControl.getPlayerData();
        Player p = e.getPlayer();
        
        System.out.println("STORE: " + p.getGameMode().name());
        
        data.restore(p, p.getGameMode());
        
        e.setCancelled(true);
    }
}
