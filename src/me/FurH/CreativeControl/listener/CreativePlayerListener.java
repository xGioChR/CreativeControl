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

import com.sk89q.worldedit.bukkit.selections.Selection;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.CreativePlayerData;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.integration.worldedit.CreativeWorldEditHook;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent e) {
        if (e.isCancelled()) { return; }
        
        final Player player = e.getPlayer();
        final GameMode newgm = e.getNewGameMode();
        final GameMode oldgm = player.getGameMode();

        CreativeMainConfig    config   = CreativeControl.getMainConfig();
        CreativeControl       plugin   = CreativeControl.getPlugin();
        if (config.data_inventory) {
            if (!plugin.hasPerm(player, "Data.Status")) {
                InventoryView view = player.getOpenInventory();
                view.close();
                if (plugin.isLoggedIn(player)) {
                    CreativePlayerData    data     = CreativeControl.getPlayerData();
                    data.process(player, newgm, oldgm);
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    /*
     * Player Command Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) { return; }
        Player p = e.getPlayer();
        String msg = e.getMessage().toLowerCase();
        World world = p.getWorld();

        /*
         * Command Black List
         */
        CreativeControl       plugin   = CreativeControl.getPlugin();
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (!plugin.hasPerm(p, "BlackList.Commands")) {
                for (String cmd : msg.split(" ")) {
                    if (config.black_cmds.contains(cmd)) {
                        CreativeCommunicator  com      = CreativeControl.getCommunicator();
                        CreativeMessages      messages = CreativeControl.getMessages();
                        com.msg(p, messages.player_cmdblacklist);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        
        if (config.block_worledit) {
            if (plugin.getWorldEdit() != null) {
                CreativeWorldEditHook weh      = CreativeControl.getWorldEditHook();
                Selection select = plugin.getWorldEdit().getSelection(p);
                if (msg.startsWith("//set")) {
                    weh.saveBlocks(select, p);
                } else
                if (msg.startsWith("//undo")) {
                    weh.delBlocks(select, p);
                } else
                if (msg.startsWith("//redo")) {
                    weh.saveBlocks(select, p);
                }
            }
        }
    }
    
    /*
     * Player Death Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        World world = p.getWorld();
        
        /*
         * Clear drops on creative death
         */
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if ((p.getGameMode().equals(GameMode.CREATIVE)) && (config.prevent_drops)) {
            CreativeControl       plugin   = CreativeControl.getPlugin();
            if (!plugin.hasPerm(p, "Preventions.ClearDrops")) {
                e.getDrops().clear();
            }
        }
    }
    
    /*
     * Player Enchant Item Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnchantItemEvent(EnchantItemEvent e) {
        if (e.isCancelled()) { return; }
        Player p = e.getEnchanter();
        World world = p.getWorld();

        /*
         * Prevent Creative Player Enchant Items
         */
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            CreativeControl       plugin   = CreativeControl.getPlugin();
            if ((!plugin.hasPerm(p, "Preventions.Enchantments")) && (config.prevent_enchant)) {
                e.setCancelled(true);
            }
        }
    }

    /*
     * Inventory Click Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) { return; }
        if (!(e.getWhoClicked() instanceof Player)) { return; }
        Player p = (Player)e.getWhoClicked();
        World world = p.getWorld();

        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            CreativeControl       plugin   = CreativeControl.getPlugin();
            if (config.prevent_invinteract) {
                if (!plugin.hasPerm(p, "Preventions.InventoryInteract")) {
                    int slot = e.getRawSlot();
                    if (e.getInventory().getType() == InventoryType.PLAYER) {
                        if (!((slot >= 36) && (slot <= 44))) {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            }
            
            if (!plugin.hasPerm(p, "Preventions.StackLimit")) {
                int stacklimit = config.prevent_stacklimit;
                ItemStack current = e.getCurrentItem();
                if (current.getAmount() > stacklimit) {
                    current.setAmount(stacklimit);
                }
                
                ItemStack cursor = e.getCursor();
                if (cursor.getAmount() > stacklimit) {
                    cursor.setAmount(stacklimit);
                }

                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getAmount() > stacklimit) {
                            item.setAmount(stacklimit);
                        }
                    }
                }
            }
            
            if (!plugin.hasPerm(p, "BlackList.Inventory")) {
                ItemStack current = e.getCurrentItem();
                if (config.black_inventory.contains(current.getTypeId())) {
                    p.getInventory().remove(current);
                    e.setCancelled(true);
                }
                
                ItemStack cursor = e.getCursor();
                if (config.black_inventory.contains(cursor.getTypeId())) {
                    p.getInventory().remove(cursor);
                    e.setCancelled(true);
                }

                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null) {
                        if (config.black_inventory.contains(item.getTypeId())) {
                            p.getInventory().remove(item);
                        }
                    }
                }
            }
        }
    }
 
    /*
     * Player Kick Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerKick(PlayerKickEvent e) {
        cleanup(e.getPlayer());
    }
    
    /*
     * Player Quit Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerQuit(PlayerQuitEvent e) {
        cleanup(e.getPlayer());
    }
    
    /*
     * Player Join Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        final CreativeControl       plugin   = CreativeControl.getPlugin();
        
        if (CreativeControl.getMainConfig().data_teleport) {
            CreativeUtil.getFloor(p);
        }
        
        if (plugin.hasUpdate) {
            if (plugin.hasPerm(p, "Updater.Broadcast")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        CreativeCommunicator  com      = CreativeControl.getCommunicator();
                        CreativeMessages      messages = CreativeControl.getMessages();
                        com.msg(p, messages.updater_new, plugin.newversion, plugin.currentversion);
                        com.msg(p, messages.updater_visit);
                    }
                }, 40L);
            }
        }
    }

    /*
     * Player Changed World Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        /*
         * Gamemode Handler
         */
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        if (config.world_changegm) {
            CreativeControl       plugin   = CreativeControl.getPlugin();
            CreativeCommunicator  com      = CreativeControl.getCommunicator();
            CreativeMessages      messages = CreativeControl.getMessages();
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if ((!config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                    com.msg(p, messages.blocks_nocreative);
                    p.setGameMode(GameMode.SURVIVAL);
                }
            } else 
            if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                if ((config.world_creative) && (!plugin.hasPerm(p, "World.Keep"))) {
                    com.msg(p, messages.blocks_nosurvival);
                    p.setGameMode(GameMode.CREATIVE);
                }
            }
        }
    }

    /*
     * Player Pickup Item Module
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (e.isCancelled()) { return; }
        Player p = e.getPlayer();
        World world = p.getWorld();

        /*
        * Item Pickup prevent
        */
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (config.prevent_pickup) {
                CreativeControl       plugin   = CreativeControl.getPlugin();
                if (!plugin.hasPerm(p, "Preventions.Pickup")) {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    /*
     * Player Drops Item Module
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.isCancelled()) { return; }
        Player p = e.getPlayer();
        World world = p.getWorld();
        
        /*
        * Item drop prevent
        */
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (config.prevent_drop) {
                CreativeControl       plugin   = CreativeControl.getPlugin();
                if (!plugin.hasPerm(p, "Preventions.ItemDrop")) {
                    String itemName = e.getItemDrop().getType().getName().toLowerCase().replace("_", " ");
                    CreativeCommunicator  com      = CreativeControl.getCommunicator();
                    CreativeMessages      messages = CreativeControl.getMessages();
                    com.msg(p, messages.player_cantdrop, itemName);
                    e.getItemDrop().remove();
                }
            }
        }
    }
    
    /*
     * Chicken Egg Throw Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerEggThrowEvent(PlayerEggThrowEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();
        
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (config.prevent_eggs) {
                CreativeControl       plugin   = CreativeControl.getPlugin();
                if (!plugin.hasPerm(p, "Preventions.Eggs")) {
                    CreativeCommunicator  com      = CreativeControl.getCommunicator();
                    CreativeMessages      messages = CreativeControl.getMessages();
                    com.msg(p, messages.player_chicken);
                    e.setHatching(false);
                    e.setNumHatches((byte)0);
                }
            }
        }
    }
    
    /*
     * Player interact section
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block i = e.getClickedBlock();
        World world = p.getWorld();
        String ItemName = p.getItemInHand().getType().toString().toLowerCase().replace("_", " ");
        
        CreativeCommunicator  com      = CreativeControl.getCommunicator();
        CreativeMessages      messages = CreativeControl.getMessages();
        CreativeControl       plugin   = CreativeControl.getPlugin();
        CreativeWorldNodes    config   = CreativeWorldConfig.get(world);
        CreativeMainConfig    main     = CreativeControl.getMainConfig();

        if (i == null) { return; }
        
        if (config.prevent_economy) {
            if (i.getType() == Material.WALL_SIGN || i.getType() == Material.SIGN_POST) {
                com.msg(p, messages.player_cantdo);
                e.setCancelled(true);
                return;
            }
        }

        if (main.selection_tool == p.getItemInHand().getTypeId()) {
            if (plugin.hasPerm(p, "Utily.Selection")) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Location right = e.getClickedBlock().getLocation();
                    plugin.right.put(p, right);
                    com.msg(p, messages.sel_second, right.getBlockX(), right.getBlockY(), right.getBlockZ());
                    e.setCancelled(true);
                    return;
                } else
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Location left = e.getClickedBlock().getLocation();
                    plugin.left.put(p, left);
                    com.msg(p, messages.sel_first, left.getBlockX(), left.getBlockY(), left.getBlockZ());
                    e.setCancelled(true);
                    return;
                }
            }
        }
        
        if (plugin.mods.containsKey(p.getName())) {
            String data = plugin.mods.get(p.getName());
            if (data.equals("Block-Add-Tool")) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.info")) {
                        
                        e.setCancelled(true);
                        return;
                    }
                } else
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.add")) {
                        
                        e.setCancelled(true);
                        return;
                    }
                }
            } else 
            if (data.equals("Block-Del-Tool")) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.info")) {
                        
                        e.setCancelled(true);
                        return;
                    }
                } else
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.del")) {
                        
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (config.world_exclude) { return; }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if (config.black_interact.contains(i.getTypeId())) {
                    if (!plugin.hasPerm(p, "BlackList.ItemInteract")) {
                        com.msg(p, messages.player_cantuse, i.getType().toString().toLowerCase().replace("_", " "));
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        
        if (p.getItemInHand().getType() == Material.MINECART || p.getItemInHand().getType() == Material.BOAT) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                plugin.player = p;
            }
        }
        
        if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                if (e.getItem() != null) {
                    if (config.prevent_eggs) {
                        if ((p.getItemInHand().getType() == Material.MONSTER_EGG) || (p.getItemInHand().getType() == Material.MONSTER_EGGS)) {
                            if (!plugin.hasPerm(p, "Preventions.Eggs")) {
                                com.msg(p, messages.player_cantuse, ItemName);
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                    
                    if (config.black_use.contains(e.getItem().getTypeId())) {
                        if (!plugin.hasPerm(p, "BlackList.ItemUse")) {
                            com.msg(p, messages.player_cantuse, ItemName);
                            e.setCancelled(true);
                            return;
                        }
                    } 

                    if (config.prevent_potion) {
                        if (p.getItemInHand().getTypeId() == 373) {
                            if (!plugin.hasPerm(p, "Preventions.PotionSplash")) {
                                com.msg(p, messages.player_cantuse, ItemName);
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Block info Module
     */
    
    /*
    private void blockInfo(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        String player = p.getName();
        Block b = e.getClickedBlock();
        World world = b.getWorld();
        
        int x = b.getX(); int y = b.getY(); int z = b.getZ(); int type = b.getTypeId();
        
        CreativeMainConfig config = CreativeControl.getConf();
        CreativeSQLDatabase db = CreativeControl.getSQL();
        CreativeBlockCache cache = CreativeControl.getCache();
        
        CreativeBlockLocation block = new CreativeBlockLocation(player, b, null, null);
        CreativeBlockLocation blockdb = db.getBlock(block);
        if (blockdb != null) {
            msg(p, config.getMessage("ingame.blockInfo.owner"), Msg.MSG, blockdb.getOwner());
            msg(p, config.getMessage("ingame.blockInfo.data"), Msg.MSG, world.getName(), x, y, z, type);

            if (cache.isCached(block.toString())) {
                msg(p, config.getMessage("ingame.blockInfo.cachedTrue"), Msg.MSG);
            } else {
                msg(p, config.getMessage("ingame.blockInfo.cachedFalse"), Msg.MSG);
            }
            msg(p, config.getMessage("ingame.blockInfo.dbStatus"), Msg.MSG);
        } else {
            if (cache.isCached(block.toString())) {
                msg(p, config.getMessage("ingame.blockInfo.owner"), Msg.MSG, cache.getOwner(block.toString()));
                msg(p, config.getMessage("ingame.blockInfo.data"), Msg.MSG, world.getName(), x, y, z, type);
                msg(p, config.getMessage("ingame.blockInfo.cachedTrue"), Msg.MSG);
                msg(p, config.getMessage("ingame.blockInfo.memoryStatus"), Msg.MSG);
            } else {
                msg(p, config.getMessage("ingame.blockInfo.notIn"), Msg.MSG);
            }
        }
    }

    /*
     * Manualy Add block module
     */
    /*
    private boolean addBlock(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        World world = p.getWorld();
        
        CreativeMainConfig config = CreativeControl.getConf();
        CreativeBlockManager protection = CreativeControl.getProtection();
        
        if (!config.isExcluded(world, b.getTypeId())) {
            if ((config.getWorldBoolean(world, "BlockProtection.NoDrop")) || (config.getWorldBoolean(world, "BlockProtection.OwnBlocks"))) {
                if (protection.isProtected(b)) {
                    msg(p, config.getMessage("ingame.addBlock.already"), Msg.MSG);
                    return true;
                } else {
                    protection.addBlock(p.getName(), b);
                    msg(p, config.getMessage("ingame.addBlock.added"), Msg.MSG);
                    return true;
                }
            }
        } else {
            msg(p, config.getMessage("ingame.addBlock.cant"), Msg.MSG);
            return true;
        }
        return false;
    }

    /*
     * Manualy del Block module
     */
    /*
    private boolean delBlock(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        World world = p.getWorld();
        
        CreativeBlockManager protection = CreativeControl.getProtection();
        CreativeMainConfig config = CreativeControl.getConf();
        if (config.getWorldBoolean(world, "BlockProtection.OwnBlocks")) {
            if (protection.isProtected(b)) {
                if (protection.isOwner(p)) {
                    protection.delBlock(b);
                    msg(p, config.getMessage("ingame.delBlock.deleted"), Msg.MSG);
                    return true;
                } else {
                    msg(p, config.getMessage("ingame.blocks.pertence"), Msg.MSG, protection.getOwner());
                    return true;
                }
            } else {
                msg(p, config.getMessage("ingame.blockInfo.notIn"), Msg.MSG);
                return true;
            }
        }

        if (config.getWorldBoolean(world, "BlockProtection.NoDrop")) {
            if (plugin.hasPerm(p, "NoDrop.Command")) {
                protection.delBlock(b);
                return true;
            }
        }
        return false;
    }
    
    private void msg(Player p, String message, Msg type, Object...objects) {
        CreativeCommunicator com = CreativeControl.getCom();
        com.msg(p, message, type, objects);
    }

    private void clearHash(Player p) {
        plugin.right.remove(p);
        plugin.left.remove(p);
        plugin.debug.remove(p.getName());
        CreativeMainConfig config = CreativeControl.getConf();
        config.clearFriends(p);
    }
    */ 

    private void cleanup(Player p) {
        CreativeControl plugin = CreativeControl.getPlugin();
        plugin.right.remove(p);
        plugin.left.remove(p);
        CreativePlayerFriends friend = CreativeControl.getFriends();
        friend.uncache(p);
    }
}