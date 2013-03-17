package me.FurH.CreativeControl.integration;

import me.FurH.CreativeControl.CreativeControl;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.mcsg.survivalgames.api.PlayerJoinArenaEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class SurvivalGames implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onJoinArenaEvent(PlayerJoinArenaEvent e) {

        CreativeControl plugin = CreativeControl.getPlugin();
        Player p = e.getPlayer();

        if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
            if (!plugin.hasPerm(p, "Integration.SurvivalGames")) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
}
