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

package me.FurH.CreativeControl.integration;

import org.bukkit.entity.Player;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;

/**
 *
 * @author FurmigaHumana
 */
public class AuthMe {

    public static boolean isLoggedIn(Player player) {
        boolean li = PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase());
        return li;
    }

    public static boolean isLoggedInComplete(Player player) {
        boolean li = isLoggedIn(player) && LimboCache.getInstance().getLimboPlayer(player.getName().toLowerCase()) == null;
        return li;
    }
}