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

package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.bukkit.selections.Selection;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeWorldEditHook {
    
    public Selection getSelection(Player p) {
        CreativeControl plugin = CreativeControl.getPlugin();
        Selection select = plugin.getWorldEdit().getSelection(p);
        return select;
    }
    
    public void saveBlocks(Selection select, final Player p) {
        if (select == null) { return; }
        final Location max = select.getMaximumPoint();
        final Location min = select.getMinimumPoint();
        final World world = max.getWorld();
        final CreativeWorldNodes nodes = CreativeWorldConfig.get(world);
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            Location loc = new Location(world, x, y, z);
                            Block b = world.getBlockAt(loc);
                            CreativeBlockManager manager = CreativeControl.getManager();
                            manager.addBlock(p.getName(), b, nodes.block_nodrop);
                        }
                    }
                }
                interrupt();
            }
        };
        t.setPriority(4);
        t.start();
    }

    public void delBlocks(Selection select, final Player p) {
        if (select == null) { return; }
        final Location max = select.getMaximumPoint();
        final Location min = select.getMinimumPoint();
        final World world = max.getWorld();
        final CreativeWorldNodes nodes = CreativeWorldConfig.get(world);
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            Location loc = new Location(world, x, y, z);
                            Block b = world.getBlockAt(loc);
                            CreativeBlockManager manager = CreativeControl.getManager();
                            manager.addBlock(p.getName(), b, nodes.block_nodrop);
                        }
                    }
                }
                interrupt();
            }
        };
        t.setPriority(4);
        t.start();
    }
}
