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

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeLRUCache;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeWorldConfig extends CreativeMainConfig {
    private static CreativeLRUCache<String, CreativeWorldNodes> cache = new CreativeLRUCache<String, CreativeWorldNodes>(100);
    private static CreativeWorldNodes nodes = new CreativeWorldNodes();
    private static CreativeMainConfig config = null;
    
    public static void setConfig(CreativeMainConfig main) {
        config = main;
    }
    
    public CreativeWorldConfig() {
        super();
    }
        
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

    public static int clear() {
        int total = 0;
        
        total += cache.size();
        cache.clear();
        
        return total;
    }

    public static void load(World w) {
        CreativeMainConfig   main   = CreativeControl.getMainConfig();
        
        CreativeWorldNodes x = new CreativeWorldNodes();

        x.world_creative        = config.getBoolean(w, "World.Creative");
        x.world_exclude         = config.getBoolean(w, "World.Exclude");
        x.world_changegm        = config.getBoolean(w, "World.ChangeGameMode");
        x.world_nodrop          = config.getBoolean(w, "World.GlobalNoDrop");

        x.black_cmds            = config.getStringList(w, "BlackList.Commands");
        x.black_place           = config.getIntegerList(w, "BlackList.BlockPlace");
        x.black_break           = config.getIntegerList(w, "BlackList.BlockBreak");
        x.black_use             = config.getIntegerList(w, "BlackList.ItemUse");
        x.black_interact        = config.getIntegerList(w, "BlackList.ItemInteract");
        x.black_inventory       = config.getIntegerList(w, "BlackList.Inventory");
        x.black_sign            = config.getStringList(w, "BlackList.EconomySigns");
        x.black_sign_all        = false;
        
        x.misc_tnt              = config.getBoolean(w, "MiscProtection.NoTNTExplosion");
        x.misc_ice              = config.getBoolean(w, "MiscProtection.IceMelt");
        x.misc_liquid           = config.getBoolean(w, "MiscProtection.LiquidControl");
        x.misc_fire             = config.getBoolean(w, "MiscProtection.Fire");

        x.block_worledit        = config.getBoolean(w, "BlockProtection.WorldEdit");
        x.block_ownblock        = config.getBoolean(w, "BlockProtection.OwnBlocks");
        x.block_nodrop          = config.getBoolean(w, "BlockProtection.NoDrop");
        x.block_explosion       = config.getBoolean(w, "BlockProtection.Explosions");
        x.block_creative        = config.getBoolean(w, "BlockProtection.CreativeOnly");
        x.block_pistons         = config.getBoolean(w, "BlockProtection.Pistons");
        x.block_against         = config.getBoolean(w, "BlockProtection.BlockAgainst");
        x.block_attach          = config.getBoolean(w, "BlockProtection.CheckAttached");
        x.block_invert          = config.getBoolean(w, "BlockProtection.inverted");
        x.block_exclude         = config.getIntegerList(w, "BlockProtection.exclude");

        x.prevent_drop          = config.getBoolean(w, "Preventions.ItemDrop");
        x.prevent_pickup        = config.getBoolean(w, "Preventions.ItemPickup");
        x.prevent_pvp           = config.getBoolean(w, "Preventions.PvP");
        x.prevent_mobs          = config.getBoolean(w, "Preventions.Mobs");
        x.prevent_eggs          = config.getBoolean(w, "Preventions.Eggs");
        x.prevent_target        = config.getBoolean(w, "Preventions.Target");
        x.prevent_mobsdrop      = config.getBoolean(w, "Preventions.MobsDrop");
        x.prevent_irongolem     = config.getBoolean(w, "Preventions.IronGolem");
        x.prevent_snowgolem     = config.getBoolean(w, "Preventions.SnowGolem");
        x.prevent_wither        = config.getBoolean(w, "Preventions.Wither");
        x.prevent_drops         = config.getBoolean(w, "Preventions.ClearDrops");
        x.prevent_enchant       = config.getBoolean(w, "Preventions.Enchantments");
        x.prevent_mcstore       = config.getBoolean(w, "Preventions.MineCartStorage");
        x.prevent_bedrock       = config.getBoolean(w, "Preventions.BreakBedRock");
        x.prevent_invinteract   = config.getBoolean(w, "Preventions.InvInteract");
        x.prevent_bonemeal      = config.getBoolean(w, "Preventions.Bonemeal");
        x.prevent_villager      = config.getBoolean(w, "Preventions.InteractVillagers");
        x.prevent_potion        = config.getBoolean(w, "Preventions.PotionSplash");
        x.prevent_frame         = config.getBoolean(w, "Preventions.ItemFrame");
        x.prevent_vehicle       = config.getBoolean(w, "Preventions.VehicleDrop");
        x.prevent_limitvechile  = config.getInteger(w, "Preventions.VehicleLimit");
        x.prevent_stacklimit    = config.getInteger(w, "Preventions.StackLimit");
        x.prevent_open          = config.getBoolean(w, "Preventions.InventoryOpen");

        if (!main.config_single) {
            cache.put(w.getName(), x);
        } else {
            nodes = x;
        }
    }
}
