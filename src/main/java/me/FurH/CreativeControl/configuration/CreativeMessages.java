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

package me.FurH.CreativeControl.configuration;

import me.FurH.Core.CorePlugin;
import me.FurH.Core.configuration.Configuration;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeMessages extends Configuration {

	public CreativeMessages(CorePlugin plugin) {
		super(plugin);
	}

	public String prefix_tag = "prefix.tag";

	public String mainode_restricted = "MainNode.restricted";

	public String blockplace_cantplace = "BlockPlace.cantplace";

	public String blockmanager_belongs = "BlockManager.belongsto";
	public String blockmanager_unprotected = "BlockManager.unprotected";
	public String blockmanager_already = "BlockManager.alreadyprotected";
	public String blockmanager_protected = "BlockManager.blockprotected";
	public String blockmanager_removed = "BlockManager.blockunprotected";
	public String blockmanager_worldexcluded = "BlockManager.worlddisabled";
	public String blockmanager_excluded = "BlockManager.typeexcluded";
	public String blockmanager_limit = "BlockManager.minutelimit";

	public String blockbreak_cantbreak = "BlockBreak.cantbreak";
	public String blockbreak_survival = "BlockBreak.nosurvival";
	public String blockbreak_creativeblock = "BlockBreak.creativeblock";

	public String limits_vehicles = "Limits.vehicles";

	public String region_welcome = "Regions.welcome";
	public String region_farewell = "Regions.farewell";
	public String region_unallowed = "Regions.survival";
	public String region_cant_change = "Regions.cant_here";

	public String blacklist_commands = "BlackList.commands";

	public String selection_first = "Selection.first_point";
	public String selection_second = "Selection.second_point";

	public void load() {

		prefix_tag = getMessage("prefix.tag");

		mainode_restricted = getMessage("MainNode.restricted");

		blockplace_cantplace = getMessage("BlockPlace.cantplace");

		blockmanager_belongs = getMessage("BlockManager.belongsto");
		blockmanager_unprotected = getMessage("BlockManager.unprotected");
		blockmanager_already = getMessage("BlockManager.alreadyprotected");
		blockmanager_protected = getMessage("BlockManager.blockprotected");
		blockmanager_removed = getMessage("BlockManager.blockunprotected");
		blockmanager_worldexcluded = getMessage("BlockManager.worlddisabled");
		blockmanager_excluded = getMessage("BlockManager.typeexcluded");
		blockmanager_limit = getMessage("BlockManager.minutelimit");

		blockbreak_cantbreak = getMessage("BlockBreak.cantbreak");
		blockbreak_survival = getMessage("BlockBreak.nosurvival");
		blockbreak_creativeblock = getMessage("BlockBreak.creativeblock");

		limits_vehicles = getMessage("Limits.vehicles");

		region_welcome = getMessage("Regions.welcome");
		region_farewell = getMessage("Regions.farewell");
		region_unallowed = getMessage("Regions.unallowed");
		region_cant_change = getMessage("Regions.cant_here");

		blacklist_commands = getMessage("BlackList.commands");

		selection_first = getMessage("Selection.first_point");
		selection_second = getMessage("Selection.second_point");

		updateConfig();
	}
}
