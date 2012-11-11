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

package me.FurH.CreativeControl.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeWorldConfig {
    private static Map<String, CreativeWorldNodes> cache = new ConcurrentHashMap<String, CreativeWorldNodes>();
    private static CreativeWorldNodes nodes = new CreativeWorldNodes();
        
    public static CreativeWorldNodes get(World w) {
        CreativeMainConfig   main   = CreativeControl.getMainConfig();
        if (main.config_single) {
            return nodes;
        } else {
            CreativeWorldNodes n = cache.get(w.getName());
            if (n == null) { load(w); }
            return n;
        }
    }
    
    public static void clear() {
        cache.clear();
    }

    public static void load(World w) {
        CreativeWorldNodes x = new CreativeWorldNodes();
        x.world_creative        = getBoolean(w, "World.Creative");
        x.world_exclude         = getBoolean(w, "World.Exclude");
        x.world_changegm        = getBoolean(w, "World.ChangeGameMode");

        x.black_cmds            = getStringList(w, "BlackList.Commands");
        x.black_place           = getIntegerList(w, "BlackList.BlockPlace");
        x.black_break           = getIntegerList(w, "BlackList.BlockBreak");
        x.black_use             = getIntegerList(w, "BlackList.ItemUse");
        x.black_interact        = getIntegerList(w, "BlackList.ItemInteract");
        x.black_inventory       = getIntegerList(w, "BlackList.Inventory");
        x.black_sign            = getStringList(w, "BlackList.EconomySigns");

        x.misc_tnt              = getBoolean(w, "MiscProtection.NoTNTExplosion");
        x.misc_ice              = getBoolean(w, "MiscProtection.IceMelt");
        x.misc_liquid           = getBoolean(w, "MiscProtection.LiquidControl");
        x.misc_fire             = getBoolean(w, "MiscProtection.Fire");

        x.block_worledit        = getBoolean(w, "BlockProtection.WorldEdit");
        x.block_ownblock        = getBoolean(w, "BlockProtection.OwnBlocks");
        x.block_nodrop          = getBoolean(w, "BlockProtection.NoDrop");
        x.block_explosion       = getBoolean(w, "BlockProtection.Explosions");
        x.block_creative        = getBoolean(w, "BlockProtection.CreativeOnly");
        x.block_pistons         = getBoolean(w, "BlockProtection.Pistons");
        x.block_pistons         = getBoolean(w, "BlockProtection.BlockAgainst");
        x.block_invert          = getBoolean(w, "BlockProtection.inverted");
        x.block_exclude         = getIntegerList(w, "BlockProtection.exclude");

        x.prevent_drop          = getBoolean(w, "Preventions.ItemDrop");
        x.prevent_pickup        = getBoolean(w, "Preventions.ItemPickup");
        x.prevent_pvp           = getBoolean(w, "Preventions.PvP");
        x.prevent_mobs          = getBoolean(w, "Preventions.Mobs");
        x.prevent_eggs          = getBoolean(w, "Preventions.Eggs");
        x.prevent_target        = getBoolean(w, "Preventions.Target");
        x.prevent_mobsdrop      = getBoolean(w, "Preventions.MobsDrop");
        x.prevent_irongolem     = getBoolean(w, "Preventions.IronGolem");
        x.prevent_snowgolem     = getBoolean(w, "Preventions.SnowGolem");
        x.prevent_wither        = getBoolean(w, "Preventions.Wither");
        x.prevent_drops         = getBoolean(w, "Preventions.ClearDrops");
        x.prevent_enchant       = getBoolean(w, "Preventions.Enchantments");
        x.prevent_mcstore       = getBoolean(w, "Preventions.MineCartStorage");
        x.prevent_bedrock       = getBoolean(w, "Preventions.BreakBedRock");
        x.prevent_invinteract   = getBoolean(w, "Preventions.InvInteract");
        x.prevent_bonemeal      = getBoolean(w, "Preventions.Bonemeal");
        x.prevent_villager      = getBoolean(w, "Preventions.InteractVillagers");
        x.prevent_potion        = getBoolean(w, "Preventions.PotionSplash");
        x.prevent_frame         = getBoolean(w, "Preventions.ItemFrame");
        x.prevent_vehicle       = getBoolean(w, "Preventions.VehicleDrop");
        x.prevent_limitvechile  = getInteger(w, "Preventions.VehicleLimit");
        x.prevent_stacklimit    = getInteger(w, "Preventions.StackLimit");

        CreativeMainConfig   main   = CreativeControl.getMainConfig();
        if (!main.config_single) {
            cache.put(w.getName(), x);
        }
    }
    
    /*
     * return a Boolean from the settings file
     */
    private static boolean getBoolean(World w, String node) {
        return Boolean.parseBoolean(getSetting(node, w));
    }
    
    /*
     * return a Integer from the settings file
     */
    private static int getInteger(World w, String node) {
        return Integer.parseInt(getSetting(node, w));
    }

    /*
     * return a List from the Settings file
     */
    private static List<String> getStringList(World w, String node) {
        return Arrays.asList(getSetting(node, w).replaceAll(" ", "").split(","));
    }
    
    private static List<Integer> getIntegerList(World w, String node) {
        return CreativeUtil.toIntegerList(getSetting(node, w).replaceAll(" ", ""), ",");
    }

    /*
     * return an Object from the Settings file
     */
    private static String getSetting(String node, World w) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder() + File.separator + "worlds", w != null ? w.getName() + ".yml" : "world.yml");
        if (!dir.exists()) { CreativeUtil.ccFile(plugin.getResource("world.yml"), dir); }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            if (!config.contains(node)) {
                InputStream resource = plugin.getResource("world.yml");
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.get(node));
                    com.log("[TAG] Settings file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log("[TAG] Can't get setting node: {0}, contact the developer.", CreativeCommunicator.Type.SEVERE, node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error("[TAG] Can't update the settings file: {0}", ex, ex.getMessage());
                }
            }
        } catch (IOException e) {
            com.error("[TAG] Can't load the settings file: {0}", e, e.getMessage());
        } catch (InvalidConfigurationException ex) {
            com.error("[TAG] Can't load the settings file: {0}", ex, ex.getMessage());
            com.log("[TAG] You have a broken node in your settings file at: {0}", node);
        }
        
        String value = config.getString(node);
        if (value == null || "".equals(value)) {
            com.log(CreativeControl.tag + " You have a missing setting node at: {0}", CreativeCommunicator.Type.SEVERE, node);
            value = node;
        }
        return value;
    }
}
