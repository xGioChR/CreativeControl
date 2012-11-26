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

import java.util.ArrayList;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeRegion {
    private HashSet<CreativeRegion> areas = new HashSet<CreativeRegion>();
    private Location start, end;
    private gmType type;
    private String name;
    private World world;
    
    public CreativeRegion(CreativeControl plugin) { }
    
    public HashSet<CreativeRegion> get() {
        return areas;
    }
    
    public void add(String name, Location start, Location end, String type) {
        areas.add(new CreativeRegion(type, name, start, end));
    }

    public void remove(String name) {
        for (CreativeRegion region: areas) {
            if (region.name.equalsIgnoreCase(name)) {
                areas.remove(region);
                break;
            }
            break; //TODO: NEEDED?
        }
    }

    public CreativeRegion(Location loc) {
        for (CreativeRegion region : areas) {
            if (region.contains(loc)) {
                this.start = region.getStart();
                this.end = region.getEnd(); 
                this.type = region.type;
                this.name = region.name;
                this.world = region.world;
                break;
            }
            break; //TODO: NEEDED?
        }
    }

    public CreativeRegion(String type, String name, Location start, Location end) {
        this.start = start;
        this.end = end;
        
        if (type.equals("CREATIVE")) {
            this.type = gmType.CREATIVE;
        }
        if (type.equals("SURVIVAL")) {
            this.type = gmType.SURVIVAL;
        }

        this.name = name;
        this.world = start.getWorld();
    }
    
    public Location getStart() {
        return start;
    }
    
    public Location getEnd() {
        return end;
    }
    
    public gmType getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public World getWorld() {
        return world;
    }

    public boolean contains(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        
        Location min = start;
        Location max = end;
        
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }

    public enum gmType { CREATIVE, SURVIVAL; }
}
