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

package me.FurH.CreativeControl.util;

import java.util.HashSet;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeUtil {
 
    /*
     * return true if the first line of the sign is listed as a economy sign
     */
    public static boolean isEconomySign(org.bukkit.block.Sign sign) {
        String line1 = sign.getLine(0).replaceAll(" ", "_");

        CreativeWorldNodes config = CreativeControl.getWorldNodes(sign.getWorld());
        
        if (line1.contains("§")) {
            line1 = line1.replaceAll("§([0-9a-fk-or])", "");
        }

        if (config.black_sign.contains(line1.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * return a HashSet of the List contends
     */
    public static HashSet<String> toStringHashSet(String string, String split) {

        try {
            return CollectionUtils.toStringHashSet(string, split);
        } catch (CoreMsgException ex) {
            CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }

        return null;
    }
    
    /*
     * return a HashSet of the List contends
     */
    public static HashSet<Integer> toIntegerHashSet(String string, String split) {
        
        try {
            return CollectionUtils.toIntegerHashSet(string, split);
        } catch (CoreMsgException ex) {
            CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        
        return null;
    }
    
    /*
     * return a Integer
     */
    public static int toInteger(String str) {

        try {
            return CollectionUtils.toInteger(str);
        } catch (CoreMsgException ex) {
            CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        
        return 0;
    }
    
    public static double toDouble(String str) {

        try {
            return CollectionUtils.toDouble(str);
        } catch (CoreMsgException ex) {
            CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        
        return 0;
    }
    
    public static void getFloor(Player player) {
        Location loc = player.getLocation();
        Block b1 = loc.getBlock().getRelative(BlockFace.DOWN);

        if (b1.getType() != Material.AIR) {
            return;
        }

        int limit = 256;
        while (b1.getType() != Material.AIR && limit > 0) {
            b1 = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            limit--;
        }

        Location newloc = new Location(loc.getWorld(), loc.getX(), b1.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        player.teleport(newloc);
    }
}