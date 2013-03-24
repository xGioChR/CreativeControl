/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
