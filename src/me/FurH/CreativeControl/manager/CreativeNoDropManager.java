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

package me.FurH.CreativeControl.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.cache.CreativeFastCache;
import me.FurH.CreativeControl.database.CreativeSQLDatabase;
import me.FurH.CreativeControl.util.CreativeCommunicator;
import me.FurH.CreativeControl.util.CreativeUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeNoDropManager {

    public static List<Block> getBlocks(Block b, boolean cattach) {
        List<Block> blocks = new ArrayList<Block>();

        if (cattach) {
            HashSet<Block> attached = CreativeBlockMatcher.getAttached(b);
            for (Block attach : attached) {
                if (isProtected(attach)) {
                    blocks.add(attach);
                }
            }

            if (b.getTypeId() == 64 || b.getTypeId() == 71) {
                Block blockdown = b.getRelative(BlockFace.DOWN);
                if (blockdown.getTypeId() == 64 || blockdown.getTypeId() == 71) {
                    if (isProtected(blockdown)) {
                        blocks.add(blockdown);
                    }
                }
            } else {
                if (isProtected(b)) {
                    blocks.add(b);
                } else {
                    Block blockup = b.getRelative(BlockFace.UP);
                    if (blockup.getTypeId() == 64 || blockup.getTypeId() == 71) {
                        if (isProtected(blockup)) {
                            blocks.add(blockup);
                        }
                    }
                }
            }
        } else {
            if (isProtected(b)) {
                blocks.add(b);
            }
        }
        
        return blocks;
    }

    /*
     * return true if the block is protected
     */
    public static boolean isProtected(Block b) {
        return isProtected(b, false);
    }

    /*
     * return the array representation of the protection
     */
    private static boolean isProtected(Block b, boolean force) {
        CreativeBlockManager manager = CreativeControl.getManager();
        if (!force && !manager.isProtectable(b.getWorld(), b.getTypeId())) {
            return false;
        }

        CreativeFastCache   cache      = CreativeControl.getFastCache();
        String location = CreativeUtil.getLocation(b.getLocation());
        CreativeSQLDatabase  db         = CreativeControl.getDb();
        
        if (cache.contains(location)) {
            return true;
        } else {
            try {
                ResultSet rs = db.getQuery("SELECT location FROM `"+db.prefix+"blocks` WHERE location = '" + location + "'");
                if (rs.next()) {
                    return true;
                }
            } catch (SQLException ex) {
                CreativeCommunicator com        = CreativeControl.getCommunicator();
                com.error("[TAG] Failed to get the block from the database, {0}", ex, ex.getMessage());
                if (!db.isOk()) { db.fix(); }
            }
        }
                
        return false;
    }
}
