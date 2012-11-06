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

package me.FurH.CreativeControl.util;

import java.text.MessageFormat;
import java.util.logging.Logger;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeMainConfig;
import me.FurH.CreativeControl.configuration.CreativeMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeCommunicator {
    private static final Logger logger = Logger.getLogger("minecraft");
    
    /*
     * send a message to the player if is not null, otherside log to console
     */
    public void msg(Player p, String message, Object...objects) {
        if (message == null || "".equals(message)) { return; }
        
        if (p != null) {
            CreativeMainConfig config   = CreativeControl.getMainConfig();
            if (!config.com_quiet) {
                p.sendMessage(format(message, true, objects));
            }
        } else {
            log(message, objects);
        }
    }
    
    /*
     * send a mensagen to the commandsender
     */
    public void msg(CommandSender sender, String message, Object...objects) {
        if (sender instanceof Player) {
            sender.sendMessage(format(message, true, objects));
        } else {
            log(message, objects);
        }
    }
    
    /*
     * Shortcut, logs and dump a throwable
     */
    public void error(String message, Throwable ex, Object...objects) {
        error(message, Type.SEVERE, ex, objects);
    }
    
    /*
     * Logs and dump a throwable
     */
    public void error(String message, Type type, Throwable ex, Object...objects) {
        log(message, type, objects);
        CreativeMainConfig config   = CreativeControl.getMainConfig();
        if (config.com_debugstack) {
            ex.printStackTrace();
        }
        CreativeControl    plugin   = CreativeControl.getPlugin();
        log("[TAG] This error is avaliable at: plugins/{0}/error/{1}", type, plugin.getDescription().getName(), CreativeUtil.stack(ex));
    }
    
    /*
     * Shortcut, logs to console
     */
    public void log(String message, Object...objects) {
        log(message, Type.INFO, objects);
    }
    
    /*
     * Logs something to console
     */
    public void log(String s, Type type, Object... objects) {
        if (s == null || "".equals(s)) { return; }
        
        if (type == Type.INFO) {
            logger.info(format(s, false, objects));
        }
        if (type == Type.SEVERE) {
            logger.severe(format(s, false, objects));
        }
        if (type == Type.WARNING) {
            logger.warning(format(s, false, objects));
        }
        if (type == Type.DEBUG) {
            CreativeMainConfig config   = CreativeControl.getMainConfig();
            if (config.com_debugcons) {
                logger.info(format(s, false, objects));
            }
        }
    }

    /*
     * Format the message string
     */
    private String format(String s, boolean b, Object...objects) {        
        s = MessageFormat.format(s, objects);

        CreativeMessages   messages = CreativeControl.getMessages();
        if (s.contains("[TAG]")) {
            s = s.replaceAll("\\[TAG\\]", messages.prefix_tag);
        }

        if (s.contains("[SMALL]")) {
            s = s.replaceAll("\\[SMALL\\]", messages.prefix_small);
        }

        if (b) {
            return s.replaceAll("&([0-9a-fk-or])", "\u00a7$1");
        } else {
            return s.replaceAll("&([0-9a-fk-or])", "");
        }
    }

    /*
     * The levels of log
     */
    public enum Type { 
        INFO, WARNING, SEVERE, DEBUG; 
    }
}