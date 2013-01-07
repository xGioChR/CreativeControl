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

package me.FurH.CreativeControl.data.friend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerFriends {
    private ConcurrentHashMap<String, HashSet<String>> hascache = new ConcurrentHashMap<String, HashSet<String>>(500);
    
    public void uncache(Player p) {
        hascache.remove(p.getName().toLowerCase());
    }
    
    public void clear() {
        hascache.clear();
    }
    
    public void saveFriends(String player, HashSet<String> friends) {
        CreativeSQLDatabase db = CreativeControl.getDb();
        
        hascache.replace(player, friends);

        String query = "UPDATE `"+db.prefix+"friends` SET friends = '"+friends.toString()+"' WHERE player = '"+player.toLowerCase()+"'";
        db.executeQuery(query);
    }
    
    public HashSet<String> getFriends(String player) {
        HashSet<String> friends = hascache.get(player);
        
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();

        if (friends == null) {
            ResultSet rs = null;
            try {
                rs = db.getQuery("SELECT * FROM `"+db.prefix+"friends` WHERE player = '" + player.toLowerCase() + "'");
                
                if (rs.next()) {
                    friends = CreativeUtil.toStringHashSet(rs.getString("friends"), ", ");
                } else {
                    friends = new HashSet<String>();
                    db.executeQuery("INSERT INTO `"+db.prefix+"friends` (player, friends) VALUES ('"+ player.toLowerCase() +"', '"+ friends.toString() +"');", true);
                }

                hascache.put(player, friends);
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to get the data from the database, {0}", ex, ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }
            }
        }
        
        return friends;
    }
}
