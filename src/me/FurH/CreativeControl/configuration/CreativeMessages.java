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

package me.FurH.CreativeControl.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeCommunicator.Type;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMessages {
    public String prefix_tag                     = "prefix.tag";
    public String prefix_small                   = "prefix.small";
    
    public String allblocks_othername            = "ingame.selection.otherName";
    public String allblocks_selnull              = "ingame.selection.selnull";
    public String allblocks_selsize              = "ingame.selection.size";
    public String allblocks_while                = "ingame.selection.while";
    public String allblocks_processed            = "ingame.selection.processed";

    public String player_cantuse                 = "ingame.player.cantusethis";
    public String player_cmdblacklist            = "ingame.player.cmdblacklist";
    public String player_cantdrop                = "ingame.player.cantdrop";
    public String player_chicken                 = "ingame.player.chicken";
    public String player_cantuse2                = "ingame.player.cantuse";
    public String player_cantdo                  = "ingame.player.cantdo";
    
    public String updater_new                    = "ingame.updater.new";
    public String updater_visit                  = "ingame.updater.visit";
    
    public String blocks_nocreative              = "ingame.blocks.nocreative";
    public String blocks_nosurvival              = "ingame.blocks.nosurvival";
    public String blocks_pertence                = "ingame.blocks.pertence";
    public String blocks_cantplace               = "ingame.blocks.cantplace";
    public String blocks_wither                  = "ingame.blocks.wither";
    public String blocks_snowgolem               = "ingame.blocks.snowgolem";
    public String blocks_irongolem               = "ingame.blocks.irongolem";
    public String blocks_bedrock                 = "ingame.blocks.bedrock";
    public String blocks_cantbreak               = "ingame.blocks.cantbreak";
    public String blocks_creative                = "ingame.blocks.creative";
    public String blocks_nodrop                  = "ingame.blocks.nodrop";
    
    public String region_cwelcome                = "ingame.region.CreativeWelcome";
    public String region_swelcome                = "ingame.region.SurvivalWelcome";
    public String region_cleave                  = "ingame.region.CreativeLeave";
    public String region_sleave                  = "ingame.region.SurvivalLeave";
    
    public String entity_pvp                     = "ingame.entity.pvp";
    public String entity_mobs                    = "ingame.entity.mob";
    public String entity_vehicle                 = "ingame.entity.vehicle";
    
    public String sel_null                       = "ingame.selection.selnull";
    public String sel_first                      = "ingame.selection.first";
    public String sel_second                     = "ingame.selection.second";
    
    public String commands_type                  = "Commands.type";
    public String commands_noperm                = "Commands.NoPerm";
    public String commands_nothere               = "Commands.NotHere";
    
    public String commands_help1                 = "Commands.Help.cmd1";
    public String commands_help2                 = "Commands.Help.cmd2";
    public String commands_help3                 = "Commands.Help.cmd3";
    public String commands_help4                 = "Commands.Help.cmd4";
    public String commands_help5                 = "Commands.Help.cmd5";
    public String commands_help6                 = "Commands.Help.cmd6";
    public String commands_help7                 = "Commands.Help.cmd7";
    public String commands_help8                 = "Commands.Help.cmd8";
    public String commands_help9                 = "Commands.Help.cmd9";
    public String commands_help10                = "Commands.Help.cmd10";
    public String commands_help11                = "Commands.Help.cmd11";
    
    public String commands_flist_usage           = "Commands.Friend.list.usage";
    public String commands_flist_help            = "Commands.Friend.list.help";
    public String commands_flist_nofriends       = "Commands.Friend.list.hnoFriends";
    public String commands_flist_friends         = "Commands.Friend.list.Friends";
    public String commands_flist_unofriends      = "Commands.Friend.list.unoFriends";
    
    public String commands_fadd_usage            = "Commands.Friend.add.usage";
    public String commands_fadd_help             = "Commands.Friend.add.help";
    public String commands_fadd_added            = "Commands.Friend.add.added";
    public String commands_fadd_already          = "Commands.Friend.add.already";
    
    public String commands_frem_usage            = "Commands.Friend.rem.usage";
    public String commands_frem_help             = "Commands.Friend.rem.help";
    public String commands_frem_empty            = "Commands.Friend.rem.empty";
    public String commands_frem_removed          = "Commands.Friend.rem.removed";
    public String commands_frem_notin            = "Commands.Friend.rem.notin";
    
    public String commands_fallow_usage          = "Commands.Friend.allow.usage";
    public String commands_fallow_help           = "Commands.Friend.allow.help";
    
    public String commands_ftrans_usage          = "Commands.Friend.transfer.usage";
    public String commands_ftrans_help           = "Commands.Friend.transfer.help";
    
    public String commands_cleanup_processed     = "Commands.Cleanup.processed";
    public String commands_cleanup_more          = "Commands.Cleanup.morehelp";
    
    public String commands_ccleanup_help         = "Commands.Cleanup.cleanup.help";
    public String commands_ccleanup_usage        = "Commands.Cleanup.cleanup.usage";
    
    public String commands_acleanup_help         = "Commands.Cleanup.all.help";
    public String commands_acleanup_usage        = "Commands.Cleanup.all.usage";
    
    public String commands_pcleanup_help         = "Commands.Cleanup.player.help";
    public String commands_pcleanup_usage        = "Commands.Cleanup.player.usage";
    
    public String commands_wcleanup_help         = "Commands.Cleanup.world.help";
    public String commands_wcleanup_usage        = "Commands.Cleanup.world.usage";
    
    public String commands_tcleanup_help         = "Commands.Cleanup.type.help";
    public String commands_tcleanup_usage        = "Commands.Cleanup.type.usage";
    
    public String commands_scheck_help           = "Commands.Check.status.help";
    public String commands_scheck_there          = "Commands.Check.status.there";
    public String commands_scheck_more           = "Commands.Check.morehelp";
    
    public String commands_pcheck_help           = "Commands.Check.player.help";
    public String commands_pcheck_gm             = "Commands.Check.player.hasGm";
    public String commands_pcheck_noton          = "Commands.Check.player.notOn";
    
    public String commands_padd_help             = "Commands.Add.player.help";
    public String commands_padd_usage            = "Commands.Add.player.usage";
    public String commands_padd_more             = "Commands.Add.morehelp";
    
    public String commands_sadd_hep              = "Commands.Add.help";
    public String commands_sadd_usag             = "Commands.Add.usage";
    
    public String commands_del_more              = "Commands.Del.morehelp";
    
    public String commands_adel_help             = "Commands.Del.all.help";
    public String commands_adel_usage            = "Commands.Del.all.usage";
    
    public String commands_tdel_help             = "Commands.Del.type.help";
    public String commands_tdel_usage            = "Commands.Del.type.usage";
    
    public String commands_pdel_help             = "Commands.Del.player.help";
    public String commands_pdel_usage            = "Commands.Del.player.usage";
    
    public String commands_sel_more              = "Commands.Selection.morehelp";
    public String commands_sel_number            = "Commands.Selection.number";
    
    public String commands_usel_help             = "Commands.Selection.up.help";
    public String commands_usel_expended         = "Commands.Selection.up.expanded";
    public String commands_usel_usage            = "Commands.Selection.up.usage";
    
    public String commands_dsel_help             = "Commands.Selection.down.help";
    public String commands_dsel_expended         = "Commands.Selection.down.expanded";
    public String commands_dsel_usage            = "Commands.Selection.down.usage";
    
    public String commands_vsel_help             = "Commands.Selection.vert.help";
    public String commands_vsel_expended         = "Commands.Selection.vert.expanded";
    public String commands_vsel_usage            = "Commands.Selection.vert.usage";
    
    public String commands_region_define         = "Commands.Region.define";
    public String commands_region_more           = "Commands.Region.morehelp";
    public String commands_region_created        = "Commands.Region.Created";
    
    public String commands_crdefine_help         = "Commands.Region.Creative.DefineHelp";
    public String commands_crdefine_usage        = "Commands.Region.Creative.Usage";
    
    public String commands_srdefine_help         = "Commands.Region.Survival.DefineHelp";
    public String commands_srdefine_usage        = "Commands.Region.Survival.Usage";
    
    public String commands_cremove_usage         = "Commands.Region.Remove.Usage";
    public String commands_cremove_help          = "Commands.Region.Remove.RemoveHelp";
    public String commands_cremove_sucess        = "Commands.Region.Remove.Success";
    
    public String commands_reloading             = "Commands.Reload.reloading";
    public String commands_reloaded              = "Commands.Reload.reloaded";
    public String commands_reload_help           = "Commands.Reload.help";
    
    public String commands_status_help           = "Commands.Status.help";
    public String commands_status_queue          = "Commands.Status.Queue";
    public String commands_status_sqlreads       = "Commands.Status.Reads";
    public String commands_status_sqlwrites      = "Commands.Status.Writes";
    public String commands_status_cache          = "Commands.Status.Cache";
    public String commands_status_cachereads     = "Commands.Status.CacheReads";
    public String commands_status_cachewrites    = "Commands.Status.CacheWrites";
    
    public String commands_atool_help            = "Commands.Tool.add.help";
    public String commands_atool_usage           = "Commands.Tool.add.usage";
    
    public String commands_dtool_help            = "Commands.Tool.del.help";
    public String commands_dtool_usage           = "Commands.Tool.del.usage";
    
    public String commands_tool_act              = "Commands.Tool.activated";
    public String commands_tool_dec              = "Commands.Tool.deactivated";
    public String commands_tool_more             = "Commands.Tool.morehelp";
    
    public String updater_loading                = "Commands.Updater.Loading";
    public String updater_loadfailed             = "Commands.Updater.LoadFailed";
    public String updater_loaded                 = "Commands.Updater.Loaded";
    public String updater_process                = "Commands.Updater.Process";
    public String updater_duplicated             = "Commands.Updater.Duplicated";
    public String updater_checkfailed            = "Commands.Updater.CheckFailed";
    public String updater_done                   = "Commands.Updater.Done";
    
    public String backup_generating              = "Commands.Backup.Creating";
    public String backup_done                    = "Commands.Backup.Done";
    
    public String cleanup_locked                 = "Commands.CleanupProcess.Locked";
    public String cleanup_searching              = "Commands.CleanupProcess.Searching";
    public String cleanup_process                = "Commands.CleanupProcess.Process";
    public String cleanup_corrupted              = "Commands.CleanupProcess.Corrupted";
    public String cleanup_duplicated             = "Commands.CleanupProcess.Duplicated";
    public String cleanup_checkfailed            = "Commands.CleanupProcess.CheckFailed";
    public String cleanup_done                   = "Commands.CleanupProcess.Done";

    public void load() {
        prefix_tag                     = getMessage("prefix.tag");
        prefix_small                   = getMessage("prefix.small");

        allblocks_othername            = getMessage("ingame.selection.otherName");
        allblocks_selnull              = getMessage("ingame.selection.selnull");
        allblocks_selsize              = getMessage("ingame.selection.size");
        allblocks_while                = getMessage("ingame.selection.while");
        allblocks_processed            = getMessage("ingame.selection.processed");

        player_cantuse                 = getMessage("ingame.player.cantusethis");
        player_cmdblacklist            = getMessage("ingame.player.cmdblacklist");
        player_cantdrop                = getMessage("ingame.player.cantdrop");
        player_chicken                 = getMessage("ingame.player.chicken");
        player_cantuse2                = getMessage("ingame.player.cantuse");
        player_cantdo                  = getMessage("ingame.player.cantdo");

        updater_new                    = getMessage("ingame.updater.new");
        updater_visit                  = getMessage("ingame.updater.visit");

        blocks_nocreative              = getMessage("ingame.blocks.nocreative");
        blocks_nosurvival              = getMessage("ingame.blocks.nosurvival");
        blocks_pertence                = getMessage("ingame.blocks.pertence");
        blocks_cantplace               = getMessage("ingame.blocks.cantplace");
        blocks_wither                  = getMessage("ingame.blocks.wither");
        blocks_snowgolem               = getMessage("ingame.blocks.snowgolem");
        blocks_irongolem               = getMessage("ingame.blocks.irongolem");
        blocks_bedrock                 = getMessage("ingame.blocks.bedrock");
        blocks_cantbreak               = getMessage("ingame.blocks.cantbreak");
        blocks_creative                = getMessage("ingame.blocks.creative");
        blocks_nodrop                  = getMessage("ingame.blocks.nodrop");

        region_cwelcome                = getMessage("ingame.region.CreativeWelcome");
        region_swelcome                = getMessage("ingame.region.SurvivalWelcome");
        region_cleave                  = getMessage("ingame.region.CreativeLeave");
        region_sleave                  = getMessage("ingame.region.SurvivalLeave");

        entity_pvp                     = getMessage("ingame.entity.pvp");
        entity_mobs                    = getMessage("ingame.entity.mob");
        entity_vehicle                 = getMessage("ingame.entity.vehicle");

        sel_null                       = getMessage("ingame.selection.selnull");
        sel_first                      = getMessage("ingame.selection.first");
        sel_second                     = getMessage("ingame.selection.second");
    
        commands_type                  = getMessage("Commands.type");
        commands_noperm                = getMessage("Commands.NoPerm");
        commands_nothere               = getMessage("Commands.NotHere");

        commands_help1                 = getMessage("Commands.Help.cmd1");
        commands_help2                 = getMessage("Commands.Help.cmd2");
        commands_help3                 = getMessage("Commands.Help.cmd3");
        commands_help4                 = getMessage("Commands.Help.cmd4");
        commands_help5                 = getMessage("Commands.Help.cmd5");
        commands_help6                 = getMessage("Commands.Help.cmd6");
        commands_help7                 = getMessage("Commands.Help.cmd7");
        commands_help8                 = getMessage("Commands.Help.cmd8");
        commands_help9                 = getMessage("Commands.Help.cmd9");
        commands_help10                = getMessage("Commands.Help.cmd10");
        commands_help11                = getMessage("Commands.Help.cmd11");

        commands_flist_usage           = getMessage("Commands.Friend.list.usage");
        commands_flist_help            = getMessage("Commands.Friend.list.help");
        commands_flist_nofriends       = getMessage("Commands.Friend.list.hnoFriends");
        commands_flist_friends         = getMessage("Commands.Friend.list.Friends");
        commands_flist_unofriends      = getMessage("Commands.Friend.list.unoFriends");

        commands_fadd_usage            = getMessage("Commands.Friend.add.usage");
        commands_fadd_help             = getMessage("Commands.Friend.add.help");
        commands_fadd_added            = getMessage("Commands.Friend.add.added");
        commands_fadd_already          = getMessage("Commands.Friend.add.already");

        commands_frem_usage            = getMessage("Commands.Friend.rem.usage");
        commands_frem_help             = getMessage("Commands.Friend.rem.help");
        commands_frem_empty            = getMessage("Commands.Friend.rem.empty");
        commands_frem_removed          = getMessage("Commands.Friend.rem.removed");
        commands_frem_notin            = getMessage("Commands.Friend.rem.notin");

        commands_fallow_usage          = getMessage("Commands.Friend.allow.usage");
        commands_fallow_help           = getMessage("Commands.Friend.allow.help");

        commands_ftrans_usage          = getMessage("Commands.Friend.transfer.usage");
        commands_ftrans_help           = getMessage("Commands.Friend.transfer.help");

        commands_cleanup_processed     = getMessage("Commands.Cleanup.processed");
        commands_cleanup_more          = getMessage("Commands.Cleanup.morehelp");

        commands_ccleanup_help         = getMessage("Commands.Cleanup.cleanup.help");
        commands_ccleanup_usage        = getMessage("Commands.Cleanup.cleanup.usage");

        commands_acleanup_help         = getMessage("Commands.Cleanup.all.help");
        commands_acleanup_usage        = getMessage("Commands.Cleanup.all.usage");

        commands_pcleanup_help         = getMessage("Commands.Cleanup.player.help");
        commands_pcleanup_usage        = getMessage("Commands.Cleanup.player.usage");

        commands_wcleanup_help         = getMessage("Commands.Cleanup.world.help");
        commands_wcleanup_usage        = getMessage("Commands.Cleanup.world.usage");

        commands_tcleanup_help         = getMessage("Commands.Cleanup.type.help");
        commands_tcleanup_usage        = getMessage("Commands.Cleanup.type.usage");

        commands_scheck_help           = getMessage("Commands.Check.status.help");
        commands_scheck_there          = getMessage("Commands.Check.status.there");
        commands_scheck_more           = getMessage("Commands.Check.morehelp");

        commands_pcheck_help           = getMessage("Commands.Check.player.help");
        commands_pcheck_gm             = getMessage("Commands.Check.player.hasGm");
        commands_pcheck_noton          = getMessage("Commands.Check.player.notOn");

        commands_padd_help             = getMessage("Commands.Add.player.help");
        commands_padd_usage            = getMessage("Commands.Add.player.usage");
        commands_padd_more             = getMessage("Commands.Add.morehelp");

        commands_sadd_hep              = getMessage("Commands.Add.help");
        commands_sadd_usag             = getMessage("Commands.Add.usage");

        commands_del_more              = getMessage("Commands.Del.morehelp");
        commands_adel_help             = getMessage("Commands.Del.all.help");
        commands_adel_usage            = getMessage("Commands.Del.all.usage");

        commands_tdel_help             = getMessage("Commands.Del.type.help");
        commands_tdel_usage            = getMessage("Commands.Del.type.usage");

        commands_pdel_help             = getMessage("Commands.Del.player.help");
        commands_pdel_usage            = getMessage("Commands.Del.player.usage");

        commands_sel_more              = getMessage("Commands.Selection.morehelp");
        commands_sel_number            = getMessage("Commands.Selection.number");

        commands_usel_help             = getMessage("Commands.Selection.up.help");
        commands_usel_expended         = getMessage("Commands.Selection.up.expanded");
        commands_usel_usage            = getMessage("Commands.Selection.up.usage");

        commands_dsel_help             = getMessage("Commands.Selection.down.help");
        commands_dsel_expended         = getMessage("Commands.Selection.down.expanded");
        commands_dsel_usage            = getMessage("Commands.Selection.down.usage");

        commands_vsel_help             = getMessage("Commands.Selection.vert.help");
        commands_vsel_expended         = getMessage("Commands.Selection.vert.expanded");
        commands_vsel_usage            = getMessage("Commands.Selection.vert.usage");

        commands_region_define         = getMessage("Commands.Region.define");
        commands_region_more           = getMessage("Commands.Region.morehelp");
        commands_region_created        = getMessage("Commands.Region.Created");

        commands_crdefine_help         = getMessage("Commands.Region.Creative.DefineHelp");
        commands_crdefine_usage        = getMessage("Commands.Region.Creative.Usage");

        commands_srdefine_help         = getMessage("Commands.Region.Survival.DefineHelp");
        commands_srdefine_usage        = getMessage("Commands.Region.Survival.Usage");

        commands_cremove_usage         = getMessage("Commands.Region.Remove.Usage");
        commands_cremove_help          = getMessage("Commands.Region.Remove.RemoveHelp");
        commands_cremove_sucess        = getMessage("Commands.Region.Remove.Success");

        commands_reloading             = getMessage("Commands.Reload.reloading");
        commands_reloaded              = getMessage("Commands.Reload.reloaded");
        commands_reload_help           = getMessage("Commands.Reload.help");

        commands_status_help           = getMessage("Commands.Status.help");
        commands_status_queue          = getMessage("Commands.Status.Queue");
        commands_status_sqlreads       = getMessage("Commands.Status.Reads");
        commands_status_sqlwrites      = getMessage("Commands.Status.Writes");
        commands_status_cache          = getMessage("Commands.Status.Cache");
        commands_status_cachereads     = getMessage("Commands.Status.CacheReads");
        commands_status_cachewrites    = getMessage("Commands.Status.CacheWrites");

        commands_atool_help            = getMessage("Commands.Tool.add.help");
        commands_atool_usage           = getMessage("Commands.Tool.add.usage");

        commands_dtool_help            = getMessage("Commands.Tool.del.help");
        commands_dtool_usage           = getMessage("Commands.Tool.del.usage");

        commands_tool_act              = getMessage("Commands.Tool.activated");
        commands_tool_dec              = getMessage("Commands.Tool.deactivated");
        commands_tool_more             = getMessage("Commands.Tool.morehelp");
        
        updater_loading                = getMessage("Commands.Updater.Loading");
        updater_loadfailed             = getMessage("Commands.Updater.LoadFailed");
        updater_loaded                 = getMessage("Commands.Updater.Loaded");
        updater_process                = getMessage("Commands.Updater.Process");
        updater_duplicated             = getMessage("Commands.Updater.Duplicated");
        updater_checkfailed            = getMessage("Commands.Updater.CheckFailed");
        updater_done                   = getMessage("Commands.Updater.Done");
        
        backup_generating              = getMessage("Commands.Backup.Creating");
        backup_done                    = getMessage("Commands.Backup.Done");
        
        cleanup_locked                 = getMessage("Commands.CleanupProcess.Locked");
        cleanup_searching              = getMessage("Commands.CleanupProcess.Searching");
        cleanup_process                = getMessage("Commands.CleanupProcess.Process");
        cleanup_corrupted              = getMessage("Commands.CleanupProcess.Corrupted");
        cleanup_duplicated             = getMessage("Commands.CleanupProcess.Duplicated");
        cleanup_checkfailed            = getMessage("Commands.CleanupProcess.CheckFailed");
        cleanup_done                   = getMessage("Commands.CleanupProcess.Done");
    }
    
    private String getMessage(String node) {
        CreativeCommunicator com    = CreativeControl.getCommunicator();
        CreativeControl      plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder(), "messages.yml");
        if (!dir.exists()) { CreativeUtil.ccFile(plugin.getResource("messages.yml"), dir); }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            if (!config.contains(node)) {
                InputStream resource = plugin.getResource("messages.yml");
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.getString(node));
                    com.log(CreativeControl.tag + "Messages file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log(CreativeControl.tag + "Can't get the message node {0}, contact the developer.", Type.SEVERE, node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error(CreativeControl.tag + "Can't update the messages file: {0}", ex, ex.getMessage());
                }
            }
        } catch (IOException e) {
            com.error(CreativeControl.tag + "Can't load the messages file: {0}", e, e.getMessage());
        } catch (InvalidConfigurationException ex) {
            com.error(CreativeControl.tag + "Can't load the messages file: {0}", ex, ex.getMessage());
            com.log(CreativeControl.tag + " You have a broken message node at: {0}", node);
        }
        
        String value = config.getString(node);
        if (value == null || "".equals(value)) {
            com.log(CreativeControl.tag + " You have a missing message node at: {0}", Type.SEVERE, node);
            value = node;
        }
        return value;
    }
}
