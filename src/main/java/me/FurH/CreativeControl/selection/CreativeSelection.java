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

package me.FurH.CreativeControl.selection;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSelection {
    private Location start, end;
    private Vector vector;

    public CreativeSelection(Location start, Location end) {
        int sx = start.getBlockX();
        int ex = end.getBlockX();
        if (sx > ex) {
            int i = sx;
            sx = ex;
            ex = i;
        }

        int sy = start.getBlockY();
        int ey = end.getBlockY();
        if (sy > ey) {
            int i = sy;
            sy = ey;
            ey = i;
        }

        int sz = start.getBlockZ();
        int ez = end.getBlockZ();
        if (sz > ez) {
            int i = sz;
            sz = ez;
            ez = i;
        }

        this.start = new Location(start.getWorld(), sx, sy, sz);
        this.end = new Location(start.getWorld(), ex, ey, ez);
    }

    public Vector getVector() {
        vector = new Vector(Math.abs(end.getBlockX() - start.getBlockX()), Math.abs(end.getBlockY() - start.getBlockY()), Math.abs(end.getBlockZ() - start.getBlockZ()));
        return vector;
    }
    
    public Location getStart() {
        return start;
    }
    
    public Location getEnd() {
        return end;
    }
    
    public int getArea() {
        Location min = start;
        Location max = end;

        return (int)((max.getX() - min.getX() + 1) *
                     (max.getY() - min.getY() + 1) *
                     (max.getZ() - min.getZ() + 1));
    }
}
