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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.region.CreativeRegion.gmType;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeRegionManager {    
    private HashSet<CreativeRegion> areas = new HashSet<CreativeRegion>();
    
    public HashSet<CreativeRegion> getAreas() {
        return areas;
    }
    
    public CreativeRegion getRegion(Location loc) {
        
        for (CreativeRegion region : areas) {
            if (region.contains(loc)) {
                return region;
            }
        }
        
        return null;
    }
    
    public void addRegion(String name, Location start, Location end, String type) {
        CreativeRegion region = new CreativeRegion();
        region.start = start;
        region.end = end;
        
        if (type.equals("CREATIVE")) {
            region.type = gmType.CREATIVE;
        }
        if (type.equals("SURVIVAL")) {
            region.type = gmType.SURVIVAL;
        }

        region.name = name;
        region.world = start.getWorld();

        areas.add(region);
    }
    
    public void removeRegion(String name) {
        for (CreativeRegion region: areas) {
            if (region.name.equalsIgnoreCase(name)) {
                areas.remove(region);
                break;
            }
        }
    }

    public int loadRegions() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();

        int total = 0;
        try {
            ResultSet rs = db.getQuery("SELECT * FROM `"+db.prefix+"regions`");
            while (rs.next()) {
                String name = rs.getString("name");
                Location start = CreativeUtil.getLocation(rs.getString("start"));
                Location end = CreativeUtil.getLocation(rs.getString("end"));
                String type = rs.getString("type");
                addRegion(name, start, end, type);
                total++;
            }
        } catch (SQLException ex) {
            com.error("[TAG] Failed to get regions from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
        return total;
    }
    
    public boolean getRegion(String name) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();

        try {
            ResultSet rs = db.getQuery("SELECT * FROM `"+db.prefix+"regions` WHERE name = '"+name+"'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            com.error("[TAG] Failed to get regions from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
        
        return false;
    }
    
    /*
     * Delete region files
     */
    public void deleteRegion(String name) {
        CreativeSQLDatabase db = CreativeControl.getDb();

        removeRegion(name);

        String query = "DELETE FROM `"+db.prefix+"regions` WHERE name = '"+name+"'";
        db.executeQuery(query);
    }

    public void saveRegion(String name, gmType type, Location start, Location end) {
        CreativeSQLDatabase db = CreativeControl.getDb();

        if (!getRegion(name)) {
            addRegion(name, start, end, type.toString());
            String query = "INSERT INTO `"+db.prefix+"regions` (name, start, end, type) VALUES ('"+name+"', '"+CreativeUtil.getLocation(start)+"', '"+CreativeUtil.getLocation(end)+"', '"+type.toString()+"')";
            db.executeQuery(query);
        } else {
            removeRegion(name);
            addRegion(name, start, end, type.toString());
            String query = "UPDATE `"+db.prefix+"regions` SET start = '"+CreativeUtil.getLocation(start)+"', end = '"+CreativeUtil.getLocation(end)+"', type = '"+type.toString()+"' WHERE name = '"+name+"'";
            db.executeQuery(query);
        }
    }
}
