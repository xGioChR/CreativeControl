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

package me.FurH.CreativeControl.data.conversor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.data.CreativePlayerCache;
import me.FurH.CreativeControl.data.CreativePlayerData;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativePlayerInv;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerConversor {

    public static void loadup() {
        CreativeCommunicator com      = CreativeControl.getCommunicator();
        CreativeControl      plugin   = CreativeControl.getPlugin();

        File dir = new File(plugin.getDataFolder() + File.separator + "PlayerData");
        if (dir.exists()) {
            com.log("[TAG] Converting Inventories to the new System...");
            String[] files = dir.list();
            int total = 0;

            for (String file : files) {
                com.log("[TAG] Converting {0}/{1}, Name: {2}", total++, files.length, file);
                checkout(file, new File(plugin.getDataFolder() + File.separator + "PlayerData" + File.separator + file));
            }

            dir.renameTo(new File(plugin.getDataFolder() + File.separator + "PlayerDataOld"));
            com.log("[TAG] {0} Inventories converted successfuly!", total);
        }
    }

    private static void checkout(String player, File dir) {
        if (dir.exists()) {            
            File creative = new File(dir.getPath() + File.separator + "creative.inv");
            if (creative.exists() && creative.isFile()) {
                convert(player.toLowerCase(), creative, GameMode.CREATIVE);
            }

            File survival = new File(dir.getPath() + File.separator + "survival.inv");
            if (survival.exists() && survival.isFile()) {
                convert(player.toLowerCase(), survival, GameMode.SURVIVAL);
            }

            File adventurer = new File(dir.getPath() + File.separator + "adventure.inv");
            if (adventurer.exists() && adventurer.isFile()) {
                convert(player.toLowerCase(), adventurer, GameMode.ADVENTURE);
            }
        }
    }

    private static void convert(String player, File file, GameMode gm) {
        CreativeCommunicator com      = CreativeControl.getCommunicator();
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            CreativePlayerInv[] fromFile = (CreativePlayerInv[]) ois.readObject();
            ois.close();
            
            ItemStack[] armor = new ItemStack[4];
            ItemStack[] items = new ItemStack[fromFile.length - 4];

            for (int i = 0; i < 4; i++) {
                armor[i] = fromFile[i].getItem();
            }

            for (int i = 4; i < fromFile.length; i++) {
                items[(i - 4)] = fromFile[i].getItem();
            }
            
            CreativePlayerData    data     = CreativeControl.getPlayerData();
            CreativeSQLDatabase db = CreativeControl.getDb();
  
            if (gm.equals(GameMode.ADVENTURE)) {
                CreativePlayerCache has = data.hasAdv(player);
                if (has == null) {
                    CreativePlayerCache cache = new CreativePlayerCache(); cache.name = player.toLowerCase();
                    String query = "INSERT INTO `"+db.prefix+"players_adventurer` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES "
                            + "('"+player+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ data.toListString(armor) +"', '"+ data.toListString(items) +"');";
                    db.executeQuery(query, true);
                } else {
                    String query = "UPDATE `"+db.prefix+"players_adventurer` SET armor = '"+data.toListString(armor)+"', inventory = '"+ data.toListString(items) +"' WHERE id = '"+has.id+"'";
                    db.executeQuery(query, true);
                }
            } else 
            if (gm.equals(GameMode.CREATIVE)) {
                CreativePlayerCache has = data.hasCre(player);
                if (has == null) {
                    CreativePlayerCache cache = new CreativePlayerCache(); cache.name = player.toLowerCase();
                    String query = "INSERT INTO `"+db.prefix+"players_creative` (player, armor, inventory) VALUES "
                            + "('"+cache.name+"', '"+ data.toListString(armor) +"', '"+ data.toListString(items) +"');";
                    db.executeQuery(query, true);
                } else {
                    String query = "UPDATE `"+db.prefix+"players_creative` SET armor = '"+data.toListString(armor)+"', inventory = '"+ data.toListString(items) +"' WHERE id = '"+has.id+"'";
                    db.executeQuery(query, true);
                }
            } else 
            if (gm.equals(GameMode.SURVIVAL)) {
                CreativePlayerCache has = data.hasSur(player);

                if (has == null) {
                    CreativePlayerCache cache = new CreativePlayerCache(); cache.name = player.toLowerCase();
                    String query = "INSERT INTO `"+db.prefix+"players_survival` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES "
                            + "('"+player+"', '"+cache.health+"', '"+cache.food+"', '"+cache.ex+"', '"+cache.sat+"', '" + cache.exp +"', '"+ data.toListString(armor) +"', '"+ data.toListString(items) +"');";
                    db.executeQuery(query, true);
                } else {
                    String query = "UPDATE `"+db.prefix+"players_survival` SET armor = '"+data.toListString(armor)+"', inventory = '"+ data.toListString(items) +"' WHERE id = '"+has.id+"'";
                    db.executeQuery(query, true);
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Couldn't restore {0}'s inventory, {1}", ex, player, ex.getMessage());
        } catch (ClassNotFoundException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Couldn't restore {0}'s inventory, {1}", ex, player, ex.getMessage());
        } catch (ClassCastException ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Couldn't restore {0}'s inventory, {1}", ex, player, ex.getMessage());
        }
    }
}
