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
public class CreativeRegionCreator {    
    
    /*
     * Load regions by file
     */
    public void loadRegions() {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeRegion regions = CreativeControl.getRegions();
        CreativeSQLDatabase db = CreativeControl.getDb();

        try {
            ResultSet rs = db.getQuery("SELECT * FROM `"+db.prefix+"regions`");
            while (rs.next()) {
                String name = rs.getString("name");
                Location start = CreativeUtil.getLocation(rs.getString("start"));
                Location end = CreativeUtil.getLocation(rs.getString("end"));
                String type = rs.getString("type");
                regions.add(name, start, end, type);
            }
        } catch (SQLException ex) {
            com.error("[TAG] Failed to get regions from the database, {0}", ex, ex.getMessage());
            if (!db.isOk()) { db.fix(); }
        }
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
        CreativeRegion regions = CreativeControl.getRegions();

        regions.remove(name);

        String query = "DELETE FROM `"+db.prefix+"regions` WHERE name = '"+name+"'";
        db.executeQuery(query);
    }
    
    /*
     * Save regions
     */
    public void saveRegion(String name, gmType type, Location start, Location end) {          

        CreativeSQLDatabase db = CreativeControl.getDb();
        CreativeRegion regions = CreativeControl.getRegions();

        if (!getRegion(name)) {
            regions.add(name, start, end, type.toString());
            String query = "INSERT INTO `"+db.prefix+"regions` (name, start, end, type) VALUES ('"+name+"', '"+CreativeUtil.getLocation(start)+"', '"+CreativeUtil.getLocation(end)+"', '"+type.toString()+"')";
            db.executeQuery(query);
        } else {
            regions.remove(name);
            regions.add(name, start, end, type.toString());
            String query = "UPDATE `"+db.prefix+"regions` SET start = '"+CreativeUtil.getLocation(start)+"', end = '"+CreativeUtil.getLocation(end)+"', type = '"+type.toString()+"' WHERE name = '"+name+"'";
            db.executeQuery(query);
        }
    }
}
