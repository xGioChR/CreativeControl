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

import me.FurH.Core.player.PlayerUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.region.CreativeRegion;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMoveListener implements Listener {
    private GameMode was; // TODO: Deprecated

    /*
     * Player Move Region Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (e.isCancelled()) { return; }
        
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() &&
            e.getTo().getBlockY() == e.getFrom().getBlockY() &&
            e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
            return;
        }
                
        Player p = e.getPlayer();
        World world = p.getWorld();
        Location loc = p.getLocation();

        CreativeWorldNodes config = CreativeControl.getWorldNodes(world);

        if (config.world_exclude) { return; }

        CreativeMessages     messages   = CreativeControl.getMessages();
        CreativeControl      plugin     = CreativeControl.getPlugin();
        Communicator         com        = plugin.getCommunicator();
        
        CreativeRegion region = CreativeControl.getRegioner().getRegion(loc);
        if (region != null) {
            World w = region.world;
            
            if (w != world) { 
                return; 
            }

            GameMode type = region.gamemode;
            if (type == GameMode.CREATIVE) {
                if (!plugin.hasPerm(p, "Region.Keep.Survival")) {
                    if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                        com.msg(p, messages.region_welcome_creative, region.name);
                        p.setGameMode(GameMode.CREATIVE);
                        was = type;
                    }
                }
            } else
            if (type == GameMode.SURVIVAL) {
                if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
                    if (!plugin.hasPerm(p, "Region.Keep.Creative")) {
                        PlayerUtils.toSafeLocation(p);
                        com.msg(p, messages.region_welcome_survival, region.name);
                        p.setGameMode(GameMode.SURVIVAL);
                        was = type;
                    }
                }
            }
        } else {
            if (!plugin.hasPerm(p, "World.Keep")) {
                if (config.world_creative) {
                    if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                        if (was == GameMode.CREATIVE) {
                            com.msg(p, messages.region_farewell_creative);
                            p.setGameMode(GameMode.CREATIVE);
                        } else
                        if (was == GameMode.SURVIVAL) {
                            com.msg(p, messages.region_farewell_survival);
                            p.setGameMode(GameMode.CREATIVE);
                        } else {
                            p.setGameMode(GameMode.CREATIVE);
                        }
                    }
                } else
                if (!config.world_creative) {
                    if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
                        PlayerUtils.toSafeLocation(p);
                        if (was == GameMode.CREATIVE) {
                            com.msg(p, messages.region_farewell_creative);
                            p.setGameMode(GameMode.SURVIVAL);
                        } else
                        if (was == GameMode.SURVIVAL) {
                            com.msg(p, messages.region_farewell_survival);
                            p.setGameMode(GameMode.SURVIVAL);
                        } else {
                            p.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                }
            }
        }
    }
}