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

package me.FurH.CreativeControl.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeUtil {
    
    /*
     * return true if the first line of the sign is listed as a economy sign
     */
    public static boolean isEconomySign(Sign sign) {
        String line1 = sign.getLine(0).replaceAll(" ", "_");

        CreativeWorldNodes config = CreativeWorldConfig.get(sign.getWorld());

        if (line1.contains("§")) {
            line1 = line1.replaceAll("§([0-9a-fk-or])", "");
        }

        if (config.black_sign.contains(line1.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * convert and string to a list
     */
    public static List<Integer> toIntegerList(String string, String split) {
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                List<Integer> ints = new ArrayList<Integer>();
                String[] splits = string.split(split);

                for (String str : splits) {
                    try {
                        int i = Integer.parseInt(str);
                        ints.add(i);
                    } catch (Exception ex) {
                        CreativeCommunicator com    = CreativeControl.getCommunicator();
                        com.error("[TAG] {0} is not a valid number!, {1}", ex, str, ex.getMessage());
                    }
                }
                
                return ints;
            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    return Arrays.asList(new Integer[] { Integer.parseInt(string) });
                } else {
                    return new ArrayList<Integer>();
                }
            }
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] Failed to parse string to list: {0}, split: {1}, {2}", ex, string, split, ex.getMessage());
            return new ArrayList<Integer>();
        }
    }
    
    /*
     * convert and string to a list
     */
    public static List<String> toStringList(String string, String split) {
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                return Arrays.asList(string.split(split));
            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    return Arrays.asList(new String[] { string });
                } else {
                    return new ArrayList<String>();
                }
            }
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] Failed to parse string to list: {0}, split: {1}, {2}", ex, string, split, ex.getMessage());
            return new ArrayList<String>();
        }
    }
    
    /*
     * return a Integer
     */
    public static int toInteger(String str) {
        int ret = 0;
        try {
            ret = Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("[TAG] {0} is not a valid number!, {1}", ex, str, ex.getMessage());
        }
        return ret;
    }
    
    public static void getFloor(Player player) {
        Location loc = player.getLocation();
        Block b1 = loc.getBlock().getRelative(BlockFace.DOWN);

        int limit = 64;
        while (b1.getType() != Material.AIR && limit > 0) {
            b1 = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            limit--;
        }

        Location newloc = new Location(loc.getWorld(), loc.getX(), b1.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        player.teleport(newloc);
    }
    
    /*
     * Dump the stack to a file
     */
    public static String stack(Throwable ex) {
        CreativeControl      plugin = CreativeControl.getPlugin();
        String format1 = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(System.currentTimeMillis());
        File data = new File(plugin.getDataFolder() + File.separator + "error", "");
        if (!data.exists()) { data.mkdirs(); }
        
        data = new File(data.getAbsolutePath(), "error-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                CreativeCommunicator com    = CreativeControl.getCommunicator();
                com.error("Failed to create new log file, {0} .", e, e.getMessage());
            }
        }
        
        StackTraceElement[] st = ex.getStackTrace();
        FileWriter Writer;
        try {
            String format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis());
            Writer = new FileWriter(data, true);
            BufferedWriter Out = new BufferedWriter(Writer);
            Out.write(format2 + " - " + "Error Message: " + ex.getMessage() + System.getProperty("line.separator"));
            
            List<String> pls = new ArrayList<String>();
            Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();
            for (Plugin pl1 : plugins) {
                pls.add(pl1.getDescription().getFullName());
            }

            Out.write(format2 + " - " + "Plugins ("+pls.size()+"): " + pls.toString() + System.getProperty("line.separator"));
            Out.write(format2 + " - " + "=============================[ ERROR  STACKTRACE ]=============================" + System.getProperty("line.separator"));
            for (int i = 0; i < st.length; i++) {
                Out.write(format2 + " - " + st[i].toString() + System.getProperty("line.separator"));
            }

            Out.write(format2 + " - " + "=============================[ END OF STACKTRACE ]=============================" + System.getProperty("line.separator"));
            Out.close();
        } catch (IOException e) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("Failed to write in the log file, {0}", e, e.getMessage());
        }
        
        return format1;
    }

    /*
     * Copy a file from a location
     */
    public static void ccFile(InputStream in, File file) {
        try {
            if ((file.getParentFile() != null) && (!file.getParentFile().exists())) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[512];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
        } catch (IOException ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error("Failed to copy the file {0}, {1}", ex, file.getName(), ex.getMessage());
        }
    }

    /*
     * return a String represation of the location
     */
    public static String getLocation(Location location) {
        return (location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ());
    }
    
    /*
     * get a location from string
     */
    public static Location getLocation(String location) {
        CreativeCommunicator com = CreativeControl.getCommunicator();
        try {
            String[] split = location.split(":");
            World w = Bukkit.getWorld(split[0]);
            if (w != null) {
                return new Location(w, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
            } else {
                com.log("[TAG] Failed to get the world: {0}!", split[0]);
                return null;
            }
        } catch (Exception ex) {
            com.error("[TAG] Failed to parse the location: {0}, {1}", ex, location, ex.getMessage());
            return null;
        }
    }

    public static String getSimpleDate(long date) {
        return new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(date);
    }

    /*
     * return the date in the defined miliseconds
     */
    public static String getDate(long date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }
}