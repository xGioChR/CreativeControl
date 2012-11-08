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
        nodes.world_creative        = getBoolean(w, "World.Creative");
        nodes.world_exclude         = getBoolean(w, "World.Exclude");
        nodes.world_changegm        = getBoolean(w, "World.ChangeGameMode");

        nodes.black_cmds            = getStringList(w, "BlackList.Commands");
        nodes.black_place           = getIntegerList(w, "BlackList.BlockPlace");
        nodes.black_break           = getIntegerList(w, "BlackList.BlockBreak");
        nodes.black_use             = getIntegerList(w, "BlackList.ItemUse");
        nodes.black_interact        = getIntegerList(w, "BlackList.ItemInteract");

        nodes.misc_tnt              = getBoolean(w, "MiscProtection.NoTNTExplosion");
        nodes.misc_ice              = getBoolean(w, "MiscProtection.IceMelt");
        nodes.misc_liquid           = getBoolean(w, "MiscProtection.LiquidControl");
        nodes.misc_fire             = getBoolean(w, "MiscProtection.Fire");

        nodes.block_worledit        = getBoolean(w, "BlockProtection.WorldEdit");
        nodes.block_ownblock        = getBoolean(w, "BlockProtection.OwnBlocks");
        nodes.block_nodrop          = getBoolean(w, "BlockProtection.NoDrop");
        nodes.block_explosion       = getBoolean(w, "BlockProtection.Explosions");
        nodes.block_creative        = getBoolean(w, "BlockProtection.CreativeOnly");
        nodes.block_pistons         = getBoolean(w, "BlockProtection.Pistons");
        nodes.block_pistons         = getBoolean(w, "BlockProtection.BlockAgainst");
        nodes.block_invert          = getBoolean(w, "BlockProtection.inverted");
        nodes.block_exclude         = getIntegerList(w, "BlockProtection.exclude");

        nodes.prevent_drop          = getBoolean(w, "Preventions.ItemDrop");
        nodes.prevent_pickup        = getBoolean(w, "Preventions.ItemPickup");
        nodes.prevent_pvp           = getBoolean(w, "Preventions.PvP");
        nodes.prevent_mobs          = getBoolean(w, "Preventions.Mobs");
        nodes.prevent_eggs          = getBoolean(w, "Preventions.Eggs");
        nodes.prevent_target        = getBoolean(w, "Preventions.Target");
        nodes.prevent_mobsdrop      = getBoolean(w, "Preventions.MobsDrop");
        nodes.prevent_irongolem     = getBoolean(w, "Preventions.IronGolem");
        nodes.prevent_snowgolem     = getBoolean(w, "Preventions.SnowGolem");
        nodes.prevent_wither        = getBoolean(w, "Preventions.Wither");
        nodes.prevent_drops         = getBoolean(w, "Preventions.ClearDrops");
        nodes.prevent_enchant       = getBoolean(w, "Preventions.Enchantments");
        nodes.prevent_mcstore       = getBoolean(w, "Preventions.MineCartStorage");
        nodes.prevent_bedrock       = getBoolean(w, "Preventions.BreakBedRock");
        nodes.prevent_invinteract   = getBoolean(w, "Preventions.InvInteract");
        nodes.prevent_bonemeal      = getBoolean(w, "Preventions.Bonemeal");
        nodes.prevent_villager      = getBoolean(w, "Preventions.InteractVillagers");
        nodes.prevent_potion        = getBoolean(w, "Preventions.PotionSplash");
        nodes.prevent_frame         = getBoolean(w, "Preventions.ItemFrame");
        nodes.prevent_economy       = getBoolean(w, "Preventions.EconomySign");
        nodes.prevent_vehicle       = getBoolean(w, "Preventions.VehicleDrop");
        nodes.prevent_limitvechile  = getInteger(w, "Preventions.VehicleLimit");

        CreativeMainConfig   main   = CreativeControl.getMainConfig();
        if (!main.config_single) {
            cache.put(w.getName(), nodes);
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
