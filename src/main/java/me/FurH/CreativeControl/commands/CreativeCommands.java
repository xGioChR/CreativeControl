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

package me.FurH.CreativeControl.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.database.extra.CreativeSQLBackup;
import me.FurH.CreativeControl.database.extra.CreativeSQLCleanup;
import me.FurH.CreativeControl.database.extra.CreativeSQLMigrator;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
import me.FurH.CreativeControl.region.CreativeRegion.gmType;
import me.FurH.CreativeControl.region.CreativeRegionManager;
import me.FurH.CreativeControl.selection.CreativeBlocksSelection;
import me.FurH.CreativeControl.selection.CreativeBlocksSelection.Type;
import me.FurH.CreativeControl.selection.CreativeSelection;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        /*CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (args.length <= 0) {
            msg(sender, "[TAG] &8CreativeControl &4{0} &8by &4FurmigaHumana", plugin.currentversion);
            msg(sender, messages.commands_type);
            return true;
        } else
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("tool")) {
                toolCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("status")) {
                statusCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("del")) {
                delCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("add")) {
                addCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("admin")) {
                onAdminCommand(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("check")) {
                checkCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("cleanup")) {
                cleanupCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("region")) {
                regionCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("sel")) {
                selCmd(sender, cmd, string, args);
            } else
            if ((args[0].equalsIgnoreCase("friend") || (args[0].equalsIgnoreCase("f")))) {
                friendCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("reload")) {
                reloadCmd(sender, cmd, string, args);
            } else {
                if (!plugin.hasPerm(sender, "Commands.Help")) {
                    msg(sender, messages.commands_noperm);
                    return true;
                } else {
                    if ((sender instanceof Player)) {
                        msg(sender, messages.commands_help1);
                        msg(sender, messages.commands_help2);
                        msg(sender, messages.commands_help3);
                        msg(sender, messages.commands_help4);
                        msg(sender, messages.commands_help5);
                        msg(sender, messages.commands_help8);
                        msg(sender, messages.commands_help9);
                        msg(sender, messages.commands_help10);
                        msg(sender, messages.commands_help6);
                        msg(sender, messages.commands_help11);
                        return true;
                    } else
                    if (!(sender instanceof Player)) {
                        msg(sender, messages.commands_help3);
                        msg(sender, messages.commands_help4);
                        msg(sender, messages.commands_help5);
                        msg(sender, messages.commands_help10);
                        msg(sender, messages.commands_help11);
                        msg(sender, messages.commands_help7);
                        return true;
                    }
                }
            }
        }*/
        return true;
    }

    /*
     * //cc admin[0] migrator[1] [>sqlite/>mysql/>lwc][2]
     */
    /*public boolean onAdminCommand(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Admin")) {
            msg(sender, messages.commands_noperm);
            return true;
        } else {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("migrator")) {
                    if (!plugin.hasPerm(sender, "Commands.Admin.Migrator")) {
                        msg(sender, messages.commands_noperm);
                        return true;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.migrator_more1);
                                msg(sender, messages.migrator_more2);
                                return true;
                            } else {
                                if (args[2].equalsIgnoreCase(">sqlite") || args[2].equalsIgnoreCase(">mysql")) {
                                    CreativeSQLMigrator migrator = null;
                                    String type = null;

                                    if (args[2].equalsIgnoreCase(">sqlite")) {
                                        type = "mysql>sqlite";
                                    } else
                                    if (args[2].equalsIgnoreCase(">mysql")) {
                                        type = "sqlite>mysql";
                                    }

                                    if (sender instanceof Player) {
                                        migrator = new CreativeSQLMigrator(plugin, (Player)sender, type);
                                    } else {
                                        migrator = new CreativeSQLMigrator(plugin, null, type);
                                    }

                                    if (migrator.lock) {
                                        msg(sender, messages.migrator_locked);
                                        return true;
                                    } else {
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, migrator); //TODO: ensure thread safety
                                        return true;
                                    }
                                } else {
                                    msg(sender, messages.migrator_more1);
                                    msg(sender, messages.migrator_more2);
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.migrator_more1);
                            msg(sender, messages.migrator_more2);
                            return true;
                        }
                    }
                } else {
                    msg(sender, messages.migrator_more1);
                    msg(sender, messages.migrator_more2);
                    return true;
                }
            } else {
                msg(sender, messages.migrator_mysqlsqlite);
                msg(sender, messages.migrator_sqlitemysql);
                return true;
            }
        }
    }

    public boolean friendCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativePlayerFriends    friends   = CreativeControl.getFriends();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        CreativeSQLDatabase      db        = CreativeControl.getDb();

        if (!plugin.hasPerm(sender, "Commands.Friend")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("list")) {
                    if (!plugin.hasPerm(sender, "Commands.Friend.list")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_flist_usage);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_flist_help);
                                    return true;
                                } else {
                                    if ((friends.getFriends(args[2]) == null) || (friends.getFriends(args[2]).size() < 0)) {
                                        msg(sender, messages.commands_flist_nofriends);
                                        return true;
                                    } else {
                                        String list = friends.getFriends(args[2]).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
                                        msg(sender, messages.commands_flist_friends, list);
                                        return true;
                                    }
                                }
                            }
                        } else {
                            if ((friends.getFriends(sender.getName()) == null) || (friends.getFriends(sender.getName()).size() < 0)) {
                                msg(sender, messages.commands_flist_unofriends);
                                return true;
                            } else {
                                String list = friends.getFriends(sender.getName()).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
                                msg(sender, messages.commands_flist_friends, list);
                                return true;
                            }
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("add")) {
                    if (!plugin.hasPerm(sender, "Commands.Friend.add")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_fadd_usage);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_fadd_help);
                                    return true;
                                } else {
                                    if (friends.getFriends(sender.getName()) == null) {
                                        HashSet<String> list = CreativeUtil.toStringHashSet(args[2], ", ");
                                        friends.saveFriends(sender.getName(), list);
                                        msg(sender, messages.commands_fadd_added, args[2]);
                                        list.clear(); list = null;
                                        return true;
                                    } else {
                                        HashSet<String> list = friends.getFriends(sender.getName());
                                        if (list.contains(args[2])) {
                                            msg(sender, messages.commands_fadd_already, args[2]);
                                        } else {
                                            list.add(args[2]);
                                            friends.saveFriends(sender.getName(), list);
                                            msg(sender, messages.commands_fadd_added, args[2]);
                                        }
                                        list.clear(); list = null;
                                        return true;
                                    }
                                }
                            }
                        } else {
                            msg(sender, messages.commands_fadd_usage);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("remove")) {
                    if (!plugin.hasPerm(sender, "Commands.Friend.remove")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_frem_usage);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_frem_help);
                                    return true;
                                } else {
                                    if (friends.getFriends(sender.getName()) == null) {
                                        msg(sender, messages.commands_frem_empty, args[2]);
                                        return true;
                                    } else {
                                        HashSet<String> list = friends.getFriends(sender.getName());
                                        if (list.contains(args[2])) {
                                            list.remove(args[2]);
                                            friends.saveFriends(sender.getName(), list);
                                            msg(sender, messages.commands_frem_removed, args[2]);
                                        } else {
                                            msg(sender, messages.commands_frem_notin, args[2]);
                                        }
                                        list.clear(); list = null;
                                    }
                                }
                            }
                        } else {
                            msg(sender, messages.commands_frem_usage);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("allow")) {
                    if (!(sender instanceof Player)) {
                        msg(sender, messages.commands_nothere);
                        return false;
                    } else
                    if ((sender instanceof Player)) {
                        if (!plugin.hasPerm(sender, "Commands.Friend.allow")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    msg(sender, messages.commands_fallow_usage);
                                    return true;
                                } else {
                                    if (args[2].equals("?")) {
                                        msg(sender, messages.commands_fallow_help);
                                        return true;
                                    } else {
                                        selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.ALLOW);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_fallow_usage);
                                return true;
                            }
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("transfer")) {
                    if (!(sender instanceof Player)) {
                        msg(sender, messages.commands_nothere);
                        return false;
                    } else
                    if ((sender instanceof Player)) {
                        if (!plugin.hasPerm(sender, "Commands.Friend.transfer")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    if (args.length > 4) {
                                        msg(sender, messages.commands_ftrans_usage);
                                        return true;
                                    } else {
                                        if (args[2].equals("all")) {
                                            if (!plugin.hasPerm(sender, "Commands.Friend.transfer.all")) {
                                                msg(sender, messages.commands_noperm);
                                                return false;
                                            } else {
                                                HashSet<String> locations = new HashSet<String>();
                                                HashSet<String> backup = new HashSet<String>();
                                                
                                                long startTimer = System.currentTimeMillis();
                                                long elapsedTime = 0;
                                                
                                                msg(sender, messages.updater_loading);
                                                
                                                PreparedStatement ps = null;
                                                ResultSet rs = null;
                                                try {
                                                    ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks` WHERE owner = '"+sender.getName().toLowerCase()+"'");
                                                    rs = ps.getResultSet();

                                                    while (rs.next()) {
                                                        locations.add(rs.getString("location"));
                                                        backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                                                                + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
                                                    }
                                                    
                                                } catch (SQLException ex) {
                                                    CreativeControl.plugin.getCommunicator().error(Thread.currentThread(), ex, "[TAG] Failed to get the block from the database, " + ex.getMessage());
                                                    if (!db.isOk()) { db.fix(); }
                                                } finally {
                                                    if (rs != null) {
                                                        try {
                                                            rs.close();
                                                        } catch (SQLException ex) { }
                                                    }
                                                    if (ps != null) {
                                                        try {
                                                            ps.close();
                                                        } catch (SQLException ex) { }
                                                    }
                                                }
                                                
                                                elapsedTime = (System.currentTimeMillis() - startTimer);
                                                msg(sender, messages.updater_loaded, locations.size(), elapsedTime);

                                                msg(sender, messages.backup_generating);

                                                CreativeSQLBackup.backup(backup);

                                                elapsedTime = (System.currentTimeMillis() - startTimer);
                                                msg(sender, messages.backup_done, elapsedTime);
                                                
                                                for (String location : locations) {
                                                    //cache.remove(location);
                                                }
                                                
                                                CreativeBlockManager manager = CreativeControl.getManager();
                                                int newOwner = manager.getPlayerId(args[3].toLowerCase());
                                                int oldOwner = manager.getPlayerId(sender.getName().toLowerCase());
                                                
                                                db.queue("UPDATE `"+db.prefix+"blocks` SET owner = '"+newOwner+"' WHERE owner = '"+oldOwner+"'");

                                                locations.clear(); locations = null;
                                                backup.clear(); backup = null;

                                                msg(sender, messages.commands_cleanup_processed);
                                                return true;
                                            }
                                        } else {
                                            msg(sender, messages.commands_ftrans_usage);
                                            return true;
                                        }
                                    }
                                } else {
                                    if (args[2].equals("?")) {
                                        msg(sender, messages.commands_ftrans_help);
                                        return true;
                                    } else
                                    if (args[2].equals("all")) {
                                        msg(sender, messages.commands_ftrans_usage);
                                        return true;
                                    } else {
                                        selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.TRANSFER);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_ftrans_usage);
                                return true;
                            }
                        }
                    }
                } else {
                    msg(sender, messages.commands_flist_usage);
                    msg(sender, messages.commands_fadd_usage);
                    msg(sender, messages.commands_frem_usage);
                    msg(sender, messages.commands_fallow_usage);
                    msg(sender, messages.commands_ftrans_usage);
                    return true;
                }
            } else {
                msg(sender, messages.commands_flist_usage);
                msg(sender, messages.commands_fadd_usage);
                msg(sender, messages.commands_frem_usage);
                msg(sender, messages.commands_fallow_usage);
                msg(sender, messages.commands_ftrans_usage);
                return true;
            }
        }
        return true;
    }
    
    public boolean checkCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Check")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("status")) {
                    if (!plugin.hasPerm(sender, "Commands.Check.status")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            msg(sender, messages.commands_scheck_help);
                            return true;
                        } else {
                            int creative = 0;
                            int survival = 0;

                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (players.getGameMode().equals(GameMode.CREATIVE)) {
                                    creative++;
                                }
                                if (players.getGameMode().equals(GameMode.SURVIVAL)) {
                                    survival++;
                                }
                            }
                            msg(sender, messages.commands_scheck_there, survival, creative);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("player")) {
                    if (!plugin.hasPerm(sender, "Commands.Check.player")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_pcheck_help);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_pcheck_help);
                                    return true;
                                } else {
                                    Player player = Bukkit.getPlayer(args[2]);

                                    if (player != null) {
                                        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                            msg(sender, messages.commands_pcheck_gm, player.getName(), "survival");
                                        } else
                                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                                            msg(sender, messages.commands_pcheck_gm, player.getName(), "creative");
                                        }
                                    } else {
                                        msg(sender, messages.commands_pcheck_noton, args[2]);
                                    }
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.commands_pcheck_help);
                            msg(sender, messages.commands_scheck_more);
                            return true;
                        }
                    }
                } else {
                    msg(sender, messages.commands_scheck_help);
                    msg(sender, messages.commands_pcheck_help);
                    msg(sender, messages.commands_scheck_more);
                    return true;
                }
            } else {
                msg(sender, messages.commands_scheck_help);
                msg(sender, messages.commands_pcheck_help);
                msg(sender, messages.commands_scheck_more);
                return true;
            }
        }
    }

    public boolean addCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        if (!(sender instanceof Player)) {
            msg(sender, messages.commands_nothere);
            return false;
        } else
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            if (!plugin.hasPerm(sender, "Commands.Add")) {
                msg(sender, messages.commands_noperm);
                return false;
            } else {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("player")) {
                        if (!plugin.hasPerm(sender, "Commands.Add.player")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    msg(sender, messages.commands_padd_help);
                                    return true;
                                } else {
                                    if (args[2].equals("?")) {
                                        msg(sender, messages.commands_padd_help);
                                        return true;
                                    } else {
                                        selection.allBlocks(sender, args[2], Type.ADD);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_padd_usage);
                                msg(sender, messages.commands_padd_more);
                                return true;
                            }
                        }
                    } else
                    if (args[1].equals("?")) {
                        msg(sender, messages.commands_sadd_hep);
                        return true;
                    } else {
                        msg(sender, messages.commands_sadd_usag);
                        msg(sender, messages.commands_padd_usage);
                        msg(sender, messages.commands_padd_more);
                        return true;
                    }
                } else {
                    selection.allBlocks(sender, sender.getName(), Type.ADD);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cleanupCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeSQLDatabase      db        = CreativeControl.getDb();
        
        if (!plugin.hasPerm(sender, "Commands.Cleanup")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            if(args.length > 1) {
                if (args[1].equalsIgnoreCase("all")) {
                    if (!plugin.hasPerm(sender, "Commands.Cleanup.all")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            msg(sender, messages.commands_acleanup_help);
                            return true;
                        } else {
                            for (World world : Bukkit.getWorlds()) {
                                String query = "DELETE FROM `"+db.prefix+"blocks_"+world.getName()+"`";
                                db.execute(query);
                            }

                            //cache.clear();
                            msg(sender, messages.commands_cleanup_processed);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("type")) {
                    if (!plugin.hasPerm(sender, "Commands.Cleanup.type")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_tcleanup_help);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_tcleanup_help);
                                    return true;
                                } else {
                                    HashSet<String> locations = new HashSet<String>();
                                    HashSet<String> backup = new HashSet<String>();

                                    long startTimer = System.currentTimeMillis();
                                    long elapsedTime = 0;

                                    msg(sender, messages.updater_loading);
                                    
                                    PreparedStatement ps = null;
                                    ResultSet rs = null;
                                    
                                    try {
                                        ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks` WHERE type = '"+args[2].toLowerCase()+"'");
                                        rs = ps.getResultSet();
                                        
                                        while (rs.next()) {
                                            locations.add(rs.getString("location"));
                                            backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                                                    + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
                                        }

                                    } catch (SQLException ex) {
  
                                    } finally {
                                        if (rs != null) {
                                            try {
                                                rs.close();
                                            } catch (SQLException ex) { }
                                        }
                                        if (ps != null) {
                                            try {
                                                ps.close();
                                            } catch (SQLException ex) { }
                                        }
                                    }
                                    
                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.updater_loaded, locations.size(), elapsedTime);

                                    msg(sender, messages.backup_generating);

                                    CreativeSQLBackup.backup(backup);

                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.backup_done, elapsedTime);

                                    for (String location : locations) {
                                        String query = "DELETE FROM `"+db.prefix+"blocks` WHERE location = '"+location+"'";
                                        //cache.remove(location);
                                        db.execute(query);
                                    }
                                    
                                    locations.clear(); locations = null;
                                    backup.clear(); backup = null;
                                    
                                    msg(sender, messages.commands_cleanup_processed);
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.commands_tcleanup_usage);
                            msg(sender, messages.commands_cleanup_more);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("player")) {
                    if (!plugin.hasPerm(sender, "Commands.Cleanup.player")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_pcleanup_help);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_pcleanup_help);
                                    return true;
                                } else {
                                    HashSet<String> locations = new HashSet<String>();
                                    HashSet<String> backup = new HashSet<String>();

                                    long startTimer = System.currentTimeMillis();
                                    long elapsedTime = 0;

                                    msg(sender, messages.updater_loading);
                                    
                                    PreparedStatement ps = null;
                                    ResultSet rs = null;
                                    
                                    try {
                                        ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks` WHERE owner = '"+args[2].toLowerCase()+"'");
                                        rs = ps.getResultSet();
                                        
                                        while (rs.next()) {
                                            locations.add(rs.getString("location"));
                                            backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                                                    + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
                                        }
                                        
                                    } catch (SQLException ex) {
                                    } finally {
                                        if (rs != null) {
                                            try {
                                                rs.close();
                                            } catch (SQLException ex) { }
                                        }
                                        if (ps != null) {
                                            try {
                                                ps.close();
                                            } catch (SQLException ex) { }
                                        }
                                    }
                                    
                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.updater_loaded, locations.size(), elapsedTime);

                                    msg(sender, messages.backup_generating);

                                    CreativeSQLBackup.backup(backup);

                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.backup_done, elapsedTime);

                                    for (String location : locations) {
                                        String query = "DELETE FROM `"+db.prefix+"blocks` WHERE location = '"+location+"'";
                                        //cache.remove(location);
                                        db.execute(query);
                                    }
                                    
                                    locations.clear(); locations = null;
                                    backup.clear(); backup = null;
                                    
                                    msg(sender, messages.commands_cleanup_processed);
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.commands_pcleanup_usage);
                            msg(sender, messages.commands_cleanup_more);
                            return true;
                        }
                    }
                } else
                if (args[1].equalsIgnoreCase("world")) {
                    if (!plugin.hasPerm(sender, "Commands.Cleanup.world")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                msg(sender, messages.commands_wcleanup_help);
                                return true;
                            } else {
                                if (args[2].equals("?")) {
                                    msg(sender, messages.commands_wcleanup_help);
                                    return true;
                                } else {
                                    HashSet<String> locations = new HashSet<String>();
                                    HashSet<String> backup = new HashSet<String>();

                                    long startTimer = System.currentTimeMillis();
                                    long elapsedTime = 0;

                                    msg(sender, messages.updater_loading);
                                    
                                    PreparedStatement ps = null;
                                    ResultSet rs = null;
                                    
                                    try {
                                        ps = db.getQuery("SELECT * FROM `"+db.prefix+"blocks`");
                                        rs = ps.getResultSet();
                                        
                                        while (rs.next()) {
                                            locations.add(rs.getString("location"));
                                            backup.add("INSERT INTO `"+db.prefix+"blocks` (id, owner, location, type, allowed, time) VALUES ('"+rs.getInt("id")+"',"
                                                    + " '"+rs.getString("owner")+"', '"+rs.getString("location")+"', '"+rs.getInt("type")+"', '"+rs.getString("allowed")+"', '"+rs.getString("time")+"')");
                                        }
                                    } catch (SQLException ex) {
                                    } finally {
                                        if (rs != null) {
                                            try {
                                                rs.close();
                                            } catch (SQLException ex) { }
                                        }
                                        if (ps != null) {
                                            try {
                                                ps.close();
                                            } catch (SQLException ex) { }
                                        }
                                    }
                                    
                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.updater_loaded, locations.size(), elapsedTime);

                                    msg(sender, messages.backup_generating);

                                    CreativeSQLBackup.backup(backup);

                                    elapsedTime = (System.currentTimeMillis() - startTimer);
                                    msg(sender, messages.backup_done, elapsedTime);

                                    for (String location : locations) {
                                        Location loc = CreativeUtil.getLocation(location);
                                        if (loc.getWorld().getName().equalsIgnoreCase(args[2])) {
                                            String query = "DELETE FROM `"+db.prefix+"blocks` WHERE location = '"+location+"'";
                                            //cache.remove(location);
                                            db.execute(query);
                                        }
                                    }
                                    
                                    locations.clear(); locations = null;
                                    backup.clear(); backup = null;
                                    
                                    msg(sender, messages.commands_cleanup_processed);
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.commands_wcleanup_usage);
                            msg(sender, messages.commands_cleanup_more);
                            return true;
                        }
                    }
                } if (args[1].equalsIgnoreCase("corrupt")) {
                    if (!plugin.hasPerm(sender, "Commands.Cleanup.corrupt")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            msg(sender, messages.commands_ccleanup_help);
                            return true;
                        } else {
                            CreativeSQLCleanup cleanup = new CreativeSQLCleanup(null);
                            if (sender instanceof Player) {
                                cleanup = new CreativeSQLCleanup((Player)sender);
                            }
                            
                            if (cleanup.lock) {
                                msg(sender, messages.cleanup_locked);
                                return true;
                            } else {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, cleanup);
                                return true;
                            }
                        }
                    }
                } else {
                    msg(sender, messages.commands_acleanup_usage);
                    msg(sender, messages.commands_pcleanup_usage);
                    msg(sender, messages.commands_tcleanup_usage);
                    msg(sender, messages.commands_wcleanup_usage);
                    msg(sender, messages.commands_ccleanup_usage);
                    msg(sender, messages.commands_cleanup_more);
                    return true;
                }
            } else {
                msg(sender, messages.commands_acleanup_usage);
                msg(sender, messages.commands_pcleanup_usage);
                msg(sender, messages.commands_tcleanup_usage);
                msg(sender, messages.commands_wcleanup_usage);
                msg(sender, messages.commands_ccleanup_usage);
                msg(sender, messages.commands_cleanup_more);
                return true;
            }
        }
    }

    public boolean delCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        if (!(sender instanceof Player)) {
            msg(sender, messages.commands_nothere);
            return false;
        } else
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            if (!plugin.hasPerm(sender, "Commands.Del")) {
                msg(sender, messages.commands_noperm);
                return false;
            } else {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("all")) {
                        if (!plugin.hasPerm(sender, "Commands.Del.all")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                msg(sender, messages.commands_adel_help);
                                return true;
                            } else {
                                selection.allBlocks(sender, sender.getName(), Type.DELALL);
                                return true;
                            }
                        }
                    } else
                    if (args[1].equalsIgnoreCase("type")) {
                        if (!plugin.hasPerm(sender, "Commands.Del.type")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    msg(sender, messages.commands_tdel_help);
                                    return true;
                                } else {
                                    if (args[2].equals("?")) {
                                        msg(sender, messages.commands_tdel_help);
                                        return true;
                                    } else {
                                        selection.allBlocks(sender, args[2], Type.DELTYPE);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_tdel_usage);
                                msg(sender, messages.commands_del_more);
                                return true;
                            }
                        }
                    } else
                    if (args[1].equalsIgnoreCase("player")) {
                        if (!plugin.hasPerm(sender, "Commands.Del.player")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    msg(sender, messages.commands_pdel_help);
                                    return true;
                                } else {
                                    if (args[2].equals("?")) {
                                        msg(sender, messages.commands_pdel_help);
                                        return true;
                                    } else {
                                        selection.allBlocks(sender, args[2], Type.DELPLAYER);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_pdel_usage);
                                msg(sender, messages.commands_del_more);
                                return true;
                            }
                        }
                    } else {
                        msg(sender, messages.commands_adel_usage);
                        msg(sender, messages.commands_tdel_usage);
                        msg(sender, messages.commands_pdel_usage);
                        msg(sender, messages.commands_del_more);
                        return true;
                    }
                } else {
                    msg(sender, messages.commands_adel_usage);
                    msg(sender, messages.commands_tdel_usage);
                    msg(sender, messages.commands_pdel_usage);
                    msg(sender, messages.commands_del_more);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean selCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (!(sender instanceof Player)) {
            msg(sender, messages.commands_nothere);
            return false;
        } else
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("expand")) {
                    if (!plugin.hasPerm(sender, "Commands.Expand")) {
                        msg(sender, messages.commands_noperm);
                        return false;
                    } else {
                        if (args.length > 2) {
                            if (args.length > 3) {
                                if (args.length > 4) {
                                    msg(sender, messages.commands_sel_more);
                                    return true;
                                } else {
                                    if (args[2].equalsIgnoreCase("up")) {
                                        if (args[3].equalsIgnoreCase("?")) {
                                            msg(sender, messages.commands_usel_help);
                                            msg(sender, messages.commands_sel_more);
                                            return true;
                                        } else {
                                            try {
                                                int upAdd = Integer.parseInt(args[3]);
                                                Location up = plugin.right.get(p);

                                                World world = up.getWorld();
                                                double x = up.getX();
                                                double y = up.getY();
                                                double z = up.getZ();

                                                Location newUp = new Location(world, x, y + upAdd, z);
                                                plugin.right.remove(p);
                                                plugin.right.put(p, newUp);

                                                msg(sender, messages.commands_usel_expended, upAdd);
                                                return true;
                                            } catch (Exception e) {
                                                msg(sender, messages.commands_sel_number);
                                                return true;
                                            }
                                        }
                                    } else
                                    if (args[2].equalsIgnoreCase("down")) {
                                        if (args[3].equalsIgnoreCase("?")) {
                                            msg(sender, messages.commands_dsel_help);
                                            msg(sender, messages.commands_sel_more);
                                            return true;
                                        } else {
                                            try {
                                                int downAdd = Integer.parseInt(args[3]);
                                                Location down = plugin.left.get(p);

                                                World world = down.getWorld();
                                                double x = down.getX();
                                                double y = down.getY();
                                                double z = down.getZ();

                                                Location newDown = new Location(world, x, y - downAdd, z);
                                                plugin.left.remove(p);
                                                plugin.left.put(p, newDown);

                                                msg(sender, messages.commands_dsel_expended, downAdd);
                                                return true;
                                            } catch (Exception e) {
                                                msg(sender, messages.commands_sel_number);
                                                return true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (args[2].equalsIgnoreCase("vert")) {
                                    if (args.length > 3) {
                                        msg(sender, messages.commands_vsel_help);
                                        msg(sender, messages.commands_sel_more);
                                        return true;
                                    } else {
                                        Location vright = plugin.right.get(p);
                                        Location vleft = plugin.right.get(p);

                                        World world = vright.getWorld();
                                        double x = vright.getX();
                                        double z = vright.getZ();

                                        World world2 = vleft.getWorld();
                                        double x2 = vleft.getX();
                                        double z2 = vleft.getZ();

                                        Location newVRight = new Location(world, x, 256, z);
                                        Location newVLeft = new Location(world2, x2, 1, z2);

                                        plugin.right.remove(p);
                                        plugin.right.put(p, newVRight);

                                        plugin.left.remove(p);
                                        plugin.left.put(p, newVLeft);

                                        msg(sender, messages.commands_vsel_expended);
                                        return true;
                                    }
                                } else {
                                    msg(sender, messages.commands_usel_usage);
                                    msg(sender, messages.commands_dsel_usage);
                                    msg(sender, messages.commands_vsel_usage);
                                    msg(sender, messages.commands_sel_more);
                                    return true;
                                }
                            }
                        } else {
                            msg(sender, messages.commands_usel_usage);
                            msg(sender, messages.commands_dsel_usage);
                            msg(sender, messages.commands_vsel_usage);
                            msg(sender, messages.commands_sel_more);
                            return true;
                        }
                    }
                } else {
                    msg(sender, messages.commands_usel_usage);
                    msg(sender, messages.commands_dsel_usage);
                    msg(sender, messages.commands_vsel_usage);
                    msg(sender, messages.commands_sel_more);
                    return true;
                }
            } else {
                msg(sender, messages.commands_usel_usage);
                msg(sender, messages.commands_dsel_usage);
                msg(sender, messages.commands_vsel_usage);
                msg(sender, messages.commands_sel_more);
                return true;
            }
        }
        return true;
    }
    
    public boolean regionCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        CreativeMainConfig       main      = CreativeControl.getMainConfig();
        if (!(sender instanceof Player)) {
            msg(sender, messages.commands_nothere);
            return false;
        } else
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            Location left = plugin.left.get((Player)sender);
            Location right = plugin.right.get((Player)sender);
            if (!plugin.hasPerm(p, "Commands.Region")) {
                msg(sender, messages.commands_noperm);
                return false;
            } else {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("define")) {
                        if (!plugin.hasPerm(sender, "Commands.Region.define")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    if (args.length > 4) {
                                        msg(sender, messages.commands_region_define);
                                        msg(sender, messages.commands_region_more);
                                        return true;
                                    } else {
                                        if (args[2].equalsIgnoreCase("creative")) {
                                            if (args[3].equalsIgnoreCase("?")) {
                                                msg(sender, messages.commands_crdefine_help);
                                                return true;
                                            } else {
                                                Location start = null;
                                                Location end = null;
                                                
                                                if (!main.selection_usewe || selection.getSelection((Player)sender) == null) {
                                                    if ((left == null) || (right == null)) {
                                                        msg(sender, messages.sel_null);
                                                        return true;
                                                    }
                                                    
                                                    CreativeSelection sel = new CreativeSelection(left, right);

                                                    start = sel.getStart();
                                                    end = sel.getEnd();
                                                } else {
                                                    Selection sel = selection.getSelection((Player)sender);

                                                    if (sel == null) {
                                                        msg(sender, messages.sel_null);
                                                        return true;
                                                    }

                                                    start = sel.getMinimumPoint();
                                                    end = sel.getMaximumPoint();
                                                }

                                                setRegion(gmType.CREATIVE, args[3], start, end);
                                                
                                                start = null;
                                                end = null;
                                                
                                                msg(sender, messages.commands_region_created, args[3]);
                                                return true;
                                            }
                                        } else
                                        if (args[2].equalsIgnoreCase("survival")) {
                                            if (args[3].equalsIgnoreCase("?")) {
                                                msg(sender, messages.commands_crdefine_help);
                                                msg(sender, messages.commands_srdefine_help);
                                                return true;
                                            } else {
                                                Location start = null;
                                                Location end = null;
                                                
                                                if (!main.selection_usewe || selection.getSelection((Player)sender) == null) {
                                                    if ((left == null) || (right == null)) {
                                                        msg(sender, messages.sel_null);
                                                        return true;
                                                    }
                                                    
                                                    CreativeSelection sel = new CreativeSelection(left, right);

                                                    start = sel.getStart();
                                                    end = sel.getEnd();
                                                } else {
                                                    Selection sel = selection.getSelection((Player)sender);

                                                    if (sel == null) {
                                                        msg(sender, messages.sel_null);
                                                        return true;
                                                    }

                                                    start = sel.getMinimumPoint();
                                                    end = sel.getMaximumPoint();
                                                }

                                                setRegion(gmType.SURVIVAL, args[3], start, end);
                                                
                                                start = null;
                                                end = null;
                                                
                                                msg(sender, messages.commands_region_created, args[3]);
                                                return true;
                                            }
                                        } else {
                                            msg(sender, messages.commands_crdefine_usage);
                                            msg(sender, messages.commands_srdefine_usage);
                                            msg(sender, messages.commands_region_more);
                                            return true;
                                        }
                                    }
                                } else {
                                    msg(sender, messages.commands_crdefine_usage);
                                    msg(sender, messages.commands_srdefine_usage);
                                    msg(sender, messages.commands_region_more);
                                    return true;
                                }
                            } else {
                                msg(sender, messages.commands_crdefine_usage);
                                msg(sender, messages.commands_srdefine_usage);
                                msg(sender, messages.commands_region_more);
                                return true;
                            }
                        }
                    } else
                    if (args[1].equalsIgnoreCase("remove")) {
                        if (!plugin.hasPerm(sender, "Commands.Region.remove")) {
                            msg(sender, messages.commands_noperm);
                            return false;
                        } else {
                            if (args.length > 2) {
                                if (args.length > 3) {
                                    msg(sender, messages.commands_cremove_usage);
                                    return true;
                                } else {
                                    if (args[2].equalsIgnoreCase("?")) {
                                        msg(sender, messages.commands_cremove_help);
                                        return true;
                                    } else {
                                        removeRegion(args[2]);
                                        msg(sender, messages.commands_cremove_sucess);
                                        return true;
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_cremove_usage);
                                msg(sender, messages.commands_region_more);
                                return true;
                            }
                        }
                    }
                } else {
                    msg(sender, messages.commands_region_more);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reloadCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Reload")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            msg(sender, messages.commands_reloading);
            plugin.reload(sender);
            msg(sender, messages.commands_reloaded);
        }
        if(args.length > 1) {
            msg(sender, messages.commands_reload_help);
            return true;
        }
        return false;
    }
    
    public boolean statusCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeSQLDatabase      db        = CreativeControl.getDb();
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        //CreativeBlockCache       cache     = CreativeControl.getCache();
        
        if (!plugin.hasPerm(sender, "Commands.Status")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("report")) {
                    msg(sender, "&7Report saved at: &a/plugins/CreativeControl/report/"+CreativePerformance.report()+".txt");
                    return true;
                } else {
                    msg(sender, messages.commands_status_help);
                    return true;
                }
            } else {
                msg(sender, messages.commands_status_queue, db.getQueue());
                msg(sender, messages.commands_status_sqlreads, db.getReads());
                msg(sender, messages.commands_status_sqlwrites, db.getWrites());
                //msg(sender, messages.commands_status_cache, (cache.getSize()), cache.getMaxSize());
                //msg(sender, messages.commands_status_cachereads, (cache.getReads()));
                //msg(sender, messages.commands_status_cachewrites, (cache.getWrites()));
                return true;
            }
        }
    }

    public boolean toolCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (!(sender instanceof Player)) {
            msg(sender, messages.commands_nothere);
            return false;
        } else
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            if (!plugin.hasPerm(p, "Commands.Tool")) {
                msg(sender, messages.commands_noperm);
                return false;
            } else {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("tool")) {
                        if (args.length > 1) {
                            if (args[1].equalsIgnoreCase("add")) {
                                if (args.length > 2) {
                                    msg(sender, messages.commands_atool_help);
                                    return true;
                                } else {
                                    if (!plugin.hasPerm(p, "Commands.Tool.add")) {
                                        msg(sender, messages.commands_noperm);
                                        return true;
                                    } else {
                                        if (plugin.mods.containsKey(p.getName())) {
                                            msg(sender, messages.commands_tool_dec);
                                            plugin.mods.remove(p.getName());
                                            plugin.modsfastup.remove(p.getName());
                                            return true;
                                        } else {
                                            plugin.mods.put(p.getName(), "Block-Add-Tool");
                                            plugin.modsfastup.add(p.getName());
                                            msg(sender, messages.commands_tool_act);
                                            return true;
                                        }
                                    }
                                }
                            } else 
                            if (args[1].equalsIgnoreCase("del")) {
                                if (args.length > 2) {
                                    msg(sender, messages.commands_dtool_help);
                                    return true;
                                } else {
                                    if (!plugin.hasPerm(p, "Commands.Tool.del")) {
                                        msg(sender, messages.commands_noperm);
                                        return true;
                                    } else {
                                        if (plugin.mods.containsKey(p.getName())) {
                                            msg(sender, messages.commands_tool_dec);
                                            plugin.mods.remove(p.getName());
                                            plugin.modsfastup.remove(p.getName());
                                            return true;
                                        } else {
                                            plugin.mods.put(p.getName(), "Block-Del-Tool");
                                            plugin.modsfastup.add(p.getName());
                                            msg(sender, messages.commands_tool_act);
                                            return true;
                                        }
                                    }
                                }
                            } else {
                                msg(sender, messages.commands_atool_usage);
                                msg(sender, messages.commands_dtool_usage);
                                msg(sender, messages.commands_tool_more);
                                return true;
                            }
                        } else {
                            msg(sender, messages.commands_atool_usage);
                            msg(sender, messages.commands_dtool_usage);
                            msg(sender, messages.commands_tool_more);
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public void setRegion(gmType type, String name, Location start, Location end) {
        CreativeRegionManager    region    = CreativeControl.getRegioner();
        region.addRegion(name, start, end, type.toString());
        region.saveRegion(name, type, start, end);
    }

    private void removeRegion(String string) {
        CreativeRegionManager    region    = CreativeControl.getRegioner();
        region.deleteRegion(string);
    }

    public void msg(CommandSender sender, String s, Object... objects) {
        CreativeControl.plugin.getCommunicator().msg(sender, s, objects);
    }*/
}