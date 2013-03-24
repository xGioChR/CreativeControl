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

package me.FurH.CreativeControl.integration;

import com.cypherx.xauth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class xAuth {
    
    public static boolean isLoggedIn(Player player) {
        xAuthPlayer xpl = getAuth().getPlayerManager().getPlayer(player.getName());
        boolean li = true;
        if (!xpl.isAuthenticated()) {
            li = false;
        }
        else if (xpl.isGuest()) {
            li = false;
        }
        return li;
    }

    private static com.cypherx.xauth.xAuth getAuth() {
        return (com.cypherx.xauth.xAuth) Bukkit.getServer().getPluginManager().getPlugin("xAuth");
    }
}
