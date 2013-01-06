package me.FurH.CreativeControl.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeCommunicator.Type;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePerformance {
    public static EnumMap<Event, EventData> times = new EnumMap<Event, EventData>(Event.class);

    public static void update(Event event, double time) {
        CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.perfm_monitor) {
            EventData data = new EventData();
            List<Double> dt = new ArrayList<Double>();

            if (times.containsKey(event)) {
                data = times.get(event);
                dt = data.times;
            }

            data.cast++;
            if (time > 0) {
                dt.add(time);
            }
            
            CreativeControl.getCommunicator().log("{0}, took {1} ms", Type.DEBUG, event.toString(), time);

            times.put(event, data);
        }
    }
    
    public static String report() {
        CreativeControl      plugin = CreativeControl.getPlugin();
        String format1 = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(System.currentTimeMillis());
        File data = new File(plugin.getDataFolder() + File.separator + "report");
        if (!data.exists()) { data.mkdirs(); }
        
        CreativeCommunicator com     = CreativeControl.getCommunicator();
        CreativeBlockManager manager = CreativeControl.getManager();
        data = new File(data.getAbsolutePath(), "report-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                com.log("Failed to create new log file, {0} .", e.getMessage());
            }
        }
        
        try {
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

            CreativeBlockCache cache = CreativeControl.getCache();
            CreativeSQLDatabase db = CreativeControl.getDb();

            bw.write(format2 +l);
            bw.write("	=============================[ TEST INFORMATION ]============================="+l);
            bw.write("	- Plugin: " + plugin.getDescription().getFullName() + " (Latest: " + plugin.getVersion("1.0") + ")" +l);
            bw.write("	- Uptime: " + CreativeUtil.getUptime()+l);
            bw.write("	- Players: "+totalp+" ("+creative+" Creative, "+survival+" Survival)"+l);
            bw.write("	=============================[ HARDWARE SETTINGS ]============================="+l);
            bw.write("		Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("java.vendor.url") +l);
            bw.write("		System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") +l);
            bw.write("		Processors: " + runtime.availableProcessors() +l);
            bw.write("		Memory: "+l);
            bw.write("			Free: " + CreativeUtil.format(runtime.freeMemory()) +l);
            bw.write("			Total: " + CreativeUtil.format(runtime.totalMemory()) +l);
            bw.write("			Max: " + CreativeUtil.format(runtime.maxMemory()) +l);
            bw.write("		Storage: "+l);
            bw.write("			Total: " + CreativeUtil.format(root.getTotalSpace()) +l);
            bw.write("			Free: " + CreativeUtil.format(root.getFreeSpace()) +l);
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
            bw.write("		Queue Size: " + db.getQueue() +l);
            bw.write("		Reads: " + db.getReads() +l);
            bw.write("		Writes: " + db.getWrites() +l);
            bw.write("		SQL Cache: " + db.getSize() +l);
            bw.write("		Total Blocks: " + manager.getTotal() +l);
            bw.write("	=============================[ TIMMINGS   REPORT ]============================="+l);
            for (Event event : times.keySet()) {
                EventData x = times.get(event);
                List<Double> dt = x.times;

                double min = 0;
                double max = 0;
                double avg = 0;

                if (!dt.isEmpty()) {
                    min = Collections.min(dt);
                    max = Collections.max(dt);
                    avg = ((min + max) / 2);
                }
                
                double total = 0;
                for (Double single : dt) {
                    total += single;
                }
                
                double percast = (total / (double) x.cast);
                
                bw.write("		- " + event.toString() + ":"+l);
                bw.write("			- Total: "+total+"MS, Cast "+x.cast+" times, "+String.format("%.2f", percast)+"MS/c, Min: "+min+"MS, Max: "+max+"MS, Avg: "+avg+"MS"+l);
            }
            bw.write("	=============================[  END OF TIMMINGS  ]============================="+l);
            bw.write(format2);
            bw.close();
            fw.close();
        } catch (IOException e) {
            com.log("Failed to write in the log file, {0}", e.getMessage());
        }
        
        return format1;
    }

    public static class EventData {
        public int cast = 0;
        public List<Double> times = new ArrayList<Double>();
    }
    
    public enum Event {
        WorldLoadEvent, StructureGrowEvent, PlayerGameModeChangeEvent, PlayerCommandPreprocessEvent, PlayerDeathEvent, EnchantItemEvent, InventoryCloseEvent, InventoryClickEvent,
        PlayerKickEvent, PlayerQuitEvent, PlayerJoinEvent, PlayerChangedWorldEvent, PlayerPickupItemEvent, PlayerDropItemEvent, PlayerEggThrowEvent, PlayerInteractEvent, PlayerMoveEvent,
        VehicleCreateEvent, VehicleDestroyEvent, EntityExplodeEvent, EntityTargetEvent, PlayerInteractEntityEvent, EntityDeathEvent, EntityDamageEvent, BlockPlaceEvent,
        BlockBreakEvent, BlockPistonExtendEvent, BlockPistonRetractEvent, SQLWrite, SQLRead, FastCacheWrite, FastCacheRead, SlowCacheWrite, SlowCacheRead
    }
}