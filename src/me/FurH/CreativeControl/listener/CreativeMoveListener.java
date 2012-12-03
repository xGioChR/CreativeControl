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

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.region.CreativeRegion;
import me.FurH.CreativeControl.region.CreativeRegion.gmType;
import me.FurH.CreativeControl.util.CreativeCommunicator;
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
    private gmType was;

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

        CreativeWorldNodes config = CreativeWorldConfig.get(world);

        if (config.world_exclude) { return; }

        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeMessages     messages   = CreativeControl.getMessages();
        CreativeControl      plugin     = CreativeControl.getPlugin();
        
        CreativeRegion region = new CreativeRegion(loc);
        if (region != null) {
            World w = region.getWorld();
            if (w != world) { return; }
            gmType type = region.getType();
            if (type == gmType.CREATIVE) {
                if (!plugin.hasPerm(p, "Region.Keep.Survival")) {
                    if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                        com.msg(p, messages.region_cwelcome);
                        p.setGameMode(GameMode.CREATIVE);
                        was = type;
                    }
                }
            } else
            if (type == CreativeRegion.gmType.SURVIVAL) {
                if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
                    if (!plugin.hasPerm(p, "Region.Keep.Creative")) {
                        CreativeUtil.getFloor(p);
                        com.msg(p, messages.region_swelcome);
                        p.setGameMode(GameMode.SURVIVAL);
                        was = type;
                    }
                }
            }
        } else {
            if (!plugin.hasPerm(p, "World.Keep")) {
                if (config.world_creative) {
                    if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                        if (was == gmType.CREATIVE) {
                            com.msg(p, messages.region_cleave);
                            p.setGameMode(GameMode.CREATIVE);
                        } else
                        if (was == gmType.SURVIVAL) {
                            com.msg(p, messages.region_sleave);
                            p.setGameMode(GameMode.CREATIVE);
                        } else {
                            p.setGameMode(GameMode.CREATIVE);
                        }
                    }
                } else
                if (!config.world_creative) {
                    if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
                        CreativeUtil.getFloor(p);
                        if (was == gmType.CREATIVE) {
                            com.msg(p, messages.region_cleave);
                            p.setGameMode(GameMode.SURVIVAL);
                        } else
                        if (was == gmType.SURVIVAL) {
                            com.msg(p, messages.region_sleave);
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