/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
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
package me.FurH.CreativeControl.integration.worldedit;

import java.lang.reflect.Method;

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.Functions;
import net.coreprotect.worldedit.WorldEdit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;

/** TODO */
@SuppressWarnings("deprecation")
public class CreativeEditSession extends EditSession {

    private LocalPlayer player;

    public CreativeEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        super(world, maxBlocks);
        this.player = player;
    }

    public CreativeEditSession(LocalWorld world, int maxBlocks, com.sk89q.worldedit.extent.inventory.BlockBag blockBag, LocalPlayer player) {
        super(world, maxBlocks, blockBag);
        this.player = player;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {

        if (!(world instanceof BukkitWorld)) {
            return super.rawSetBlock(pt, block);
        }

        World w = ((BukkitWorld) world).getWorld();

        CreativeWorldNodes config = CreativeControl.getWorldNodes(w);
        CreativeBlockManager manager = CreativeControl.getManager();

        int oldType = w.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        byte oldData = w.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();

        BlockState oldState = null;
        if (oldType == Material.SIGN_POST.getId() || oldType == Material.SIGN.getId()) {
            oldState = w.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
        }

        Block block_ = ((BukkitWorld)player.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        BlockState block_state = block_.getState();
        ItemStack[] container_contents = getContainerContents(block_);

        boolean success = super.rawSetBlock(pt, block);

        if (success) {

            logBlock(pt, block, oldType, oldData, oldState);
            prism(pt, block, oldType, oldData);
            coreprotect(pt, block, block_state, block_, container_contents);

            if (!config.world_exclude && config.block_worledit) {
                int newType = block.getType();

                if (newType == 0 || oldType != 0) {
                    manager.unprotect(w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                }

                if (newType != 0) {
                    
                    if (config.block_ownblock) {
                        if (!player.hasPermission("CreativeControl.OwnBlock.DontSave")) {
                            manager.protect(player.getName(), w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                        }
                    }

                    if (config.block_nodrop) {
                        if (!player.hasPermission("CreativeControl.NoDrop.DontSave")) {
                            manager.protect(player.getName(), w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                        }
                    }   
                }
            }
        }

        return success;
    }
    
    public void coreprotect(Vector vector, BaseBlock base_block, BlockState block_state, Block block, ItemStack[] container_contents) {

        CoreProtectAPI protect = CreativeControl.getCoreProtect();

        if (protect == null) {
            return;
        }

        if (Functions.checkConfig(block.getWorld(), "worldedit") == 0) {
            return;
        }

        try {

            Method log = WorldEdit.class.getDeclaredMethod("logData", LocalPlayer.class, Vector.class, Block.class, BlockState.class, ItemStack[].class);
            log.setAccessible(true);
            log.invoke(null, player, vector, block, block_state, container_contents);

        } catch (Exception ex) {
            
            String methods = "";
            
            for (Method m : WorldEdit.class.getDeclaredMethods()) {
                methods += m.getName() + ", ";
            }
            
            System.out.println("[ " + ex.getClass().getSimpleName() + " ]: Failed to invoke logData method, Available: " + methods);
        }
    }

    private ItemStack[] getContainerContents(Block block) {
        CoreProtectAPI protect = CreativeControl.getCoreProtect();

        if (protect == null) {
            return null;
        }

        return Functions.getContainerContents(block);
    }

    public void prism(Vector pt, BaseBlock block, int typeBefore, byte dataBefore) {
        if (CreativeControl.getPrism()) {

            if (!Prism.config.getBoolean("prism.tracking.world-edit")) {
                return;
            }

            Location loc = new Location(Bukkit.getWorld(player.getWorld().getName()), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            RecordingQueue.addToQueue(ActionFactory.create("world-edit", loc, typeBefore, dataBefore, loc.getBlock().getTypeId(), loc.getBlock().getData(), player.getName()));
        }
    }

    public void logBlock(Vector pt, BaseBlock block, int typeBefore, byte dataBefore, BlockState stateBefore) {
        Consumer consumer = CreativeControl.getLogBlock();

        if (consumer != null) {

            if (!(Config.isLogging(player.getWorld().getName(), Logging.WORLDEDIT))) {
                return;
            }

            Location location = new Location(((BukkitWorld) player.getWorld()).getWorld(), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (Config.isLogging(location.getWorld().getName(), Logging.SIGNTEXT) && (typeBefore == Material.SIGN_POST.getId() || typeBefore == Material.SIGN.getId())) {
                consumer.queueSignBreak(player.getName(), (Sign) stateBefore);
                if (block.getType() != Material.AIR.getId()) {
                    consumer.queueBlockPlace(player.getName(), location, block.getType(), (byte) block.getData());
                }
            } else {
                if (dataBefore != 0) {
                    consumer.queueBlockBreak(player.getName(), location, typeBefore, dataBefore);
                    if (block.getType() != Material.AIR.getId()) {
                        consumer.queueBlockPlace(player.getName(), location, block.getType(), (byte) block.getData());
                    }
                } else {
                    consumer.queueBlock(player.getName(), location, typeBefore, block.getType(), (byte) block.getData());
                }
            }
        }
    }
}
