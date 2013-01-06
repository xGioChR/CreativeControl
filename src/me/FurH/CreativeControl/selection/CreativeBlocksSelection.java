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

package me.FurH.CreativeControl.selection;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlocksSelection {
    private long elapsedTime = 0;
    private Location min = null;
    private Location max = null;

    public enum Type {
        DELALL, DELPLAYER, DELTYPE, ADD, ALLOW, TRANSFER;
    };

    public boolean allBlocks(CommandSender sender, final String args, final Type type) {
        final CreativeControl      plugin   = CreativeControl.getPlugin();
        final CreativeBlockManager manager  = CreativeControl.getManager();
        final CreativeCommunicator com      = CreativeControl.getCommunicator();
        final CreativeMessages     messages = CreativeControl.getMessages();
        final CreativeMainConfig   main     = CreativeControl.getMainConfig();
        
        if (!plugin.hasPerm(sender, "Commands.Use.others")) {
            if ((!args.equalsIgnoreCase(sender.getName()))) {
                com.msg(sender, messages.allblocks_othername);
                return true;
            }
        }
        
        int area = 0;
        if (!main.selection_usewe || getSelection((Player)sender) == null) {
            if (!plugin.left.containsKey((Player)sender) && !plugin.right.containsKey((Player)sender)) {
                com.msg(sender, messages.allblocks_selnull);
                return true;
            }
        
            final CreativeSelection sel = new CreativeSelection(plugin.left.get((Player)sender), plugin.right.get((Player)sender));
            
            area = sel.getArea();
            min = sel.getStart();
            max = sel.getEnd();
        } else {
            Selection sel = getSelection((Player)sender);
            
            if (sel == null) {
                com.msg(sender, messages.allblocks_selnull);
                return true;
            }
            
            area = sel.getArea();
            min = sel.getMinimumPoint();
            max = sel.getMaximumPoint();
        }

        com.msg(sender, messages.allblocks_selsize, area);
        com.msg(sender, messages.allblocks_while);
        
        final CreativeWorldNodes config = CreativeWorldConfig.get(min.getWorld());

        long startTimer = System.currentTimeMillis();
        final Player player = (Player) sender;
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            World world = min.getWorld();
                            
                            Location loc = new Location(world, x, y, z);
                            Block block = world.getBlockAt(loc);
                            
                            if (block.getType() == Material.AIR) { continue; }
                                                        
                            CreativeWorldNodes wconfig = CreativeWorldConfig.get(world);
                            
                            if (type == Type.DELALL) {
                                if (wconfig.block_ownblock) {
                                    String[] data = manager.getBlock(block);
                                    if (data != null) {
                                        if (manager.isOwner(player, data[0])) {
                                            manager.delBlock(block);
                                        }
                                    }
                                }
                                if (wconfig.block_nodrop) {
                                    if (plugin.hasPerm(player, "Command.NoDrop")) {
                                        manager.delBlock(block);
                                    }
                                }
                            } else
                            if (type == Type.DELPLAYER) {
                                if (wconfig.block_ownblock) {
                                    if (args.equalsIgnoreCase(player.getName())) {
                                        manager.delPlayer(args, block);
                                    } else {
                                        if (plugin.hasPerm(player, "OwnBlock.DelPlayer")) {
                                            manager.delPlayer(args, block);
                                        }
                                    }
                                }

                                if (wconfig.block_nodrop) {
                                    if (plugin.hasPerm(player, "Command.NoDrop")) {
                                        manager.delPlayer(args, block);
                                    }
                                }
                            } else
                            if (type == Type.DELTYPE) {
                                if (wconfig.block_ownblock) {
                                    String[] data = manager.getBlock(block);
                                    if (data != null) {
                                        if (manager.isOwner(player, data[0])) {
                                            manager.delType(args, block);
                                        }
                                    }
                                }
                                if (wconfig.block_nodrop) {
                                    if (plugin.hasPerm(player, "Command.NoDrop")) {
                                        manager.delType(args, block);
                                    }
                                }
                            } else
                            if (type == Type.ADD) {
                                String[] data = manager.getBlock(block);
                                if (data == null) {
                                    manager.addBlock(args, block, wconfig.block_nodrop);
                                }
                            } else
                            if (type == Type.ALLOW) {
                                String[] data = manager.getBlock(block);
                                if (data != null) {
                                    if (manager.isAllowed(player, data)) {
                                        String mod = args.toLowerCase();
                                        HashSet<String> als = new HashSet<String>();

                                        if (data.length > 0) {
                                            als = CreativeUtil.toStringHashSet(data[1], ", ");
                                        }
                                        
                                        if (mod.startsWith("-")) {
                                            mod = mod.substring(1);
                                            if (als.contains(mod)) {
                                                als.remove(mod);
                                                manager.update(CreativeUtil.getLocation(block.getLocation()), data[0], als.toString());
                                            }
                                        } else {
                                            if (!als.contains(mod)) {
                                                als.add(mod);
                                                manager.update(CreativeUtil.getLocation(block.getLocation()), data[0], als.toString());
                                            }
                                        }
                                    }
                                }
                            } else
                            if (type == Type.TRANSFER) {
                                String[] data = manager.getBlock(block);
                                if (data != null) {
                                    if (manager.isOwner(player, data[0])) {
                                        manager.delBlock(block);
                                        manager.addBlock(args, block, wconfig.block_nodrop);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        t.setPriority(4);
        t.start();

        elapsedTime = (System.currentTimeMillis() - startTimer);
        com.msg(sender, messages.allblocks_processed, elapsedTime);
        return true;
    }
    
    public Selection getSelection(Player p) {
        CreativeControl plugin = CreativeControl.getPlugin();
        WorldEditPlugin we = plugin.getWorldEdit();
        if (we != null) {
            return plugin.getWorldEdit().getSelection(p);
        }
        return null;
    }
}