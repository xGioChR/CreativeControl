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

package me.FurH.CreativeControl.util;

import java.util.HashSet;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.Core.number.NumberUtils;
import me.FurH.Core.time.TimeUtils;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeUtil {

    /*
     * return true if the first line of the sign is listed as a economy sign
     */
    public static boolean isBlackListedSign(org.bukkit.block.Sign sign) {

        String line1 = removeCodes(sign.getLine(0).replaceAll(" ", "_"));
        String line2 = removeCodes(sign.getLine(1).replaceAll(" ", "_"));
        String line3 = removeCodes(sign.getLine(2).replaceAll(" ", "_"));
        String line4 = removeCodes(sign.getLine(3).replaceAll(" ", "_"));

        CreativeWorldNodes config = CreativeControl.getWorldNodes(sign.getWorld());

        return config.black_sign.contains(line1) || config.black_sign.contains(line2) || config.black_sign.contains(line3) || config.black_sign.contains(line4);
    }

    private static String removeCodes(String line) {
        return line.toLowerCase().replaceAll("§([0-9a-fk-or])", "").replaceAll("[^a-zA-Z0-9]", "");
    }

    /*
     * return a HashSet of the List contends
     */
    public static HashSet<String> toStringHashSet(String string, String split) {

        try {
            return CollectionUtils.toStringHashSet(string, split);
        } catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
        }

        return null;
    }
    
    /*
     * return a HashSet of the List contends
     */
    public static HashSet<Integer> toIntegerHashSet(String string, String split) {
        
        try {
            return CollectionUtils.toIntegerHashSet(string, split);
        } catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
        }
        
        return null;
    }
    
    /*
     * return a Integer
     */
    public static int toInteger(String str) {

        try {
            return NumberUtils.toInteger(str);
        } catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
        }
        
        return 0;
    }
    
    public static double toDouble(String str) {

        try {
            return NumberUtils.toDouble(str);
        } catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
        }
        
        return 0;
    }

    public static String getSimpleDate(long date) {
        return TimeUtils.getSimpleFormatedTime(date);
    }

    /*
     * return the date in the defined miliseconds
     */
    public static String getDate(long date) {
        return TimeUtils.getFormatedTime(date);
    }
}