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
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import net.minecraft.server.v1_5_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R1.CraftWorld;
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
        final WorldServer worldServer = ((CraftWorld)world).getHandle();
        
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            
                            int id = worldServer.getTypeId(x, y, z);
                            if (id == 0) { continue; }
                            
                            CreativeBlockManager manager = CreativeControl.getManager();
                            manager.protect(p.getName(), world, x, y, z, id);
                        }
                    }
                }
                interrupt();
            }
        };
        t.setName("CreativeControl WorldEdit Hook");
        t.setPriority(1);
        t.start();
    }

    public void delBlocks(Selection select, final Player p) {
        if (select == null) { return; }
        final Location max = select.getMaximumPoint();
        final Location min = select.getMinimumPoint();
        final World world = max.getWorld();
        final WorldServer worldServer = ((CraftWorld)world).getHandle();
        
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            
                            int id = worldServer.getTypeId(x, y, z);
                            if (id == 0) { continue; }
                            
                            CreativeBlockManager manager = CreativeControl.getManager();
                            manager.unprotect(world, x, y, z, id);
                        }
                    }
                }
                interrupt();
            }
        };
        t.setName("CreativeControl WorldEdit Hook");
        t.setPriority(1);
        t.start();
    }
}