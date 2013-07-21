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

package me.FurH.CreativeControl.region;

import org.bukkit.GameMode;
import org.bukkit.Location;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeRegion {
    
    public Location start;
    public Location end;
    public GameMode gamemode;
    public String name;

    public boolean contains(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= start.getBlockX() && x <= end.getBlockX()
                && y >= start.getBlockY() && y <= end.getBlockY()
                && z >= start.getBlockZ() && z <= end.getBlockZ();
    }
}