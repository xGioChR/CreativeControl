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

package me.FurH.CreativeControl.listener;

import de.diddiz.LogBlock.Consumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.FurH.Core.blocks.BlockUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockData;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockListener implements Listener {
    
    /*
     * Block Place Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {        
        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        World world = p.getWorld();
        
        CreativeMessages        messages   = CreativeControl.getMessages();
        CreativeControl         plugin     = CreativeControl.getPlugin();
        CreativeWorldNodes      config     = CreativeControl.getWorldNodes(world);
        Communicator            com        = plugin.getCommunicator();

        /*
         * Excluded Worlds
         */
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (e.isCancelled()) {
                if (e.getBlockAgainst().getType() == Material.WALL_SIGN || e.getBlockAgainst().getType() == Material.SIGN_POST) {
                    Sign sign = (Sign)e.getBlockAgainst().getState();
                    if (CreativeUtil.isEconomySign(sign)) {
                        if (!plugin.hasPerm(p, "BlackList.EconomySigns")) {
                            com.msg(p, messages.mainode_restricted);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
        
        if (e.isCancelled()) { return; }

        /*
         * Gamemode Handler
         */
        CreativeMainConfig      main       = CreativeControl.getMainConfig();
        if (!main.events_move) {
            if (config.world_changegm) {
                if (p.getGameMode().equals(GameMode.CREATIVE)) {
                    if ((!config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                        com.msg(p, messages.region_creative_unallowed);
                        p.setGameMode(GameMode.SURVIVAL);
                        e.setCancelled(true);
                        return;
                    }
                } else 
                if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                    if ((config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                        com.msg(p, messages.region_survival_unallowed);
                        p.setGameMode(GameMode.CREATIVE);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            /*
             * Block Place BlackList
             */
            if ((config.black_place != null) && (config.black_place.contains(b.getTypeId()))) {
                if (!plugin.hasPerm(p, "BlackList.BlockPlace." + b.getTypeId())) {
                    com.msg(p, messages.blockplace_cantplace);
                    e.setCancelled(true);
                    return;
                }
            }
            
            /*
             * Anti Whiter Creation
             */
            if ((config.prevent_wither) && (!plugin.hasPerm(p, "Preventions.Wither"))) {
                if (e.getBlockPlaced().getType() == Material.SKULL) {
                    if ((world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND) &&
                            (world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.SOUL_SAND) &&
                            (world.getBlockAt(b.getX() + 1, b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND) &&
                            (world.getBlockAt(b.getX() - 1, b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND) ||
                            (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() - 1).getType() == Material.SOUL_SAND) &&
                            (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() + 1).getType() == Material.SOUL_SAND)) {
                        com.msg(p, messages.mainode_restricted);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            
            /*
             * Anti SnowGolem Creation
             */
            if ((config.prevent_snowgolem) && (!plugin.hasPerm(p, "Preventions.SnowGolem")) && 
                    ((b.getType() == Material.PUMPKIN) || (b.getType() == Material.JACK_O_LANTERN)) &&
                    (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.SNOW_BLOCK) &&
                    (world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.SNOW_BLOCK)) {
                com.msg(p, messages.mainode_restricted);
                e.setCancelled(true);
                return;
            }
            
            /*
             * Anti IronGolem Creation
             */
            if ((config.prevent_irongolem) && (!plugin.hasPerm(p, "Preventions.IronGolem")) && 
                    ((b.getType() == Material.PUMPKIN) || (b.getType() == Material.JACK_O_LANTERN)) && 
                    (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK) &&
                    (world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.IRON_BLOCK) &&
                    (((world.getBlockAt(b.getX() + 1, b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK) &&
                    (world.getBlockAt(b.getX() - 1, b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK)) ||
                    ((world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() + 1).getType() == Material.IRON_BLOCK) &&
                    (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() - 1).getType() == Material.IRON_BLOCK)))) {
                com.msg(p, messages.mainode_restricted);
                e.setCancelled(true);
                return;
            }
        }

        CreativeBlockManager    manager    = CreativeControl.getManager();

        Block r = e.getBlockReplacedState().getBlock();
        Block ba = e.getBlockAgainst();

        if (config.block_nodrop) {
            if (config.misc_liquid) {
                if (r.getType() != Material.AIR) {
                    manager.unprotect(r);
                }
            } 
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if (!plugin.hasPerm(p, "NoDrop.DontSave")) {
                    manager.protect(p, b);
                }
            }
        } else
        if (config.block_ownblock) {
            if (config.misc_liquid) {
                if (r.getType() != Material.AIR) {
                    CreativeBlockData data = manager.isprotected(r, true);
                    if (data != null) {
                        if (manager.isAllowed(p, data)) {
                            manager.unprotect(b);
                        } else {
                            com.msg(p, messages.blockmanager_belongs, data.owner);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            if (config.block_against) {
                CreativeBlockData data = manager.isprotected(ba, true);
                if (data != null) {
                    if (!manager.isAllowed(p, data)) {
                        com.msg(p, messages.blockmanager_belongs, data.owner);
                        e.setCancelled(true);
                        return;
                    }
                }
            }

            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if (!plugin.hasPerm(p, "OwnBlock.DontSave")) {
                    manager.protect(p, b);
                }
            }
        }
    }

    
    /*
     * Block Break Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        World world = b.getWorld();

        CreativeControl         plugin     = CreativeControl.getPlugin();
        CreativeMessages        messages   = CreativeControl.getMessages();
        CreativeWorldNodes      config     = CreativeControl.getWorldNodes(world);
        CreativeBlockManager    manager    = CreativeControl.getManager();
        Communicator            com        = plugin.getCommunicator();

        if (config.world_exclude) {
            return;
        }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (e.isCancelled()) {
                if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                    Sign sign = (Sign)b.getState();
                    if (CreativeUtil.isEconomySign(sign)) {
                        if (!plugin.hasPerm(p, "BlackList.EconomySigns")) {
                            com.msg(p, messages.mainode_restricted);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
        
        if (e.isCancelled()) { return; }
        /*
         * Gamemode Handler
         */
        CreativeMainConfig      main       = CreativeControl.getMainConfig();
        if (!main.events_move) {
            if (config.world_changegm) {
                if (p.getGameMode().equals(GameMode.CREATIVE)) {
                    if ((!config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                        com.msg(p, messages.region_creative_unallowed);
                        p.setGameMode(GameMode.SURVIVAL);
                        e.setCancelled(true);
                        return;
                    }
                } else 
                if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                    if ((config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                        com.msg(p, messages.region_survival_unallowed);
                        p.setGameMode(GameMode.CREATIVE);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        /*
         * Anti BedRock Breaking
         */
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if ((config.prevent_bedrock) && (!plugin.hasPerm(p, "Preventions.BreakBedRock"))) {
                if (b.getType() == Material.BEDROCK) {
                    if (b.getY() < 1) {
                        com.msg(p, messages.blockbreak_survival);
                        e.setCancelled(true);
                        return;
                    }
                }
            }

            /*
             * Block Break BlackList
             */
            if ((config.black_break != null) && (config.black_break.contains(b.getTypeId()))) {
                if (!plugin.hasPerm(p, "BlackList.BlockBreak." + b.getTypeId())) {
                    com.msg(p, messages.blockbreak_cantbreak);
                    e.setCancelled(true);
                    return;
                }
            }
        }
                
        if (config.block_nodrop) {
            List<Block> attached = new ArrayList<Block>();

            if (config.block_attach) {
                
                if (b.getRelative(BlockFace.UP).getType() == Material.SAND || b.getRelative(BlockFace.UP).getType() == Material.GRAVEL) {
                    attached.add(b.getRelative(BlockFace.UP));
                }
                
                attached.addAll(BlockUtils.getAttachedBlock(b));
            }
            
            attached.add(b);
            for (Block block : attached) {
                CreativeBlockData data = manager.isprotected(block, false);

                if (data != null) {
                    process(config, e, block, p);
                }
            }
        } else
        if (config.block_ownblock) {
            HashSet<Block> attached = new HashSet<Block>();
            attached.add(b);

            if (config.block_attach) {
                attached.addAll(BlockUtils.getAttachedBlock(b));
            }

            for (Block block : attached) {
                CreativeBlockData data = manager.isprotected(block, false);

                if (data != null) {
                    if (manager.isAllowed(p, data)) {
                        com.msg(p, messages.blockmanager_belongs, data.owner);
                        e.setCancelled(true);
                        break;
                    } else {
                        process(config, e, block, p);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) { return; }
        
        World world = e.getBlock().getWorld();
        CreativeBlockManager    manager    = CreativeControl.getManager();
        CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        
        if (config.block_pistons) {
            for (Block b : e.getBlocks()) {
                if (b.getType() == Material.AIR) { return; }
                if (manager.isprotected(b, true) != null) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) { return; }
                
        Block b = e.getBlock();
        World world = b.getWorld();

        if (b.getType() == Material.AIR) { return; }
        if (!e.isSticky()) { return; }
        
        CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        
        if (config.block_pistons) {
            BlockFace direction = null;
            MaterialData data = b.getState().getData();

            if (data instanceof PistonBaseMaterial) {
                direction = ((PistonBaseMaterial) data).getFacing();
            }
            
            if (direction == null) { return; }
            Block moved = b.getRelative(direction, 2);
            CreativeBlockManager    manager    = CreativeControl.getManager();
            if (manager.isprotected(moved, true) != null) {
                e.setCancelled(true);
            }
        }
    }
    
    public void logBlock(Player p, Block b) {
        Consumer                consumer   = CreativeControl.getConsumer();
        if (consumer != null) {
            consumer.queueBlockBreak(p.getName(), b.getState());
        }
    }

    private void process(CreativeWorldNodes config, BlockBreakEvent e, Block b, Player p) {
        if (!e.isCancelled()) {
            CreativeMessages        messages   = CreativeControl.getMessages();
            CreativeBlockManager    manager    = CreativeControl.getManager();
            Communicator            com        = CreativeControl.plugin.getCommunicator();
            
            if (config.block_creative) {
                if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                    com.msg(p, messages.blockbreak_cantbreak);
                    e.setCancelled(true);
                    return;
                }
            }
            
            manager.unprotect(b);
            logBlock(p, b);
            e.setExpToDrop(0);
            b.setType(Material.AIR);

            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                com.msg(p, messages.blockbreak_creativeblock);
            }
        }
    }
}