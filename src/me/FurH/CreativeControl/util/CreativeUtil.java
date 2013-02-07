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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeUtil {
    
    public static String encode(String string) {
        if (string == null) {
            return "MA==";
        }
        return Base64Coder.encodeString(string);
    }
    
    public static String decode(String string) {
        if (string == null) {
            return "MA==";
        }
        return Base64Coder.decodeString(string);
    }

    /*
     * return true if the first line of the sign is listed as a economy sign
     */
    public static boolean isEconomySign(org.bukkit.block.Sign sign) {
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
     * return a HashSet of the List contends
     */
    public static HashSet<String> toStringHashSet(String string, String split) {
        HashSet<String> set = new HashSet<String>();

        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                set.addAll(Arrays.asList(string.split(split)));
            } else
            if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(string);
            }
        } catch (Exception ex) {
            CreativeCommunicator com    = CreativeControl.getCommunicator();
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to parse string to list: {0}, split: {1}, {2}", string, split, ex.getMessage());
        }
        
        return set;
    }
    
    /*
     * return a HashSet of the List contends
     */
    public static HashSet<Integer> toIntegerHashSet(String string, String split) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        HashSet<Integer> set = new HashSet<Integer>();

        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                String[] splits = string.split(split);

                for (String str : splits) {
                    try {
                        int i = Integer.parseInt(str);
                        set.add(i);
                    } catch (Exception ex) {
                        com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                                "[TAG] [TAG] {0} is not a valid number!, {1}", str, ex.getMessage());
                    }
                }
            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    set.add(Integer.parseInt(string));
                }
            }
        } catch (Exception ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to parse string to list: {0}, split: {1}, {2}", string, split, ex.getMessage());
        }
        
        return set;
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] {0} is not a valid number!, {1}", str, ex.getMessage());
        }
        return ret;
    }
    
    public static void getFloor(Player player) {
        Location loc = player.getLocation();
        Block b1 = loc.getBlock().getRelative(BlockFace.DOWN);

        if (b1.getType() != Material.AIR) {
            return;
        }

        int limit = 64;
        while (b1.getType() != Material.AIR && limit > 0) {
            b1 = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            limit--;
        }

        Location newloc = new Location(loc.getWorld(), loc.getX(), b1.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        player.teleport(newloc);
    }

    public static String getUptime() {
        long time = (System.currentTimeMillis() - CreativeControl.start);
        return (int)(time / 86400000) + "d " + (int)(time / 3600000 % 24) + "h " + (int)(time / 60000 % 60) + "m " + (int)(time / 1000 % 60) + "s";
    }

    /*
     * Dump the stack to a file
     */
    public static String stack(String className, int line, String method, Throwable ex, String message) {
        CreativeControl      plugin = CreativeControl.getPlugin();
        String format1 = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(System.currentTimeMillis());
        File data = new File(plugin.getDataFolder() + File.separator + "error", "");
        if (!data.exists()) { data.mkdirs(); }
        
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        data = new File(data.getAbsolutePath(), "error-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                com.log("Failed to create new log file, {0} .", e.getMessage());
            }
        }
        
        CreativeMainConfig config = CreativeControl.getMainConfig();
        boolean local = false;
        
        if (!config.database_mysql) {
            local = true;
        } else
        if ("localhost".equals(config.database_host)) {
            local = true;
        } else
        if ("127.0.0.1".equals(config.database_host)) {
            local = true;
        } else
        if (Bukkit.getServer().getIp().equals(config.database_host)) {
            local = true;
        }

        try {
            StackTraceElement[] st = ex.getStackTrace();
            String l = System.getProperty("line.separator");

            String format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis());
            FileWriter fw = new FileWriter(data, true);
            BufferedWriter bw = new BufferedWriter(fw);
            Runtime runtime = Runtime.getRuntime();
            
            File root = new File("/");

            int creative = 0;
            int survival = 0;
            int totalp = Bukkit.getOnlinePlayers().length;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode().equals(GameMode.CREATIVE)) {
                    creative++;
                } else {
                    survival++;
                }
            }
            
            CreativeBlockManager manager = CreativeControl.getManager();
            CreativeBlockCache cache = CreativeControl.getCache();
            CreativeSQLDatabase db = CreativeControl.getDb();
            
            bw.write(format2 +l);
            bw.write("	=============================[ ERROR INFORMATION ]============================="+l);
            bw.write("	- Plugin: " + plugin.getDescription().getFullName() + " (Latest: " + plugin.getVersion("1.0") + ")" +l);
            bw.write("	- Uptime: " + getUptime()+l);
            bw.write("	- Players: "+totalp+" ("+creative+" Creative, "+survival+" Survival)"+l);
            bw.write("	- Error Message: " + ex.getMessage() +l);
            bw.write("	- Location: " + className + ", Line: " + line + ", Method: " + method +l);
            bw.write("	- Comment: " + message +l);
            bw.write("	=============================[ HARDWARE SETTINGS ]============================="+l);
            bw.write("		Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("java.vendor.url") +l);
            bw.write("		System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") +l);
            bw.write("		Processors: " + runtime.availableProcessors() +l);
            bw.write("		Memory: "+l);
            bw.write("			Free: " + format(runtime.freeMemory()) +l);
            bw.write("			Total: " + format(runtime.totalMemory()) +l);
            bw.write("			Max: " + format(runtime.maxMemory()) +l);
            bw.write("		Storage: "+l);
            bw.write("			Total: " + format(root.getTotalSpace()) +l);
            bw.write("			Free: " + format(root.getFreeSpace()) +l);
            bw.write("	=============================[ INSTALLED PLUGINS ]============================="+l);
            bw.write("	Plugins:"+l);
            for (Plugin x : plugin.getServer().getPluginManager().getPlugins()) {
                bw.write("		- " + x.getDescription().getFullName() +l);
            }
            bw.write("	=============================[  LOADED   WORLDS  ]============================="+l);
            bw.write("	Worlds:"+l);
            for (World w : plugin.getServer().getWorlds()) {
                bw.write("		" + w.getName() + ":" +l);
                bw.write("			Envioronment: " + w.getEnvironment().toString() +l);
                bw.write("			Player Count: " + w.getPlayers().size() +l);
                bw.write("			Entity Count: " + w.getEntities().size() +l);
                bw.write("			Loaded Chunks: " + w.getLoadedChunks().length +l);
            }
            bw.write("	=============================[  SQL INFORMATION  ]============================="+l);
            bw.write("	- Cache Status:"+l);
            bw.write("		Read: " + cache.getReads() +l);
            bw.write("		Writes: " + cache.getWrites() +l);
            bw.write("		Capacity: " + cache.getSize() + "/" + cache.getMaxSize() +l);
            bw.write("	- SQL Status:"+l);
            if (db != null) {
                bw.write("		Type: " + db.type +l);
                bw.write("		Local: " + local +l);
                bw.write("		Queue Size: " + db.getQueue() +l);
                bw.write("		Reads: " + db.getReads() +l);
                bw.write("		Writes: " + db.getWrites() +l);
                bw.write("		Total Blocks: " + manager.getTotal() +l);
            } else {
                bw.write("		SQL DOWN!"+l);
            }
            bw.write("	=============================[ ERROR  STACKTRACE ]============================="+l);
            for (StackTraceElement element : st) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ END OF STACKTRACE ]============================="+l);
            bw.write(format2);
            bw.close();
            fw.close();
        } catch (IOException e) {
            com.log("Failed to write in the log file, {0}", e.getMessage());
        }
        
        return format1;
    }
    
    public static String format(double bytes) {
        DecimalFormat decimal = new DecimalFormat("#.##");
        
        if (bytes >= 1099511627776.0D) {
            return new StringBuilder().append(decimal.format(bytes / 1099511627776.0D)).append(" TB").toString();
        }
        if (bytes >= 1073741824.0D) {
            return new StringBuilder().append(decimal.format(bytes / 1073741824.0D)).append(" GB").toString();
        }
        if (bytes >= 1048576.0D) {
            return new StringBuilder().append(decimal.format(bytes / 1048576.0D)).append(" MB").toString();
        }
        if (bytes >= 1024.0D) {
            return new StringBuilder().append(decimal.format(bytes / 1024.0D)).append(" KB").toString();
        }
        return new StringBuilder().append("").append((int)bytes).append(" bytes").toString();
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to copy the file {0}, {1}", file.getName(), ex.getMessage());
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
                return null;
            }
        } catch (Exception ex) {
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to parse the location: {0}, {1}", location, ex.getMessage());
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