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

package me.FurH.CreativeControl;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.exceptions.CoreDbException;
import me.FurH.CreativeControl.commands.CreativeCommands;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.CreativePlayerData;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.database.extra.CreativeSQLUpdater;
import me.FurH.CreativeControl.integration.AuthMe;
import me.FurH.CreativeControl.integration.MobArena;
import me.FurH.CreativeControl.integration.SurvivalGames;
import me.FurH.CreativeControl.integration.worldedit.CreativeEditSessionFactory;
import me.FurH.CreativeControl.integration.xAuth;
import me.FurH.CreativeControl.listener.*;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.metrics.CreativeMetrics;
import me.FurH.CreativeControl.metrics.CreativeMetrics.Graph;
import me.FurH.CreativeControl.permissions.CreativePermissions;
import me.FurH.CreativeControl.region.CreativeRegion;
import me.FurH.CreativeControl.region.CreativeRegion.CreativeMode;
import me.FurH.CreativeControl.region.CreativeRegionManager;
import me.FurH.CreativeControl.selection.CreativeBlocksSelection;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeControl extends CorePlugin {

    public CreativeControl() {
        super("&8[&3CreativeControl&8]&7:&f");
    }

    /* classes */
    public static CreativeControl plugin;
    private static CreativeSQLDatabase database;
    private static CreativeBlocksSelection selector;
    private static CreativeRegionManager regioner;
    private static CreativeBlockManager manager;
    private static CreativePlayerData data;
    private static CreativePlayerFriends friends;
    private static CreativeMainConfig mainconfig;
    private static CreativeMessages messages;
    private static Consumer lbconsumer = null;
    private static CreativeWorldConfig worldconfig;

    private static CreativePermissions permissions;

    public WeakHashMap<Player, Location> right = new WeakHashMap<Player, Location>();
    public WeakHashMap<Player, Location> left = new WeakHashMap<Player, Location>();

    public Map<String, Integer> mods = new HashMap<String, Integer>();
    public Map<String, HashSet<UUID>> limits = new HashMap<String, HashSet<UUID>>();

    public String currentversion;
    public String newversion;

    public boolean hasUpdate;

    @Override
    public void onEnable() {
        plugin = this;

        messages = new CreativeMessages(this);
        messages.load();

        getCommunicator().setTag(messages.prefix_tag);

        log("[TAG] Initializing configurations...");
        mainconfig = new CreativeMainConfig(this);
        mainconfig.load();

        worldconfig = new CreativeWorldConfig(this);
        worldconfig.setSingleConfig(mainconfig.config_single);

        if (!mainconfig.config_single) {
            for (World w : getServer().getWorlds()) { worldconfig.load(w); }
        } else {
            worldconfig.load(getServer().getWorlds().get(0));
        }

        mainconfig.updateConfig();

        getCommunicator().setDebug(mainconfig.com_debugcons);
        getCommunicator().setQuiet(mainconfig.com_quiet);

        log("[TAG] Loading Modules...");
        selector = new CreativeBlocksSelection();
        regioner = new CreativeRegionManager();
        manager = new CreativeBlockManager();
        friends = new CreativePlayerFriends();
        data = new CreativePlayerData();
        permissions = new CreativePermissions();

        database = new CreativeSQLDatabase(this, mainconfig.database_prefix, mainconfig.database_type, mainconfig.database_host, mainconfig.database_port, mainconfig.database_table, mainconfig.database_user, mainconfig.database_pass);

        try {
            database.connect();
        } catch (CoreDbException ex) {
            getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }

        database.load();

        log("[TAG] Registring Events...");
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new CreativeBlockListener(), this);
        pm.registerEvents(new CreativeEntityListener(), this);
        pm.registerEvents(new CreativePlayerListener(), this);
        pm.registerEvents(new CreativeWorldListener(), this);

        if (mainconfig.events_move) {
            pm.registerEvents(new CreativeMoveListener(), this);
        }

        if (mainconfig.events_misc) {
            pm.registerEvents(new CreativeMiscListener(), this);
        }

        loadIntegrations();
                
        CommandExecutor cc = new CreativeCommands();
        getCommand("creativecontrol").setExecutor(cc);

        setupLogBlock();
        setupWorldEdit();
        
        permissions.setup();

        log("[TAG] Cached {0} protections", manager.preCache());
        log("[TAG] Loaded {0} regions", regioner.loadRegions());
        log("[TAG] {0} blocks protected", manager.getTotal());

        PluginDescriptionFile version = getDescription();
        currentversion = "v"+version.getVersion();
        log("[CreativeControl] CreativeControl " + currentversion + " Enabled");

        if (mainconfig.updater_enabled) {
            updateThread();
        }

        startMetrics();
        
        try {
            if (database.isUpdateAvailable()) {
                log("[TAG] Database update required!");
                Bukkit.getScheduler().runTaskAsynchronously(this, new CreativeSQLUpdater(this));
            }
        } catch (CoreDbException ex) {
            getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }
    }
    
    @Override
    public void onDisable() {

        try {
            database.disconnect(false);
        } catch (CoreDbException ex) {
            getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
        }

        HandlerList.unregisterAll(this);

        clear();
        right.clear();
        left.clear();
        mods.clear();
        data.clear();
        friends.clear();
        limits.clear();
        
        getLogger().info("[CreativeControl] CreativeControl " + currentversion + " Disabled");
        getServer().getScheduler().cancelTasks(this);
    }
    
    public void reload(CommandSender sender) {
        String ssql  = mainconfig.database_type;
        boolean move = mainconfig.events_move;
        boolean misc = mainconfig.events_misc;
        
        clear();
        right.clear();
        left.clear();
        mods.clear();
        data.clear();
        friends.clear();
        limits.clear();
        
        messages.unload();
        mainconfig.unload();
        worldconfig.unload();
        
        messages.load();
        mainconfig.load();

        worldconfig.clear();

        if (!mainconfig.config_single) {
            for (World w : getServer().getWorlds()) { worldconfig.load(w); }
        } else {
            worldconfig.load(getServer().getWorlds().get(0));
        }
        loadIntegrations();
        
        String  newssql = mainconfig.database_type;
        boolean newmove = mainconfig.events_move;
        boolean newmisc = mainconfig.events_misc;

        if (!ssql.equals(newssql)) {
            
            try {
                database.disconnect(false);
            } catch (CoreDbException ex) {
                getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
            }
            
            try {
                database.connect();
            } catch (CoreDbException ex) {
                getCommunicator().error(Thread.currentThread(), ex, ex.getMessage());
            }

            database.load();

            msg(sender, "[TAG] Database Type: &4{0}&7 Defined.", database.type);
        }
        
        PluginManager pm = getServer().getPluginManager();
        if (move != newmove) {
            if (newmove) {
                pm.registerEvents(new CreativeMoveListener(), this);
                msg(sender, "[TAG] CreativeMoveListener registred, Listener enabled.");
            } else {
                HandlerList.unregisterAll(new CreativeMoveListener());
                msg(sender, "[TAG] CreativeMoveListener unregistered, Listener disabled.");
            }
        }

        if (misc != newmisc) {
            if (newmisc) {
                pm.registerEvents(new CreativeMiscListener(), this);
                msg(sender, "[TAG] CreativeMiscListener registred, Listener enabled.");
            } else {
                HandlerList.unregisterAll(new CreativeMoveListener());
                msg(sender, "[TAG] CreativeMiscListener unregistered, Listener disabled.");
            }
        }
    }
    
    public void loadIntegrations() {
        PluginManager pm = getServer().getPluginManager();
        Plugin p = pm.getPlugin("MobArena");
        if (p != null) {
            if (p.isEnabled()) {
                log("[TAG] MobArena support enabled!");
                pm.registerEvents(new MobArena(), this);
            }
        }

        p = pm.getPlugin("SurvivalGames");
        if (p != null) {
            if (p.isEnabled()) {
                log("[TAG] SurvivalGames support enabled!");
                pm.registerEvents(new SurvivalGames(), this);
            }
        }
        
        p = pm.getPlugin("Multiverse-Inventories");
        if (p != null) {
            if (p.isEnabled()) {
                if (mainconfig.config_conflict) {
                    mainconfig.data_inventory = false;
                    mainconfig.data_status = false;
                    int anoy = 5;
                    while (anoy > 0) {
                        log("[TAG] ***************************************************");
                        log("[TAG] Multiverse-Inventories Detected!!");
                        log("[TAG] Per-GameMode inventories will be disabled by this plugin");
                        log("[TAG] Use the multiverse inventories manager!");
                        log("[TAG] ***************************************************");   
                        anoy--;
                    }
                } else {
                    log("[TAG] ***************************************************");
                    log("[TAG] Multiverse-Inventories Detected!!");
                    log("[TAG] Per-GameMode inventories may be buggy!");
                    log("[TAG] Use the multiverse inventories manager!");
                    log("[TAG] ***************************************************");   
                }
            }
        }
    }

    @Override
    public boolean hasPerm(CommandSender sender, String node) {
        return ((sender instanceof Player)) ? permissions.hasPerm((Player)sender, "CreativeControl."+node) : true;
    }
    
    public String removeVehicle(UUID uuid) {
        String master = null;
        
        for (String key : limits.keySet()) {
            if (limits.get(key).contains(uuid)) {
                master = key;
                break;
            }
        }

        if (master == null) {
            return null;
        }
        
        HashSet<UUID> entity = limits.get(master);
        entity.remove(uuid);

        limits.put(master, entity);
        return master;
    }
    
    private void clear() {
        HashSet<UUID> entity = new HashSet<UUID>();

        for (String key : limits.keySet()) {
            entity.addAll(limits.get(key));
        }

        for (World w : getServer().getWorlds()) {
            for (Entity x : w.getEntities()) {
                if (entity.contains(x.getUniqueId())) {
                    x.remove();
                }
            }
        }
        
        entity.clear();
        limits.clear();
    }
    
    public void clear(Player player) {
        HashSet<UUID> entity = limits.get(player.getName());

        if (entity == null) {
            return;
        }

        for (World w : Bukkit.getWorlds()) {
            for (Entity x : w.getEntities()) {
                if (entity.contains(x.getUniqueId())) {
                    x.remove();
                }
            }
        }

        entity.clear();
        limits.remove(player.getName());
    }
    
    public static CreativePermissions getPermissions() {
        return permissions;
    }
    
    public static CreativeWorldConfig getWorldConfig() {
        return worldconfig;
    }

    public static CreativeWorldNodes getWorldNodes(World world) {
        return worldconfig.get(world);
    }

    public static CreativeControl getPlugin() { 
        return plugin; 
    }

    public static CreativeBlocksSelection getSelector() { 
        return selector; 
    }
    
    public static CreativePlayerFriends getFriends() {
        return friends;
    }

    @Override
    public CreativeSQLDatabase getDb() { 
        return CreativeControl.getDb2();
    }
    
    public static CreativeSQLDatabase getDb2() { 
        return database; 
    }
    
    public static CreativeRegionManager getRegioner() { 
        return regioner; 
    }
    
    public static CreativeMainConfig getMainConfig() {
        return mainconfig;
    }

    public static CreativeBlockManager getManager() { 
        return manager; 
    }
    
    public static CreativePlayerData getPlayerData() { 
        return data; 
    }

    public static CreativeMessages getMessages() {
        return messages;
    }
    
    public static Consumer getConsumer() { 
        return lbconsumer; 
    }

    public void setupLogBlock() {
        Plugin logblock = Bukkit.getPluginManager().getPlugin("LogBlock");
        if (logblock != null) {
            log("[TAG] LogBlock hooked as logging plugin");
            lbconsumer = ((LogBlock)logblock).getConsumer();
        }
    }

    public void setupWorldEdit() {
        Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (we != null && we.isEnabled()) {
            CreativeEditSessionFactory.setup();
        }
    }
    
    public WorldEditPlugin getWorldEdit() {
        PluginManager pm = getServer().getPluginManager();
        Plugin wex = pm.getPlugin("WorldEdit");
        return (WorldEditPlugin) wex;
    }

    public boolean isLoggedIn(Player player) {
        PluginManager pm = getServer().getPluginManager();

        if (pm.getPlugin("AuthMe") != null) {
            return AuthMe.isLoggedInComplete(player);
        }

        if (pm.getPlugin("xAuth") != null) {
            return xAuth.isLoggedIn(player);
        }

        return true;
    }

    private int survival = 0;
    private int creative = 0;
    private int useMove = 0;
    private int useMisc = 0;
    private int OwnBlock = 0;
    private int NoDrop = 0;
    
    private void startMetrics() {
        try {
            CreativeMetrics metrics = new CreativeMetrics(this);

            Graph dbType = metrics.createGraph("Database Type");
            dbType.addPlotter(new CreativeMetrics.Plotter(database.type.toString()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            for (CreativeRegion CR : regioner.getAreas()) {
                if (CR.type == CreativeMode.CREATIVE) {
                    creative++;
                } else {
                    survival++;
                }
            }
            
            Graph reg = metrics.createGraph("Regions");
            reg.addPlotter(new CreativeMetrics.Plotter("Regions") {
                    @Override
                    public int getValue() {
                            return creative+survival;
                    }
            });
            
            Graph reg1 = metrics.createGraph("Regions Type");
            reg1.addPlotter(new CreativeMetrics.Plotter("Creative") {
                    @Override
                    public int getValue() {
                            return creative;
                    }
            });
            
            reg1.addPlotter(new CreativeMetrics.Plotter("Survival") {

                    @Override
                    public int getValue() {
                            return survival;
                    }

            });
            
            if (mainconfig.events_move) {
                useMove++;
            }
            
            if (mainconfig.events_misc) {
                useMisc++;
            }
            
            Graph extra = metrics.createGraph("Extra Events");
            extra.addPlotter(new CreativeMetrics.Plotter("Move Event") {
                    @Override
                    public int getValue() {
                            return useMove;
                    }
            });
            
            extra.addPlotter(new CreativeMetrics.Plotter("Misc Protection") {
                    @Override
                    public int getValue() {
                            return useMisc;
                    }
            });
            
            for (World world : getServer().getWorlds()) {
                if (worldconfig.get(world).block_ownblock) {
                    OwnBlock++;
                } else
                if (worldconfig.get(world).block_nodrop) {
                    NoDrop++;
                }
            }
            
            Graph ptype = metrics.createGraph("Protection Type");
             ptype.addPlotter(new CreativeMetrics.Plotter("OwnBlocks") {
                    @Override
                    public int getValue() {
                            return OwnBlock;
                    }
            });
            
             ptype.addPlotter(new CreativeMetrics.Plotter("NoDrop") {
                    @Override
                    public int getValue() {
                            return NoDrop;
                    }
            });
            
            metrics.start();
        } catch (IOException e) {
        }
    }
    
    public void updateThread() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                newversion = getVersion(currentversion);
                
                double nv = CreativeUtil.toDouble(newversion.replaceAll("[^0-9]", ""));
                double od = CreativeUtil.toDouble(currentversion.replaceAll("[^0-9]", ""));

                if (od < nv) {
                    log("New Version Found: {0} (You have: {1})", newversion, currentversion);
                    log("Visit: http://bit.ly/creativecontrol/");
                    hasUpdate = true;
                }
            }
        }, 100, 21600 * 20);
    }
    
    public String getVersion(String current) {
        try {	
            URL url = new URL("http://dev.bukkit.org/server-mods/creativecontrol/files.rss");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode != null) {
                if (firstNode.getNodeType() == 1) {
                    Element firstElement = (Element)firstNode;
                    NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                    Element firstNameElement = (Element) firstElementTagName.item(0);
                    NodeList firstNodes = firstNameElement.getChildNodes();
                    return firstNodes.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            return current;
        }
        return current;
    }
}