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

package me.FurH.CreativeControl.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.inventory.InventoryStack;
import me.FurH.Core.player.PlayerUtils;
import me.FurH.Core.util.Utils;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import me.FurH.CreativeControl.data.friend.CreativePlayerFriends;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.database.extra.CreativeSQLCleanup;
import me.FurH.CreativeControl.database.extra.CreativeSQLMigrator;
import me.FurH.CreativeControl.manager.CreativeBlockManager;
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
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();

        if (args.length <= 0) {
            msg(sender, "&8[&4CreativeControl&8]&7: &8CreativeControl &4{0} &8by &4FurmigaHumana", plugin.getDescription().getVersion());
            msg(sender, "&8[&4CreativeControl&8]&7: Type '&4/cc help&7' to see the command list");
            return true;
        } else
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("tool")) {
                return toolCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("status")) {
                return statusCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("del")) {
                return delCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("add")) {
                return addCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("admin")) {
                return onAdminCommand(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("check")) {
                return checkCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("cleanup")) {
                return cleanupCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("region")) {
                return regionCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("sel")) {
                return selCmd(sender, cmd, string, args);
            } else
            if ((args[0].equalsIgnoreCase("friend") || (args[0].equalsIgnoreCase("f")))) {
                return friendCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("reload")) {
                return reloadCmd(sender, cmd, string, args);
            } else
            if (args[0].equalsIgnoreCase("set")) {
                return onSetArmorCommand(sender, cmd, string, args);
            }
        }
        
        msg(sender, "&4/cc tool <add/del> &8-&7 Manualy unprotect/protect blocks");
        msg(sender, "&4/cc status &8-&7 Simple cache and database status");
        msg(sender, "&4/cc <add/del> &8-&7 Protect/unprotect blocks inside the selection");
        msg(sender, "&4/cc admin migrator &8-&7 Migrate the database to others types");
        msg(sender, "&4/cc check <status/player> &8-&7 Get player gamemode data");
        msg(sender, "&4/cc cleanup <all/type/player/world/corrupt> &8-&7 Clean the database");
        msg(sender, "&4/cc region <create/remove> &8-&7 Create or remove gamemode regions");
        msg(sender, "&4/cc sel expand <up/down/ver> &8-&7 Expand the current selection");
        msg(sender, "&4/cc friend <add/remove/list/allow/transf> &8-&7 Friend list manager");
        msg(sender, "&4/cc reload &8-&7 Full reload of the plugin");
        return true;
    }
    
    /*
     * /cc set[0] armor[1]
     */
    public boolean onSetArmorCommand(CommandSender sender, Command cmd, String string, String[] args) {

        CreativeMessages    messages    = CreativeControl.getMessages();
        CreativeControl     plugin      = CreativeControl.getPlugin();
        CreativeMainConfig  config      = CreativeControl.getMainConfig();

        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return false;
        }
        
        if (!plugin.hasPerm(sender, "Commands.SetArmor")) {
            msg(sender, "&4You dont have permission to use this command!");
            return true;
        }
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("armor")) {
                Player p = (Player)sender;

                try {

                    config.set(config.getSettingsFile(), "CreativeArmor.Helmet", InventoryStack.getStringFromItemStack(p.getInventory().getHelmet()));
                    config.set(config.getSettingsFile(), "CreativeArmor.Chestplate", InventoryStack.getStringFromItemStack(p.getInventory().getChestplate()));
                    config.set(config.getSettingsFile(), "CreativeArmor.Leggings", InventoryStack.getStringFromItemStack(p.getInventory().getLeggings()));
                    config.set(config.getSettingsFile(), "CreativeArmor.Boots", InventoryStack.getStringFromItemStack(p.getInventory().getBoots()));

                } catch (CoreException ex) {
                    plugin.error(ex, "Failed to set the creative armor data");
                }

                config.updateConfig();
                config.load();
                
                msg(sender, "&7Creative armor defined as your current armor");
                return true;
            }
        }
        
        msg(sender, "&4/cc set armor &8-&7 Set your current armor as the creative armor");
        return true;
    }

    /*
     * //cc admin[0] migrator[1] [>sqlite/>mysql/>lwc][2]
     */
    public boolean onAdminCommand(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages messages = CreativeControl.getMessages();
        CreativeControl plugin = CreativeControl.getPlugin();
        
        if (!plugin.hasPerm(sender, "Commands.Admin")) {
            msg(sender, "&4You dont have permission to use this command!");
            return true;
        }

        if (args.length > 2) {
            if (args[2].equalsIgnoreCase(">sqlite") || args[2].equalsIgnoreCase(">mysql") || args[2].equalsIgnoreCase(">h2")) {
                CreativeSQLMigrator migrator = null;

                if (sender instanceof Player) {
                    migrator = new CreativeSQLMigrator(plugin, (Player)sender, args[2]);
                } else {
                    migrator = new CreativeSQLMigrator(plugin, null, args[2]);
                }

                if (CreativeSQLMigrator.lock) {
                    msg(sender, "&4The migrator is already running!");
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, migrator);
                }

                return true;
            }
        }

        msg(sender, "&4/cc admin migrator >sqlite &8-&7 Convert actual database to SQLite");
        msg(sender, "&4/cc admin migrator >mysql &8-&7 Convert actual database to MySQL");
        msg(sender, "&4/cc admin migrator >h2 &8-&7 Convert actual database to H2");
        return true;
    }

    /*
     * /cc f[0] list[1] [<player>][2]
     * /cc f[0] add[1] <player>[2]
     * /cc f[0] remove[1] <player>[2]
     * /cc f[0] allow[1] <player>[2]
     * /cc f[0] transfer[1] <player/all>[2] <player>[3]
     */
    public boolean friendCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativePlayerFriends    friends   = CreativeControl.getFriends();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        CreativeSQLDatabase      db        = CreativeControl.getDb();

        if (args.length > 3) {
            if (args[1].equalsIgnoreCase("transfer")) {
                if (!args[2].equals("all")) {

                    if (!plugin.hasPerm(sender, "Commands.Friend.transfer.all")) {
                        msg(sender, "&4You dont have permission to use this command!");
                        return true;
                    }

                    msg(sender, "&7Loading...");

                    CreativeBlockManager manager = CreativeControl.getManager();
                    int newOwner = db.getPlayerId(args[3].toLowerCase());
                    int oldOwner = db.getPlayerId(sender.getName().toLowerCase());

                    for (World world : Bukkit.getWorlds()) {
                        db.queue("UPDATE `"+db.prefix+"blocks_"+world.getName()+"` SET owner = '"+newOwner+"' WHERE owner = '"+oldOwner+"'");
                    }

                    manager.clear();

                    msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.");
                    return true;
                }
            }
        } else
        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("add")) {

                if (!plugin.hasPerm(sender, "Commands.Friend.add")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }
                
                if (friends.getFriends(sender.getName()) == null) {
                    HashSet<String> list = CreativeUtil.toStringHashSet(args[2], ", ");
                    
                    friends.saveFriends(sender.getName(), list);
                    msg(sender, "&4{0}&7 was added to your friendlist!", args[2]);
                    
                    list.clear(); list = null;
                    return true;
                } else {
                    HashSet<String> list = friends.getFriends(sender.getName());
                    
                    if (list.contains(args[2])) {
                        msg(sender, "&4{0}&7 is in your friendlist already!", args[2]);
                    } else {
                        list.add(args[2]);
                        friends.saveFriends(sender.getName(), list);
                        msg(sender, "&4{0}&7 was added to your friendlist!", args[2]);
                    }
                    
                    list.clear(); list = null;
                    return true;
                }
            } else
            if (args[1].equalsIgnoreCase("list")) {
                
                if (!plugin.hasPerm(sender, "Commands.Friend.list")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }
                
                if ((friends.getFriends(args[2]) == null) || (friends.getFriends(args[2]).isEmpty())) {
                    msg(sender, "&4{0}&7 has no friends :(", args[2]);
                    return true;
                } else {
                    String list = friends.getFriends(args[2]).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
                    msg(sender, "&4{0}'s &7friends&8: &7{1}", args[2], list);
                    return true;
                }
            } else
            if (args[1].equalsIgnoreCase("remove")) {
                
                if (!plugin.hasPerm(sender, "Commands.Friend.remove")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (friends.getFriends(sender.getName()) == null) {
                    msg(sender, "&7Your friendlist is empty!");
                    return true;
                }
                
                HashSet<String> list = friends.getFriends(sender.getName());
                if (list.contains(args[2])) {
                    list.remove(args[2]);
                    friends.saveFriends(sender.getName(), list);
                    msg(sender, "&4{0}&7 has been removed from your friendlist!", args[2]);
                    return true;
                } else {
                    msg(sender, "&4{0}&7 is not in your friendlist!", args[2]);
                    return true;
                }
            } else
            if (args[1].equalsIgnoreCase("allow")) {
                
                if (!(sender instanceof Player)) {
                    msg(sender, "&4This command can't be used here!");
                    return false;
                }

                if (!plugin.hasPerm(sender, "Commands.Friend.allow")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                selection.allBlocks(sender, args[2], Type.ALLOW);
                return true;
            } else
            if (args[1].equalsIgnoreCase("transfer")) {
                
                if (!(sender instanceof Player)) {
                    msg(sender, "&4This command can't be used here!");
                    return false;
                }

                if (!plugin.hasPerm(sender, "Commands.Friend.transfer")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (!args[2].equals("all")) {
                    selection.allBlocks(sender, args[2], Type.TRANSFER);
                    return true;
                }
            }
        } else
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                
                if (!plugin.hasPerm(sender, "Commands.Friend.list")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }
                
                if ((friends.getFriends(sender.getName()) == null) || (friends.getFriends(sender.getName()).isEmpty())) {
                    msg(sender, "&7Your friendlist is empty!");
                    return true;
                } else {
                    String list = friends.getFriends(sender.getName()).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
                    msg(sender, "&7You friends&8: &7{1}", sender.getName(), list);
                    return true;
                }
            }
        }

        msg(sender, "&4/cc f list [<player>] &8-&7 List all player friends");
        msg(sender, "&4/cc f add <player> &8-&7 Add a new player to your friend list");
        msg(sender, "&4/cc f remove <player> &8-&7 Remove a player from your friend list");
        msg(sender, "&4/cc f allow <player> &8-&7 Allow a player in your block selection");
        msg(sender, "&4/cc f transfer <player/all> [<player>] &8-&7 Transfer the block ownship of all your blocks or from the selection");
        return true;
    }

    /*
     * /cc check[0] status[1]
     * /cc check[0] player[1] <player>[2]
     */
    public boolean checkCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();

        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("player")) {

                if (!plugin.hasPerm(sender, "Commands.Check.player")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                Player player = Bukkit.getPlayer(args[2]);

                if (player != null) {
                    msg(sender, "&7{0} has gamemode &4{1}", player.getName(), player.getGameMode().toString().toLowerCase());
                } else {
                    msg(sender, "&7{0} &4is not&7 online!", args[2]);
                }

                return true;
            }
        } else
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("status")) {
                
                if (!plugin.hasPerm(sender, "Commands.Check.status")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

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

                msg(sender, "&7Here are: &4{0}&7 Survival and &4{1}&7 Creative players", survival, creative);
                return true;
            }
        }

        msg(sender, "&4/cc check status &8-&7 Check the player gamemodes");
        msg(sender, "&4/cc check player <player> &8-&7 Get the player gamemode");
        return true;
    }

    /*
     * /cc add[0] player[1] [<player>][2]
     */
    public boolean addCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();

        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return false;
        }

        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("player")) {

                if (!plugin.hasPerm(sender, "Commands.Add.player")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                selection.allBlocks(sender, args[2], Type.ADD);
                return true;
            }
        }

        if (!plugin.hasPerm(sender, "Commands.Add")) {
            msg(sender, "&4You dont have permission to use this command!");
            return true;
        }
        
        selection.allBlocks(sender, sender.getName(), Type.ADD);
        return true;
    }

    /*
     * /cc cleanup[0] all[1]
     * /cc cleanup[0] corrupt[1]
     * /cc cleanup[0] type[1] <type>[2]
     * /cc cleanup[0] player[1] <player>[2]
     * /cc cleanup[0] world[1] <world>[2]
     */
    public boolean cleanupCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeSQLDatabase      db        = CreativeControl.getDb();
        CreativeBlockManager     manager   = CreativeControl.getManager();

        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("type")) {
                
                if (!plugin.hasPerm(sender, "Commands.Cleanup.type")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                for (World world : Bukkit.getWorlds()) {
                    db.queue("DELETE FROM `"+db.prefix+"blocks_"+world.getName()+"` WHERE type = '"+args[2]+"'");
                }
                
                manager.clear();

                msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.");
                return true;
            } else
            if (args[1].equalsIgnoreCase("player")) {
                
                if (!plugin.hasPerm(sender, "Commands.Cleanup.player")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                for (World world : Bukkit.getWorlds()) {
                    db.queue("DELETE FROM `"+db.prefix+"blocks_"+world.getName()+"` WHERE owner = '"+db.getPlayerId(args[2])+"'");
                }

                msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.");
                return true;
            } else
            if (args[1].equalsIgnoreCase("world")) {
                
                if (!plugin.hasPerm(sender, "Commands.Cleanup.world")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (db.hasTable(db.prefix+"blocks_"+args[2])) {
                    msg(sender, "&4There is no world called {0}", args[2]);
                } else {
                    db.queue("DROP TABLE `"+db.prefix+"blocks_"+args[2]+"`;");
                }

                manager.clear();
                db.load();

                msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.");
                return true;
            }
        } else
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("all")) {
                
                if (!plugin.hasPerm(sender, "Commands.Cleanup.all")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                for (World world : Bukkit.getWorlds()) {
                    db.queue("DROP TABLE `"+db.prefix+"blocks_"+world.getName()+"`");
                }

                manager.clear();
                db.load();

                manager.clear();
                msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.");
                return true;
            } else
            if (args[1].equalsIgnoreCase("corrupt")) {

                if (!plugin.hasPerm(sender, "Commands.Cleanup.corrupt")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                CreativeSQLCleanup cleanup = null;

                if (sender instanceof Player) {
                    cleanup = new CreativeSQLCleanup(CreativeControl.plugin, (Player)sender);
                } else {
                    cleanup = new CreativeSQLCleanup(CreativeControl.plugin, null);
                }

                if (CreativeSQLCleanup.lock) {
                    msg(sender, "&4The cleanup is already running!");
                    return true;
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, cleanup);
                    return true;
                }
            }
        }

        msg(sender, "&4/cc cleanup all &8-&7 Remove all protections");
        msg(sender, "&4/cc cleanup corrupt &8-&7 Remove all corrupt protections");
        msg(sender, "&4/cc cleanup type <typeId> &8-&7 Remove all protections of a type");
        msg(sender, "&4/cc cleanup player <player> &8-&7 Remove all protections of a player");
        msg(sender, "&4/cc cleanup world <world> &8-&7 Remove all protections of a world");
        return true;
    }

    /*
     * /cc del[0] all[1]
     * /cc del[0] type[1] <type>[2]
     * /cc del[0] player[1] <player>[2]
     */
    public boolean delCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();

        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return true;
        }

        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("type")) {

                if (!plugin.hasPerm(sender, "Commands.Del.type")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                selection.allBlocks(sender, args[2], Type.DELTYPE);
                return true;
            } else
            if (args[1].equalsIgnoreCase("player")) {

                if (!plugin.hasPerm(sender, "Commands.Del.player")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                selection.allBlocks(sender, args[2], Type.DELPLAYER);
                return true;
            }
        } else
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("all")) {

                if (!plugin.hasPerm(sender, "Commands.Del.all")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                selection.allBlocks(sender, sender.getName(), Type.DELALL);
                return true;
            }
        }

        return true;
    }

    /*
     * /cc sel[0] expand[1] vert[2]
     * /cc sel[0] expand[1] up[2] <amount>[3]
     * /cc sel[0] expand[1] down[2] <amount>[3]
     */
    public boolean selCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeMainConfig       config    = CreativeControl.getMainConfig();

        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return false;
        }

        if (config.selection_usewe) {
            msg(sender, "&4You must use the worldedit //expand command!");
            return true;
        }

        Player p = (Player) sender;
        if (args.length > 3) {
            if (args[1].equalsIgnoreCase("expand")) {

                if (!plugin.hasPerm(sender, "Commands.Expand")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (args[2].equalsIgnoreCase("up")) {
                    try {
                        int add = Integer.parseInt(args[3]);
                        
                        Location up = plugin.right.get(p);
                        
                        if (add + up.getY() > 255) {
                            up.setY(255);
                        } else {
                            up.add(0, add, 0);
                        }
                        
                        plugin.right.put(p, up);
                        msg(sender, "&7Selection expanded successfuly!");
                        return true;
                    } catch (Exception e) {
                        msg(sender, "&4{0} is not a valid number!", args[3]);
                        return true;
                    }
                } else
                if (args[2].equalsIgnoreCase("down")) {
                    try {
                        int add = Integer.parseInt(args[3]);

                        Location down = plugin.left.get(p);
                        
                        if (add - down.getY() < 0) {
                            down.setY(0);
                        } else {
                            down.subtract(0, add, 0);
                        }
                        
                        plugin.left.put(p, down);
                        msg(sender, "&7Selection expanded successfuly!");
                        return true;
                    } catch (Exception e) {
                        msg(sender, "&4{0} is not a valid number!", args[3]);
                        return true;
                    }
                }
            }
        } else
        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("vert")) {
                Location right = plugin.right.get(p);
                Location left = plugin.right.get(p);

                right.setY(255);
                left.setY(0);

                plugin.right.put(p, right);
                plugin.left.put(p, left);

                msg(sender, "&7Selection expanded successfuly!");
                return true;
            }
        }

        msg(sender, "&4/cc sel expand vert &8-&7 Expand the selection from sky to bedrock");
        msg(sender, "&4/cc sel expand up <amount> &8-&7 Expand the selecion X blocks up");
        msg(sender, "&4/cc sel expand down <amount> &8-&7 Expand the selection Y block down");
        return true;
    }
    
    /*
     * /cc region[0] define[1] creative[2] <name>[3]
     * /cc region[0] define[1] survival[2] <name>[3]
     * /cc region[0] remove[1] <name>[3]
     */
    public boolean regionCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        CreativeBlocksSelection  selection = CreativeControl.getSelector();
        CreativeMainConfig       main      = CreativeControl.getMainConfig();

        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return false;
        }

        Player p = (Player) sender;
        
        Location left = plugin.left.get(p);
        Location right = plugin.right.get(p);

        if (args.length > 3) {
            if (args[1].equalsIgnoreCase("define")) {

                if (!plugin.hasPerm(sender, "Commands.Region.define")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                Location start = null;
                Location end = null;

                if (!main.selection_usewe || selection.getSelection(p) == null) {
                    if ((left == null) || (right == null)) {
                        msg(sender, "&4You must select the area first!");
                        return true;
                    }

                    CreativeSelection sel = new CreativeSelection(left, right);

                    start = sel.getStart();
                    end = sel.getEnd();
                } else {
                    Selection sel = selection.getSelection((Player)sender);

                    if (sel == null) {
                        msg(sender, "&4You must select the area first!");
                        return true;
                    }

                    start = sel.getMinimumPoint();
                    end = sel.getMaximumPoint();
                }
                
                GameMode type = null;

                if (args[2].equalsIgnoreCase("creative")) {
                    type = GameMode.CREATIVE;
                } else if (args[2].equalsIgnoreCase("adventure")) {
                    type = GameMode.ADVENTURE;
                } else if (args[2].equalsIgnoreCase("survival")) {
                    type = GameMode.SURVIVAL;
                }

                if (type != null) {
                    setRegion(type, args[3], start, end);
                    msg(sender, "&4{0} &7region created successfully!", type.toString().toLowerCase());
                    return true;
                } else {
                    msg(sender, "&4{0} is not a valid gamemode!", args[2]);
                    return true;
                }
            }
        } else
        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("remove")) {
                
                if (!plugin.hasPerm(sender, "Commands.Region.remove")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }
                
                removeRegion(args[2]);
                msg(sender, "&7Region removed successfully");
                return true;
            }
        }

        msg(sender, "&4/cc region define creative <name> &8-&7 Create a creative region");
        msg(sender, "&4/cc region define survival <name> &8-&7 Create a survival region");
        msg(sender, "&4/cc region remove <name> &8-&7 Remove a region");
        return true;
    }

    public boolean reloadCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();

        if (!plugin.hasPerm(sender, "Commands.Reload")) {
            msg(sender, "&4You dont have permission to use this command!");
            return true;
        }

        msg(sender, "&7Reloading...");
        plugin.reload(sender);
        msg(sender, "&7Reloaded successfuly!");
        
        return true;
    }
    
    public boolean statusCmd(final CommandSender sender, Command cmd, String string, String[] args) {
        final CreativeSQLDatabase      db        = CreativeControl.getDb();
        CreativeMessages         messages  = CreativeControl.getMessages();
        final CreativeControl          plugin    = CreativeControl.getPlugin();
        final CreativeBlockManager     manager   = CreativeControl.getManager();

        if (!plugin.hasPerm(sender, "Commands.Status")) {
            msg(sender, "&4You dont have permission to use this command!");
            return true;
        }
        
        msg(sender, "&8Loading...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                
                World world = null;
                long lastSize = 0;

                for (World w : Bukkit.getWorlds()) {
                    try {
                        
                        long size = db.getTableSize(db.prefix+"blocks_"+w.getName());
                        if (size > lastSize) {
                            lastSize = size;
                            world = w;
                        }
                    } catch (CoreException ex) {
                        plugin.error(ex, "Failed to get world tables size");
                    }
                }

                Double[] times = new Double[ 10 ];
                Random rnd = new Random();
                
                int id = 56;

                while (!manager.isprotectable(world, id)) {
                    id = rnd.nextInt(256);
                }
                
                msg(sender, "&7Testing table &8'"+db.prefix+"blocks_"+world.getName()+"'&7 with id &8'" + id + "'&7...");

                for (int j1 = 0; j1 < times.length; j1++) {
                    double start = (double) System.currentTimeMillis();

                    int x = rnd.nextInt(30000000);
                    int y = rnd.nextInt(256);
                    int z = rnd.nextInt(30000000);

                    manager.isprotected(world, x, y, z, id, false);

                    times[ j1 ] = ((double) System.currentTimeMillis()) - start;
                }

                double average = PlayerUtils.getAverage(times);
                double max = Collections.max(Arrays.asList(times));
                double min = Collections.min(Arrays.asList(times));

                msg(sender, "&4Queue size&8:&7 {0}", db.getQueueSize());
                msg(sender, "&4Database reads&8:&7 {0}", db.getReads());
                msg(sender, "&4Database writes&8:&7 {0}", db.getWrites());
                msg(sender, "&4Database size&8:&7 {0} / {1}", Utils.getFormatedBytes(manager.getTablesSize()), Utils.getFormatedBytes(manager.getTablesFree()));
                try {
                    msg(sender, "&4Database type&8:&7 {0}, &4ping&8:&7 {1} ms, &4LocalHost&8:&7 {2}", db.getDatabaseEngine(), db.ping() > 0 ? db.ping() : "<1", db.isLocalHost());
                } catch (CoreException ex) { }
                msg(sender, "&4Blocks protected&8:&7 {0}", manager.getTotal());
                msg(sender, "&4Cache reads&8:&7 {0}", manager.getCache().getReads());
                msg(sender, "&4Queue writes&8:&7 {0}", manager.getCache().getWrites());
                msg(sender, "&4Cache size&8:&7 {0}/{1}", manager.getCache().size(), manager.getCache().getMaxSize());
                msg(sender, "&4Database Time&8:&7 {0} ms &8[&7 {1} max &8/&7 {2} min &8]", average, max, min);
            }
        });

        return true;
    }

    /*
     * /cc tool[0] add[1]
     * /cc tool[0] del[1]
     */
    public boolean toolCmd(CommandSender sender, Command cmd, String string, String[] args) {
        CreativeMessages         messages  = CreativeControl.getMessages();
        CreativeControl          plugin    = CreativeControl.getPlugin();
        
        if (!(sender instanceof Player)) {
            msg(sender, "&4This command can't be used here!");
            return false;
        }

        Player p = (Player) sender;
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("add")) {

                if (!plugin.hasPerm(p, "Commands.Tool.add")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (plugin.mods.containsKey(p.getName())) {
                    msg(sender, "&7Tool deactivated!");
                    plugin.mods.remove(p.getName());
                    return true;
                } else {
                    plugin.mods.put(p.getName(), 0);
                    msg(sender, "&7Touch the block to protect");
                    return true;
                }
            } else 
            if (args[1].equalsIgnoreCase("del")) {

                if (!plugin.hasPerm(p, "Commands.Tool.del")) {
                    msg(sender, "&4You dont have permission to use this command!");
                    return true;
                }

                if (plugin.mods.containsKey(p.getName())) {
                    msg(sender, "&7Tool deactivated!");
                    plugin.mods.remove(p.getName());
                    return true;
                } else {
                    plugin.mods.put(p.getName(), 1);
                    msg(sender, "&7Touch the block to unprotect");
                    return true;
                }
            }
        }

        msg(sender, "&4/cc tool add &8-&7 Manualy protect blocks");
        msg(sender, "&4/cc tool del &8-&7 Manualy unprotect blocks");
        return true;
    }
    
    public void setRegion(GameMode type, String name, Location start, Location end) {
        CreativeRegionManager    region    = CreativeControl.getRegioner();
        region.addRegion(name, start, end, type.toString());
        region.saveRegion(name, type, start, end);
    }

    private void removeRegion(String string) {
        CreativeControl.getRegioner().deleteRegion(string);
    }

    public void msg(CommandSender sender, String s, Object... objects) {
        CreativeControl.plugin.getCommunicator().msg(sender, s, objects);
    }
}