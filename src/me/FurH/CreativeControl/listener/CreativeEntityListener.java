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

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.configuration.CreativeWorldConfig;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeEntityListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onVehicleCreate(VehicleCreateEvent e) {
        CreativeControl plugin = CreativeControl.getPlugin();
        
        Vehicle vehicle = e.getVehicle();
        Player p = plugin.player;
        
        if (p == null) { return; }
        
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeMessages     messages   = CreativeControl.getMessages();
        CreativeWorldNodes config = CreativeWorldConfig.get(e.getVehicle().getWorld());
        if (config.world_exclude) { return; }
        
        if (config.prevent_vehicle) {
            if (!plugin.hasPerm(p, "Preventions.Vehicle")) {
                int limit = config.prevent_limitvechile;
                int total = 0;
                
                if (plugin.limits.get(p.getName()) != null) {
                    total = plugin.limits.get(p.getName());
                }
                
                if (limit > 0 && total >= limit) {
                    com.msg(p, messages.entity_vehicle);
                    vehicle.remove();
                } else {
                    plugin.entity.add(vehicle.getUniqueId());
                    plugin.limits.remove(p.getName());
                    plugin.limits.put(p.getName(), total++);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        Entity entity = e.getAttacker();
        Vehicle vehicle = e.getVehicle();
        
        if (entity instanceof Player) { return; }

        CreativeWorldNodes config = CreativeWorldConfig.get(e.getVehicle().getWorld());
        CreativeControl plugin = CreativeControl.getPlugin();
        
        Player p = (Player)entity;
        if (config.world_exclude) { return; }
        
        if (config.prevent_vehicle) {
            if (!plugin.hasPerm(p, "Preventions.Vehicle")) {
                if (plugin.entity.contains(vehicle.getUniqueId())) {
                    plugin.entity.remove(vehicle.getUniqueId());
                    vehicle.remove();
                }
            }
        }
    }
    
    /*
     * Anti Block explosion Module
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) { return; }

        CreativeWorldNodes config = CreativeWorldConfig.get(e.getLocation().getWorld());

        if (config.world_exclude) { return; }
        
        if (config.block_explosion) {
            CreativeBlockManager manager    = CreativeControl.getManager();
            if (e.blockList() != null && e.blockList().size() > 0) {
                for (Block b : e.blockList()) {
                    if (b != null && b.getType() != Material.AIR) {
                        if (config.block_ownblock) {
                            if (manager.isProtected(b, false)) {
                                e.blockList().remove(b);
                            }
                        } else
                        if (config.block_nodrop) {
                            if (manager.isProtected(b, true)) {
                                manager.delBlock(b, true);
                                b.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Anti Target Module
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.isCancelled())  { return; }
        if (!(e.getTarget() instanceof Player)) { return; }

        Player p = (Player)e.getTarget();
        World world = p.getWorld();
        
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (config.prevent_target) {
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                CreativeControl      plugin     = CreativeControl.getPlugin();
                if (!plugin.hasPerm(p, "Preventions.Target")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /*
     * Anti MineCart Storage Module
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();
        Entity entity = e.getRightClicked();
        
        CreativeCommunicator com        = CreativeControl.getCommunicator();
        CreativeMessages     messages   = CreativeControl.getMessages();
        CreativeControl      plugin     = CreativeControl.getPlugin();
        CreativeWorldNodes config = CreativeWorldConfig.get(world);
        
        if (config.world_exclude) { return; }
        
        if (p.getGameMode().equals(GameMode.CREATIVE)) {
            if (((entity instanceof StorageMinecart)) || ((entity instanceof PoweredMinecart))) {
                if (config.prevent_mcstore && !plugin.hasPerm(p, "Preventions.MineCartStorage")) {
                    com.msg(p, messages.player_cantuse2);
                    e.setCancelled(true);
                }
            } else
            if (entity instanceof Villager) {
                if (config.prevent_villager && !plugin.hasPerm(p, "Preventions.InteractVillagers")) {
                    com.msg(p, messages.player_cantdo);
                    e.setCancelled(true);
                }
            } else
            if (entity instanceof ItemFrame) {
                if (config.prevent_frame && !plugin.hasPerm(p, "Preventions.ItemFrame")) {
                    com.msg(p, messages.player_cantdo);
                    e.setCancelled(true);
                }
            }
        }
    }
    
    /*
     * Anti Mob Drop
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDeath(EntityDeathEvent e) {        
        if ((e.getEntity().getKiller() instanceof Player)) {
            Player p = e.getEntity().getKiller();
            World world = p.getWorld();
            
            if (p.getGameMode().equals(GameMode.CREATIVE)) {
                CreativeControl      plugin     = CreativeControl.getPlugin();
                CreativeWorldNodes config = CreativeWorldConfig.get(world);
                
                if (config.world_exclude) { return; }
                
                if (config.prevent_mobsdrop && !plugin.hasPerm(p, "Preventions.MobsDrop")) {
                    if ((e.getEntity() instanceof Creature)) {
                        e.setDroppedExp(0);
                        e.getDrops().clear();
                    }
                }
            }
        }
    }

    /*
     * Anti PvP/Mob Damage Module
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
        if ((event instanceof EntityDamageByEntityEvent)) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
            
            CreativeWorldNodes config = CreativeWorldConfig.get(e.getDamager().getWorld());
            CreativeCommunicator com        = CreativeControl.getCommunicator();
            CreativeMessages     messages   = CreativeControl.getMessages();
            CreativeControl      plugin     = CreativeControl.getPlugin();
            
            if (config.world_exclude) { return; }
            
            if (((e.getDamager() instanceof Player)) && ((e.getEntity() instanceof Player))) { //Player versus Player
                Player attacker = (Player)e.getDamager();
                if (config.prevent_pvp) {
                    if (attacker.getGameMode().equals(GameMode.CREATIVE)) {
                        if (!plugin.hasPerm(attacker, "Preventions.PvP")) {
                            com.msg(attacker, messages.entity_pvp);
                            e.setCancelled(true);
                        }
                    }
                }
            } else 
            if (((e.getDamager() instanceof Player)) && ((e.getEntity() instanceof Creature))) { //Player versus Creature
                Player attacker = (Player)e.getDamager();
                if (config.prevent_mobs) {
                    if (attacker.getGameMode().equals(GameMode.CREATIVE)) {
                        if (!plugin.hasPerm(attacker, "Preventions.Mobs")) {
                            com.msg(attacker, messages.entity_mobs, e.getEntityType().toString().toLowerCase().replace("_", " "));
                            e.setCancelled(true);
                        }
                    }
                }
            } else 
            if ((e.getDamager() instanceof Projectile)) { //Damage with projectiles [e.g Arrow, Eggs]
                Projectile projectile = (Projectile)e.getDamager();
                if (((projectile.getShooter() instanceof Player)) && ((e.getEntity() instanceof Player))) { //Player versus Player with projectiles
                    Player attacker = (Player)projectile.getShooter();
                    if (config.prevent_pvp) {
                        if (attacker.getGameMode().equals(GameMode.CREATIVE)) {
                            if (!plugin.hasPerm(attacker, "Preventions.PvP")) {
                                com.msg(attacker, messages.entity_pvp);
                                e.setCancelled(true);
                            }
                        }
                    }
                } else 
                if (((projectile.getShooter() instanceof Player)) && ((e.getEntity() instanceof Creature))) { //Player versus Creature with projectiles
                    Player attacker = (Player)projectile.getShooter();
                    if (config.prevent_mobs) {
                        if (attacker.getGameMode().equals(GameMode.CREATIVE)) {
                            if (!plugin.hasPerm(attacker, "Preventions.Mobs")) {
                                com.msg(attacker, messages.entity_mobs, e.getEntityType().toString().toLowerCase().replace("_", " "));
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}