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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeLRUCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeArmorMeta;
import me.FurH.CreativeControl.util.CreativeBookMeta;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeFireworkMeta;
import me.FurH.CreativeControl.util.CreativeItemMeta;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerData {
    public CreativeLRUCache<String, CreativePlayerCache> adventurer_cache = new CreativeLRUCache<String, CreativePlayerCache>(1000);
    public CreativeLRUCache<String, CreativePlayerCache> creative_cache = new CreativeLRUCache<String, CreativePlayerCache>(1000);
    public CreativeLRUCache<String, CreativePlayerCache> survival_cache = new CreativeLRUCache<String, CreativePlayerCache>(1000);

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
    
    public int saveRam() {
        int total = 0;
        
        total += adventurer_cache.size();
        adventurer_cache.clear();
        
        total += creative_cache.size();
        creative_cache.clear();
        
        total += survival_cache.size();
        survival_cache.clear();
        
        return total;
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
                        + "('"+cache.name+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";

                db.executeQuery(query, true);
                return true;
            } else {
                cache = newCache(p, cache);
                
                adventurer_cache.remove(cache.name);
                adventurer_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_adventurer` SET health = '"+cache.health+"', foodlevel = '"+cache.food+"', exhaustion = '"+cache.ex+"', "
                        + "saturation = '"+cache.sat+"', experience = '"+cache.exp+"', armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";
                db.executeQuery(query);
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
                        + "('"+cache.name+"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";
                
                db.executeQuery(query, true);
                return true;
            } else {
                cache = newCache(p, cache);
                
                creative_cache.remove(cache.name);
                creative_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_creative` SET armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";
                db.executeQuery(query);
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
                        + "('"+cache.name+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ toListString(cache.armor) +"', '"+ toListString(cache.items) +"');";
                
                db.executeQuery(query, true);
                return true;
            } else {
                cache = newCache(p, cache);
                
                survival_cache.remove(cache.name);
                survival_cache.put(cache.name, cache);
                
                String query = "UPDATE `"+db.prefix+"players_survival` SET health = '"+cache.health+"', foodlevel = '"+cache.food+"', exhaustion = '"+cache.ex+"', "
                        + "saturation = '"+cache.sat+"', experience = '"+cache.exp+"', armor = '"+toListString(cache.armor)+"', inventory = '"+ toListString(cache.items) +"' WHERE id = '"+cache.id+"'";
                db.executeQuery(query);
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
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_adventurer` WHERE player = '" + player.toLowerCase() + "'");
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to get the data from the database, {0}", ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }/*
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ex) { }
                }*/
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasSur(String player) {
        CreativePlayerCache cache = survival_cache.get(player.toLowerCase());
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();
        
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_survival` WHERE player = '" + player.toLowerCase() + "'");
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to get the data from the database, {0}", ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }/*
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ex) { }
                }*/
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasCre(String player) {
        CreativePlayerCache cache = creative_cache.get(player.toLowerCase());
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeSQLDatabase db = CreativeControl.getDb();

        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `"+db.prefix+"players_creative` WHERE player = '" + player.toLowerCase() + "'");
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
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to get the data from the database, {0}", ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) { }
                }/*
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ex) { }
                }*/
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
    
    public ItemStack[] toArrayStack(String item) {
        String[] stacks = item.substring(1, item.length() - 1).split(", ");

        ItemStack[] items = new ItemStack[ stacks.length ];
        for (int i = 0; i < stacks.length; i++) {
            items[i] = toItemStack(stacks[i]);
        }

        return items;
    }

    private ItemStack toItemStack(String string) {
        CreativeCommunicator com        = CreativeControl.getCommunicator();

        if (string.equals("0")) {
            return null;
        }

        ItemStack stack = new ItemStack(Material.AIR);
        if (string.equals("[]")) { return stack; }
        if (!string.contains(":")) { return stack; }
        
        String[] inv = string.split(":");
        if (inv.length < 4) { return stack; }
        
        String id = inv[0];
        String data = inv[1];
        String amount = inv[2];
        String durability = inv[3];
        String enchantments = inv[4];
        
        boolean meta = false;
        try {
            String fire = inv[5];
            meta = true;
        } catch (Exception ex) {
            meta = false;
        }
        
        if (!string.equals("0:0:0:1:[]")) {
            try {
                stack = new ItemStack(Integer.parseInt(id));
                stack.setAmount(Integer.parseInt(amount));
                stack.setDurability(Short.parseShort(durability));

                int i = Integer.parseInt(data);
                if (i > 128) {
                    i = 128;
                }

                stack.getData().setData((byte)i);
            } catch (Exception ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Invalid Item String: {0}, {1}", string, ex.getMessage());
                return new ItemStack(Material.AIR);
            }

            if (!enchantments.equals("[]")) {
                enchantments = enchantments.replaceAll("[^a-zA-Z0-9_:,=]", "");
                String[] enchant = enchantments.split(",");

                List<String> encht = new ArrayList<String>();
                encht.addAll(Arrays.asList(enchant));

                for (String exlvl : encht) {
                    if (exlvl.contains("=")) {
                        String[] split = exlvl.split("=");
                        String name = split[0];
                        String lvl = split[1];
                        try {
                            Enchantment ext = Enchantment.getByName(name);
                            stack.addUnsafeEnchantment(ext, Integer.parseInt(lvl));
                        } catch (Exception ex) {
                            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                                    "[TAG] Invalid Enchantment: {0} level {1}, {2}", name, lvl, ex.getMessage());
                        }
                    }
                }
            }
            if (meta) {
                stack = CreativeItemMeta.setItemMeta(stack, inv[5]);
                
                if (stack.getType() == Material.FIREWORK) {
                    stack = CreativeFireworkMeta.getFireWork(stack, string);
                }
                if (stack.getType() == Material.BOOK || stack.getType() == Material.BOOK_AND_QUILL || stack.getType() == Material.WRITTEN_BOOK) {
                    stack = CreativeBookMeta.setBookMeta(stack, inv[5]);
                }
                if (stack.getType() == Material.LEATHER_HELMET || stack.getType() == Material.LEATHER_CHESTPLATE || stack.getType() == Material.LEATHER_LEGGINGS || stack.getType() == Material.LEATHER_BOOTS) {
                    stack = CreativeArmorMeta.setArmorMeta(stack, inv[5]);
                }
            }
        }

        return stack;
    }
    
    public String toListString(ItemStack[] source) {
        List<String> items = new ArrayList<String>();

        for (ItemStack item : source) {
            items.add(toString(item));
        }

        return items.toString();
    }

    private String toString(ItemStack item) {
        if (item == null) { 
            return "0"; 
        }

        if (item.getType() == Material.AIR) {
            return "0";
        }

        int type = item.getTypeId();
        int amount = item.getAmount();
        byte data = item.getData().getData();
        short durability = item.getDurability();

        Map<Enchantment, Integer> e1 = item.getEnchantments();
        
        List<String> enchantments = new ArrayList<String>();
        for (Enchantment key : e1.keySet()) {
            enchantments.add(key.getName() + "=" + e1.get(key));
        }
        
        String ret = ("'"+type+":"+data+":"+amount+":"+durability+":"+enchantments+"'").replaceAll("[^a-zA-Z0-9:,_=\\[\\]]", "");
        
        if (item.hasItemMeta()) {
            ret += CreativeItemMeta.getItemMeta(item);
        }
        if (item.getType() == Material.FIREWORK) {
            ret = CreativeFireworkMeta.getFireWork(ret, item);
        }
        if (item.getItemMeta() instanceof BookMeta) {
            ret += CreativeBookMeta.getBookMeta(item);
        }
        if (item.getItemMeta() instanceof LeatherArmorMeta) {
            ret += CreativeArmorMeta.getArmorMeta(item);
        }

        return ret;
    }
}