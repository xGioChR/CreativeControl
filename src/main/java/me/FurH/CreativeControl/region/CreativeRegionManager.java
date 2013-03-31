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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.location.LocationUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import org.bukkit.GameMode;
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
            region.gamemode = GameMode.CREATIVE;
        } else
        if (type.equals("ADVENTURE")) {
            region.gamemode = GameMode.ADVENTURE;
        } else {
            region.gamemode = GameMode.SURVIVAL;
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
        Communicator com    = CreativeControl.plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb2();

        int total = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT * FROM `"+db.prefix+"regions`");
            rs = ps.getResultSet();
            
            while (rs.next()) {
                String name = rs.getString("name");
                Location start = LocationUtils.stringToLocation2(rs.getString("start"));
                Location end = LocationUtils.stringToLocation2(rs.getString("end"));
                String type = rs.getString("type");
                addRegion(name, start, end, type);
                total++;
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get regions from the database, " + ex.getMessage());
        } catch (CoreMsgException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
        }
        return total;
    }
    
    public boolean getRegion(String name) {
        Communicator com    = CreativeControl.plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb2();

        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = db.getQuery("SELECT * FROM `"+db.prefix+"regions` WHERE name = '"+name+"'");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            com.error(Thread.currentThread(), ex, "[TAG] Failed to get region from the database, " + ex.getMessage());
        } catch (CoreDbException ex) {
            com.error(Thread.currentThread(), ex, ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) { }
            }
        }
        
        return false;
    }
    
    /*
     * Delete region files
     */
    public void deleteRegion(String name) {
        CreativeSQLDatabase db = CreativeControl.getDb2();

        removeRegion(name);

        db.queue("DELETE FROM `"+db.prefix+"regions` WHERE name = '"+name+"'");
    }

    public void saveRegion(String name, GameMode type, Location start, Location end) {
        CreativeSQLDatabase db = CreativeControl.getDb2();

        if (!getRegion(name)) {
            addRegion(name, start, end, type.toString());
            db.queue("INSERT INTO `"+db.prefix+"regions` (name, start, end, type) VALUES ('"+name+"', '"+LocationUtils.locationToString2(start)+"', '"+LocationUtils.locationToString2(end)+"', '"+type.toString()+"')");
        } else {
            removeRegion(name);
            addRegion(name, start, end, type.toString());
            db.queue("UPDATE `"+db.prefix+"regions` SET start = '"+LocationUtils.locationToString2(start)+"', end = '"+LocationUtils.locationToString2(end)+"', type = '"+type.toString()+"' WHERE name = '"+name+"'");
        }
    }
}
