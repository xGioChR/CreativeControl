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

package me.FurH.CreativeControl.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import me.FurH.Core.cache.CoreLRUCache;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.Core.exceptions.CoreMsgException;
import me.FurH.Core.inventory.InvUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerData {
    public CoreLRUCache<String, CreativePlayerCache> adventurer_cache = new CoreLRUCache<String, CreativePlayerCache>(1000);
    public CoreLRUCache<String, CreativePlayerCache> creative_cache = new CoreLRUCache<String, CreativePlayerCache>(1000);
    public CoreLRUCache<String, CreativePlayerCache> survival_cache = new CoreLRUCache<String, CreativePlayerCache>(1000);

    public void clear() {
        adventurer_cache.clear();
        creative_cache.clear();
        survival_cache.clear();
    }
    
    public void clear(String player) {
        adventurer_cache.remove(player);
        creative_cache.remove(player);
        survival_cache.remove(player);
    }
    
    public void process(Player player, GameMode newgm, GameMode oldgm) {
        if (save(player, oldgm)) {
            restore(player, newgm);
        }
    }
    
    public boolean save(Player p, GameMode gm) {
        CreativeSQLDatabase db = CreativeControl.getDb();
        if (gm.equals(GameMode.ADVENTURE)) {
            CreativePlayerCache cache = hasAdv(p.getName());
            
            if (cache == null) {
                cache = new CreativePlayerCache(); cache.name = p.getName().toLowerCase();
                
                cache = newCache(p, cache);
                adventurer_cache.put(cache.name, cache);

                String query = "INSERT INTO `"+db.prefix+"players_adventurer` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES "
                        + "('"+db.getPlayerId(cache.name)+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";
                
                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }
                
                return true;
            } else {
                cache = newCache(p, cache);
                
                adventurer_cache.remove(cache.name);
                adventurer_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_adventurer` SET health = '"+cache.health+"', foodlevel = '"+cache.food+"', exhaustion = '"+cache.ex+"', "
                        + "saturation = '"+cache.sat+"', experience = '"+cache.exp+"', armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";

                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }
                
                return true;
            }
        } else 
        if (gm.equals(GameMode.CREATIVE)) {
            CreativePlayerCache cache = hasCre(p.getName());
            
            if (cache == null) {
                cache = new CreativePlayerCache(); cache.name = p.getName().toLowerCase();
                
                cache = newCache(p, cache);
                creative_cache.put(cache.name, cache);
                
                String query = "INSERT INTO `"+db.prefix+"players_creative` (player, armor, inventory) VALUES "
                        + "('"+db.getPlayerId(cache.name)+"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";

                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }

                return true;
            } else {
                cache = newCache(p, cache);
                
                creative_cache.remove(cache.name);
                creative_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_creative` SET armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";

                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }

                return true;
            }
        } else 
        if (gm.equals(GameMode.SURVIVAL)) {
            CreativePlayerCache cache = hasSur(p.getName());
            
            if (cache == null) {
                cache = new CreativePlayerCache(); cache.name = p.getName().toLowerCase();
                
                cache = newCache(p, cache);
                survival_cache.put(cache.name, cache);
                
                String query = "INSERT INTO `"+db.prefix+"players_survival` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES "
                        + "('"+db.getPlayerId(cache.name)+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";

                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }
                
                return true;
            } else {
                cache = newCache(p, cache);
                
                survival_cache.remove(cache.name);
                survival_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_survival` SET health = '"+cache.health+"', foodlevel = '"+cache.food+"', exhaustion = '"+cache.ex+"', "
                        + "saturation = '"+cache.sat+"', experience = '"+cache.exp+"', armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";
                
                try {
                    db.execute(query);
                } catch (CoreDbException ex) {
                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
                }
                
                return true;
            }
        } else {
            return false;
        }
    }
    
    public CreativePlayerCache newCache(Player p, CreativePlayerCache cache) {
        cache.armor = p.getInventory().getArmorContents();
        cache.health = p.getHealth();
        cache.food = p.getFoodLevel();
        cache.ex = p.getExhaustion();
        cache.exp = p.getTotalExperience();
        cache.sat = p.getSaturation();
        cache.items = p.getInventory().getContents();
        return cache;
    }

    public void restore(Player p, GameMode gm) {        
        if (gm.equals(GameMode.ADVENTURE)) {
            CreativePlayerCache cache = hasAdv(p.getName());
            restore(p, cache);
        } else
        if (gm.equals(GameMode.CREATIVE)) {
            CreativePlayerCache cache = hasCre(p.getName());
            restore(p, cache);
        } else
        if (gm.equals(GameMode.SURVIVAL)) {
            CreativePlayerCache cache = hasSur(p.getName());
            restore(p, cache);
        }
    }
    
    public CreativePlayerCache hasAdv(String player) {
        CreativePlayerCache cache = adventurer_cache.get(player.toLowerCase());
        Communicator com        = CreativeControl.plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_adventurer` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'");
                rs = ps.getResultSet();
                
                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.health = rs.getInt("health");
                    cache.food = rs.getInt("foodlevel");
                    cache.ex = rs.getShort("exhaustion");
                    cache.sat = rs.getShort("saturation");
                    cache.exp = rs.getInt("experience");
                    cache.armor = toArrayStack(rs.getString("armor"));
                    cache.items = toArrayStack(rs.getString("inventory"));
                    adventurer_cache.put(cache.name, cache);
                }
            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get the data from the database, " + ex.getMessage());
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasSur(String player) {
        CreativePlayerCache cache = survival_cache.get(player.toLowerCase());
        Communicator com        = CreativeControl.plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_survival` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'");
                rs = ps.getResultSet();
                
                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.health = rs.getInt("health");
                    cache.food = rs.getInt("foodlevel");
                    cache.ex = rs.getShort("exhaustion");
                    cache.sat = rs.getShort("saturation");
                    cache.exp = rs.getInt("experience");
                    cache.armor = toArrayStack(rs.getString("armor"));
                    cache.items = toArrayStack(rs.getString("inventory"));
                    survival_cache.put(cache.name, cache);
                }

            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get the data from the database, " + ex.getMessage());
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasCre(String player) {
        CreativePlayerCache cache = creative_cache.get(player.toLowerCase());
        Communicator com        = CreativeControl.plugin.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();

        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_creative` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'");
                rs = ps.getResultSet();

                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.armor = toArrayStack(rs.getString("armor"));
                    cache.items = toArrayStack(rs.getString("inventory"));
                    creative_cache.put(cache.name, cache);
                }

            } catch (SQLException ex) {
                com.error(Thread.currentThread(), ex, "[TAG] Failed to get the data from the database, " + ex.getMessage());
            } catch (CoreDbException ex) {
                com.error(Thread.currentThread(), ex, ex.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }
            }
        }
        return cache;
    }
    
    private void restore(Player p, CreativePlayerCache cache) {
        if (cache == null) { cache = new CreativePlayerCache(); }
        
        p.getInventory().setArmorContents(cache.armor);
        
        CreativeMainConfig    config   = CreativeControl.getMainConfig();
        if (config.data_status) {

            int health = cache.health;
            
            if (health <= 0) { health = 20; }
            if (health > 20) { health = 20; }

            p.setHealth(health);

            p.setFoodLevel(cache.food);
            p.setExhaustion(cache.ex);
            p.setSaturation(cache.sat);
        }
        
        p.getInventory().setContents(cache.items);
    }

    private String toListString(ItemStack[] armor) {
        return InvUtils.toListString(armor);
    }

    private ItemStack[] toArrayStack(String string) {

        try {
            return InvUtils.toArrayStack(string);
        } catch (CoreMsgException ex) {
            CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
        
        return null;
    }
}