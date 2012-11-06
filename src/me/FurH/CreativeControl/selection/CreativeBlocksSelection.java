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

import java.util.ArrayList;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.database.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlocksSelection {
    private long elapsedTime = 0;
    private Vector vector;

    public enum Type {
        DELALL, DELPLAYER, DELTYPE, ADD, ALLOW, TRANSFER;
    };

    public boolean allBlocks(CommandSender sender, final String args, final Type type) {
        final CreativeControl      plugin   = CreativeControl.getPlugin();
        final CreativeBlockManager manager  = CreativeControl.getManager();
        final CreativeCommunicator com      = CreativeControl.getCommunicator();
        final CreativeMessages     messages = CreativeControl.getMessages();
        
        if (!plugin.hasPerm(sender, "Commands.Use.others")) {
            if ((!args.equalsIgnoreCase(sender.getName()))) {
                com.msg(sender, messages.allblocks_othername);
                return false;
            }
        }
        
        if (!plugin.left.containsKey((Player)sender) && !plugin.right.containsKey((Player)sender)) {
            com.msg(sender, messages.allblocks_selnull);
            return false;
        }
        
        final CreativeSelection sel = new CreativeSelection(plugin.left.get((Player)sender), plugin.right.get((Player)sender));
        
        vector = sel.getVector();
        
        com.msg(sender, messages.allblocks_selsize, sel.getArea());
        com.msg(sender, messages.allblocks_while);

        long startTimer = System.currentTimeMillis();
        final Player player = (Player) sender;
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = 0; x <= Math.abs(vector.getBlockX()); x++) {
                    for (int y = 0; y <= Math.abs(vector.getBlockY()); y++) {
                        for (int z = 0; z <= Math.abs(vector.getBlockZ()); z++) {
                            Location start = sel.getStart();
                            Block block = start.getWorld().getBlockAt(start.getBlockX() + x, start.getBlockY() + y, start.getBlockZ() + z);
                            if (block.getType() == Material.AIR) { continue; }
                            
                            World world = block.getWorld();
                            
                            CreativeWorldNodes wconfig = CreativeWorldConfig.get(world);
                            
                            if (type == Type.DELALL) {
                                if (wconfig.block_ownblock) {
                                    String[] data = manager.getBlock(block);
                                    if (data != null) {
                                        if (manager.isOwner(player, data[0])) {
                                            manager.delBlock(block, data);
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
                                    manager.addBlock(args, block);
                                }
                            } else
                            if (type == Type.ALLOW) {
                                String[] data = manager.getBlock(block);
                                if (data != null) {
                                    if (manager.isAllowed(player, data)) {
                                        String mod = args.toLowerCase();
                                        List<String> als = new ArrayList<String>();

                                        if (data.length > 0) {
                                            als = CreativeUtil.toStringList(data[1], ", ");
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
                                        manager.delBlock(block, data);
                                        manager.addBlock(args, block);
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
        return false;
    }
}