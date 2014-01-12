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

package me.FurH.CreativeControl.listener;

import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeWorldListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onWorldInit(WorldInitEvent e) {
        loadWorld(e.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onWorldLoad(WorldLoadEvent e) {
        loadWorld(e.getWorld());
    }

    public void loadWorld(World world) {
        CreativeMainConfig   main     = CreativeControl.getMainConfig();

        if (!main.config_single) {
            CreativeControl.getWorldConfig().load(world);
        }

        CreativeControl.getDb().load(null, world.getName(), null);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onStructureGrown(StructureGrowEvent e) {
        if (e.isCancelled()) { return; }

        CreativeWorldNodes      config      = CreativeControl.getWorldNodes(e.getWorld());
        CreativeControl         plugin      = CreativeControl.getPlugin();
        Communicator            com         = plugin.getCommunicator();
        CreativeMessages        messages    = CreativeControl.getMessages();

        if (config.world_exclude) {
            return;
        }

        Player p = e.getPlayer();
        
        if (p == null) {
            return;
        }
        
        if (!e.isFromBonemeal()) {
            return;
        }

        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (config.prevent_bonemeal) {
            if (!plugin.hasPerm(p, "Preventions.Bonemeal")) {
                com.msg(p, messages.mainode_restricted);
                e.setCancelled(true);
            }
        }
    }
}