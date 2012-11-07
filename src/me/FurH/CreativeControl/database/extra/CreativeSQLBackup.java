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

package me.FurH.CreativeControl.database.extra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeSQLBackup extends Thread {
    
    public static void backup(List<String> backup) {        
        CreativeCommunicator com = CreativeControl.getCommunicator();
        CreativeControl plugin = CreativeControl.getPlugin();
        
        File dir = new File(plugin.getDataFolder() + File.separator + "backups" + File.separator);
        if (!dir.exists()) { dir.mkdirs(); }
        
        PrintWriter writer = null;
        
        try {
            writer = new PrintWriter(new File(dir.getAbsolutePath(), "backup-" + CreativeUtil.getSimpleDate(System.currentTimeMillis()) + ".sql"));
        } catch (FileNotFoundException ex) {
            com.error("[TAG] Failed to backup the protections, {0}", ex, ex.getMessage());
        }

        for (String query : backup) {
            writer.println(query + ";");
        }

        writer.close();
    }
}
