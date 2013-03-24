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

package me.FurH.CreativeControl.permissions;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeGroupManager implements CreativePermissionsInterface {

    private GroupManager groupManager;

    public CreativeGroupManager(Plugin manager) {
        this.groupManager = (GroupManager) manager;
    }

    @Override
    public boolean hasPerm(Player player, String node) {
        AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(player);

        if (handler == null) {
            return player.hasPermission(node);
        }

        return handler.has(player, node);
    }
}
