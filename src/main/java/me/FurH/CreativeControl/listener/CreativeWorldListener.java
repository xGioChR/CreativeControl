/*
 * Copyright (C) 2011-2012 FurmigaHumana.  All rights reserved.
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

package me.FurH.CreativeControl.listener;

import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeWorldListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        CreativeMainConfig   main     = CreativeControl.getMainConfig();
        if (!main.config_single) {
            CreativeControl.getWorldConfig().load(e.getWorld());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onStructureGrown(StructureGrowEvent e) {
        if (e.isCancelled()) { return; }

        CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getWorld());

        if (config.world_exclude) { return; }
        
        Player p = e.getPlayer();

        if (e.isFromBonemeal()) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if (config.prevent_bonemeal) {
                    CreativeControl      plugin   = CreativeControl.getPlugin();
                    if (!plugin.hasPerm(p, "Preventions.Bonemeal")) {
                        Communicator         com      = plugin.getCommunicator();
                        CreativeMessages     messages = CreativeControl.getMessages();
                        com.msg(p, messages.player_cantuse);
                        for (BlockState b : e.getBlocks()) {
                            b.getBlock().setType(Material.AIR); 
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
