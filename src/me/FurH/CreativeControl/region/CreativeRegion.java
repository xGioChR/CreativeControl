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

package me.FurH.CreativeControl.region;

import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeRegion {
    public Location start;
    public Location end;
    public gmType type;
    public String name;
    public World world;

    public boolean contains(Location loc) {
        double x = loc.getBlockX();
        double y = loc.getBlockY();
        double z = loc.getBlockZ();

        return x >= start.getBlockX() && x <= end.getBlockX()
                && y >= start.getBlockY() && y <= end.getBlockY()
                && z >= start.getBlockZ() && z <= end.getBlockZ();
    }

    public enum gmType { CREATIVE, SURVIVAL; }
}