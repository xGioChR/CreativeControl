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

import java.util.Arrays;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeBlockCache;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.region.CreativeRegion;
import me.FurH.CreativeControl.region.CreativeRegionCreator;
import me.FurH.CreativeControl.selection.CreativeBlocksSelection;
import me.FurH.CreativeControl.selection.CreativeBlocksSelection.Type;
import me.FurH.CreativeControl.selection.CreativeSelection;
import me.FurH.CreativeControl.util.CreativeCommunicator;
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
        CreativeMessages         messages  = CreativeControl.getMessages();
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
        }
        return true;
    }
    
    public boolean friendCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativePlayerFriends    friends   = CreativeControl.getFriends();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
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
                                        List list = Arrays.asList(args[2]);
                                        friends.saveFriends(sender.getName(), list);
                                        msg(sender, messages.commands_fadd_added, args[2]);
                                        return true;
                                    } else {
                                        List list = friends.getFriends(sender.getName());
                                        if (list.contains(args[2])) {
                                            msg(sender, messages.commands_fadd_already, args[2]);
                                            return true;
                                        } else {
                                            list.add(args[2]);
                                            friends.saveFriends(sender.getName(), list);
                                            msg(sender, messages.commands_fadd_added, args[2]);
                                            return true;
                                        }
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
                                        List list = friends.getFriends(sender.getName());
                                        if (list.contains(args[2])) {
                                            list.remove(args[2]);
                                            friends.saveFriends(sender.getName(), list);
                                            msg(sender, messages.commands_frem_removed, args[2]);
                                            return true;
                                        } else {
                                            msg(sender, messages.commands_frem_notin, args[2]);
                                            return true;
                                        }
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
                                                //String query = "UPDATE cc_blocks SET owner = '"+args[3].toLowerCase()+"' WHERE owner = '"+sender.getName()+"'";
                                                //db.executeQuery(query);
                                                //TODO: Transfer Ownship
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

    /*
     * Add command [/cc add]
     */
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
    
    /*
     * Cleanup command [/cc cleanup]
     */
    public boolean cleanupCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
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
                            //TODO: Full Cleanup
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
                                    //TODO: Cleanup Type
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
                                    //TODO: Cleanup Player
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
                                    //TODO: World Cleaup
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
                            //TODO: Cleanup
                            return true;
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
    
    /*
     * Del command [/cc del]
     */
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
    
    /*
     * Selection command [/cc sel]
     */
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
                                    if (args[3].equalsIgnoreCase("?")) {
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
    
    /*
     * Region command [/cc region]
     */
    public boolean regionCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
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
                                                if ((left == null) || (right == null)) {
                                                    msg(sender, messages.sel_null);
                                                } else {
                                                    CreativeSelection sel = new CreativeSelection(left, right);
                                                    setRegion(CreativeRegion.gmType.CREATIVE, args[3], sel);
                                                    msg(sender, messages.commands_region_created, args[3]);
                                                    return true;
                                                }
                                            }
                                        } else
                                        if (args[2].equalsIgnoreCase("survival")) {
                                            if (args[3].equalsIgnoreCase("?")) {
                                                msg(sender, messages.commands_crdefine_help);
                                                msg(sender, messages.commands_srdefine_help);
                                                return true;
                                            } else {
                                                if ((left == null) || (right == null)) {
                                                    msg(sender, messages.sel_null);
                                                } else {
                                                    CreativeSelection sel = new CreativeSelection(left, right);
                                                    setRegion(CreativeRegion.gmType.SURVIVAL, args[3], sel);
                                                    msg(sender, messages.commands_region_created, args[3]);
                                                    return true;
                                                }
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
    
    /*
     * Reload command [/cc reload]
     */
    public boolean reloadCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Reload")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            msg(sender, messages.commands_reloading);
            plugin.reload((Player)sender);
            msg(sender, messages.commands_reloaded);
        }
        if(args.length > 1) {
            msg(sender, messages.commands_reload_help);
            return true;
        }
        return false;
    }
    
    /*
     * Status command [/cc status]
     */
    public boolean statusCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeSQLDatabase      db        = CreativeControl.getDb();
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlockCache       cache     = CreativeControl.getCache();
        if (!plugin.hasPerm(sender, "Commands.Status")) {
            msg(sender, messages.commands_noperm);
            return false;
        } else {
            if (args.length > 1) {
                msg(sender, messages.commands_status_help);
                return true;
            } else {
                msg(sender, messages.commands_status_queue, db.getQueue());
                msg(sender, messages.commands_status_sqlreads, db.reads);
                msg(sender, messages.commands_status_sqlwrites, db.writes);
                msg(sender, messages.commands_status_cache, cache.getSize(), cache.getMax());
                msg(sender, messages.commands_status_cachereads, cache.getReads());
                msg(sender, messages.commands_status_cachewrites, cache.getWrites());
                return true;
            }
        }
    }
    
    /*
     * Tool command [/cc tool]
     */
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
                                        //TODO: Add Tool
                                        msg(sender, messages.commands_tool_act);
                                        return true;
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
                                        //TODO: Del Tool
                                        msg(sender, messages.commands_tool_dec);
                                        return true;
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
    
    public void setRegion(CreativeRegion.gmType type, String name, CreativeSelection sel) {
        CreativeRegionCreator    region    = CreativeControl.getRegioner();
        CreativeControl.getRegions().add(type, name, sel);
        region.saveRegion(name, type, sel);
    }

    private void removeRegion(String string) {
        CreativeRegionCreator    region    = CreativeControl.getRegioner();
        region.deleteRegion(string);
    }
    
    public void msg(CommandSender sender, String s, Object... objects) {
        CreativeCommunicator     com       = CreativeControl.getCommunicator();
        com.msg(sender, s, objects);
    }
}