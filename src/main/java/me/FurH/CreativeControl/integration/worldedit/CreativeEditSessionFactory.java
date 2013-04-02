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

package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bags.BlockBag;
import me.FurH.CreativeControl.CreativeControl;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeEditSessionFactory extends EditSessionFactory {
    
    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, player);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, blockBag, player);
    }

    public static void setup() {
        Bukkit.getScheduler().runTaskLater(CreativeControl.getPlugin(), new Runnable() {
            @Override
            public void run() {
                WorldEdit.getInstance().setEditSessionFactory(new CreativeEditSessionFactory());
            }
        }, 1L);
    }
}
