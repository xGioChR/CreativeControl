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
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.data.CreativePlayerData;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeBlockManager;
import me.FurH.CreativeControl.integration.worldedit.CreativeWorldEditHook;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
     * Inventory Close Event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) { return; }
        
        Player p = (Player)e.getPlayer();
        World world = p.getWorld();
        
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            CreativeControl       plugin   = CreativeControl.getPlugin();
            if (!plugin.hasPerm(p, "BlackList.Inventory")) {
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

            if (e.getInventory().getType() == InventoryType.PLAYER) {
                if (!plugin.hasPerm(p, "Preventions.StackLimit")) {
                    int stacklimit = config.prevent_stacklimit;
                    ItemStack current = e.getCurrentItem();
                    if (current != null) {
                        if (current.getAmount() > stacklimit) {
                            current.setAmount(stacklimit);
                        }
                    }

                    ItemStack cursor = e.getCursor();
                    if (cursor != null) {
                        if (cursor.getAmount() > stacklimit) {
                            cursor.setAmount(stacklimit);
                        }
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
                    if (current != null) {
                        if (config.black_inventory.contains(current.getTypeId())) {
                            p.getInventory().remove(current);
                            e.setCancelled(true);
                        }
                    }

                    ItemStack cursor = e.getCursor();
                    if (cursor != null) {
                        if (config.black_inventory.contains(cursor.getTypeId())) {
                            p.getInventory().remove(cursor);
                            e.setCancelled(true);
                        }
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

        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (i != null) {
                if (i.getType() == Material.WALL_SIGN || i.getType() == Material.SIGN_POST) {
                    Sign sign = (Sign)i.getState();
                    if (CreativeUtil.isEconomySign(sign)) {
                        if (!plugin.hasPerm(p, "BlackList.EconomySigns")) {
                            com.msg(p, messages.player_cantdo);
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
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
        
        if (plugin.modsfastup.contains(p.getName())) {
            String data = plugin.mods.get(p.getName());
            if (data.equals("Block-Add-Tool")) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.info")) {
                        info(p, i);
                        e.setCancelled(true);
                        return;
                    }
                } else
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.add")) {
                        add(p, i);
                        e.setCancelled(true);
                        return;
                    }
                }
            } else 
            if (data.equals("Block-Del-Tool")) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.info")) {
                        info(p, i);
                        e.setCancelled(true);
                        return;
                    }
                } else
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (plugin.hasPerm(p, "Utily.Tool.del")) {
                        del(p, i);
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
                    if (!plugin.hasPerm(p, "BlackList.ItemInteract."+i.getTypeId())) {
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
                        if (!plugin.hasPerm(p, "BlackList.ItemUse."+e.getItem().getTypeId())) {
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
     * Print informations about the block
     */
    public void info(Player p, Block b) {
        if (!is(p, b)) { return; }

        CreativeBlockManager manager = CreativeControl.getManager();
        CreativeBlockCache cache = CreativeControl.getCache();

        String[] data1 = manager.getFullData(CreativeUtil.getLocation(b.getLocation()));        
        String[] data2 = cache.get(CreativeUtil.getLocation(b.getLocation()));

        boolean insql = data1 != null;
        boolean incache = data2 != null;
        
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();
        
        if (!insql && !incache) {
            com.msg(p, messages.blockinfo_notprotected);
            plugin.mods.remove(p.getName());
            plugin.modsfastup.remove(p.getName());
            return;
        }

        String owner = null;
        String allowed = null;
        String type = null;
        String date = null;
        
        if (insql) {
            owner = data1[0];
            allowed = data1[1];
            type = data1[2];
            date = data1[3];
        } else
        if (incache) {
            owner = data2[0];
            allowed = data2[1];
            type = Integer.toString(b.getTypeId());
            date = Long.toString(System.currentTimeMillis());
        }

        Location loc = b.getLocation();
        com.msg(p, messages.blockinfo_owner, owner);
        com.msg(p, messages.blockinfo_location, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getBlock().getTypeId(), type);
        if (!"".equals(allowed) && allowed != null && !"null".equals(allowed) && !allowed.isEmpty() && !"[]".equals(allowed)) {
            com.msg(p, messages.blockinfo_allowed, allowed.replaceAll(" ,", " &a,&7").replaceAll("\\[", "").replaceAll("\\]", ""));
        }
        com.msg(p, messages.blockinfo_status, (incache ? messages.blockinfo_incache : ""), (insql ? messages.blockinfo_insql : messages.blockinfo_queue));
        com.msg(p, messages.blockinfo_date, CreativeUtil.getDate(Long.parseLong(date)));
        plugin.mods.remove(p.getName());
        plugin.modsfastup.remove(p.getName());
    }
    
    /*
     * Add a block to the database
     */
    public void add(Player p, Block b) {
        if (!is(p, b)) { return; }
        
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeBlockManager manager = CreativeControl.getManager();
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();
        CreativeWorldNodes config = CreativeWorldConfig.get(b.getWorld());

        if (b.getTypeId() == 64 || b.getTypeId() == 71) {
            String[] data = manager.getDoor2(b);
            if (data != null) {
                com.msg(p, messages.blockadd_already);
            } else {
                com.msg(p, messages.blockadd_protected);
                manager.addBlock(p, b, config.block_nodrop);
            }
        } else {
            String[] data = manager.getBlock(b);
            if (data != null) {
                com.msg(p, messages.blockadd_already);
            } else {
                com.msg(p, messages.blockadd_protected);
                manager.addBlock(p, b, config.block_nodrop);
            }
        }

        plugin.mods.remove(p.getName());
        plugin.modsfastup.remove(p.getName());
    }
    
    /*
     * Remove a protection from the block
     */
    public void del(Player p, Block b) {
        if (!is(p, b)) { return; }
        
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeBlockManager manager = CreativeControl.getManager();
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();

        if (b.getTypeId() == 64 || b.getTypeId() == 71) {
            String[] data = manager.getDoor2(b);
            if (data != null) {
                if (!manager.isOwner(p, data[0])) {
                    com.msg(p, messages.blocks_pertence, data[0]);
                } else {
                    com.msg(p, messages.blockdel_disprotected);
                    manager.delBlock(b);
                }
            } else {
                com.msg(p, messages.blockinfo_notprotected);
            }
        } else {
            String[] data = manager.getBlock(b);
            if (data != null) {
                if (!manager.isOwner(p, data[0])) {
                    com.msg(p, messages.blocks_pertence, data[0]);
                } else {
                    com.msg(p, messages.blockdel_disprotected);
                    manager.delBlock(b);
                }
            } else {
                com.msg(p, messages.blockinfo_notprotected);
            }
        }

        plugin.mods.remove(p.getName());
        plugin.modsfastup.remove(p.getName());
    }
    
    private boolean is(Player p, Block b) {
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeBlockManager manager = CreativeControl.getManager();
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();
        CreativeWorldNodes config = CreativeWorldConfig.get(b.getWorld());
        
        if (config.world_exclude) {
            com.msg(p, messages.blockinfo_world);
            plugin.mods.remove(p.getName());
            plugin.modsfastup.remove(p.getName());
            return false;
        }
        
        if (!manager.isProtectable(b.getWorld(), b.getTypeId())) {
            com.msg(p, messages.blockinfo_protectable);
            plugin.mods.remove(p.getName());
            plugin.modsfastup.remove(p.getName());
            return false;
        }
        
        return true;
    }

    private void cleanup(Player p) {
        CreativeControl plugin = CreativeControl.getPlugin();
        plugin.right.remove(p);
        plugin.left.remove(p);
        CreativePlayerFriends friend = CreativeControl.getFriends();
        friend.uncache(p);
    }
}