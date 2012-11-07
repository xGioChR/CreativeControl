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

package me.FurH.CreativeControl.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockMatcher {
    
    public static List<Block> getAttached(Block block) {
        List<Block> blocks = new ArrayList<Block>();
        
        List BLOCKUP = Arrays.asList(new Integer[] { 6, 31, 32, 37, 38, 39, 40, 55, 59, 63, 70, 72, 76, 78, 81, 83, 92, 93, 94, 104, 105, 111, 115, 132, 141, 142 });
        Block relative = block.getRelative(BlockFace.UP);
        if ((relative != null) && (relative.getType() != Material.AIR)) {
            if (BLOCKUP.contains(relative.getTypeId())) {
                if (!blocks.contains(relative)) {
                    blocks.add(relative);
                }
            }
        }

        BlockFace[] ALLFACES = new BlockFace[] { BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        for (BlockFace face : ALLFACES) {
            relative = block.getRelative(face);
            if ((relative != null) && (relative.getType() != Material.AIR)) {
                if (isAttach(relative, face) != null) {
                    if (!blocks.contains(relative)) {
                        blocks.add(relative);
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /*
     * Return the block if it is attached to the block breaked
     */
    public static Block isAttach(Block block, BlockFace face) {
        if ((block.getTypeId () == 27) || (block.getTypeId () == 28) || (block.getTypeId () == 66)) {
            if (getRailsOff(block, face) != null) { return block; }
            if (getRailsOn(block, face) != null) { return block; }
        }
        
        if ((block.getTypeId () == 50) || (block.getTypeId () == 75) || (block.getTypeId () == 76)) {
            if (getTorches(block, face) != null) { return block; }
        }
        
        if (block.getTypeId() == 68) {
            if (getSignsWall(block, face) != null) { return block; }
        }
        
        if (block.getTypeId() == 65) {
            if (getLadders(block, face) != null) { return block; }
        }
        
        if (block.getTypeId() == 69 || block.getTypeId() == 131) {
            if (getLeversOff(block, face) != null) { return block; }
            if (getLeversOnn(block, face) != null) { return block; }
        }
        
        if (block.getTypeId() == 77 || block.getTypeId() == 143) {
            if (getButtonsOff(block, face) != null) { return block; }
            if (getButtonsOnn(block, face) != null) { return block; }
        }
        
        if (block.getTypeId() == 96) {
            if (getTrapsClosed(block, face) != null) { return block; }
            if (getTrapsOpen(block, face) != null) { return block; }
        }

        if (block.getTypeId() == 106) {
            if (getVines(block, face) != null) { return block; }
        }
        return null;
    }
    
    /*
     * Match Rails off [IDs: 27/off, 28, 66]
     */
    private static Block getRailsOff(Block block, BlockFace face) {
        int UP1 = 0; int UP2 = 1;
        int NORTH = 2; int SOUTH = 3; int WEST = 4; int EAST = 5;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Rails on [IDs: 27/onn, 28]
     */
    private static Block getRailsOn(Block block, BlockFace face) {
        int UP1 = 8; int UP2 = 9;
        int NORTH = 10; int SOUTH = 11; int WEST = 12; int EAST = 13;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Torches/Redstone Torches [IDs: 50, 75, 76]
     */
    private static Block getTorches(Block block, BlockFace face) {
        int UP1 = 5; int UP2 = -1;
        int NORTH = 2; int SOUTH = 1; int WEST = 3; int EAST = 4;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Sign Wall [IDs: 68]
     */
    private static Block getSignsWall(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 4; int SOUTH = 5; int WEST = 3; int EAST = 2;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Ladders [IDs: 65]
     */
    private static Block getLadders(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 4; int SOUTH = 5; int WEST = 3; int EAST = 2;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Levers Off [IDs: 69]
     */
    private static Block getLeversOff(Block block, BlockFace face) {
        int UP1 = 5; int UP2 = 6;
        int NORTH = 2; int SOUTH = 1; int WEST = 3; int EAST = 4;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Levers Onn [IDs: 69]
     */
    private static Block getLeversOnn(Block block, BlockFace face) {
        int UP1 = 13; int UP2 = 14;
        int NORTH = 10; int SOUTH = 9; int WEST = 11; int EAST = 12;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Buttons Off [IDs: 77]
     */
    private static Block getButtonsOff(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 2; int SOUTH = 1; int WEST = 3; int EAST = 4;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Buttons Onn [IDs: 77]
     */
    private static Block getButtonsOnn(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 10; int SOUTH = 9; int WEST = 11; int EAST = 12;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Traps Closed [IDs: 96]
     */
    private static Block getTrapsClosed(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 2; int SOUTH = 3; int WEST = 1; int EAST = 0;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Traps Open [IDs: 96]
     */
    private static Block getTrapsOpen(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 6; int SOUTH = 7; int WEST = 5; int EAST = 4;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    /*
     * Match Vines
     */
    private static Block getVines(Block block, BlockFace face) {
        int UP1 = -1; int UP2 = -2;
        int NORTH = 8; int SOUTH = 2; int WEST = 4; int EAST = 1;
        return getBlock(block, face, UP1, UP2, NORTH, SOUTH, WEST, EAST);
    }
    
    
    private static Block getBlock(Block block, BlockFace face, int UP1, int UP2, int NORTH, int SOUTH, int WEST, int EAST) {
        int data = block.getData();
        
        if (face == BlockFace.UP && ((data == UP1) || (data == UP2))) {
            return block;
        } else 
        if (face == BlockFace.NORTH && (data == NORTH)) {
            return block;
        } else 
        if (face == BlockFace.SOUTH && (data == SOUTH)) {
            return block;
        } else 
        if (face == BlockFace.WEST && (data == WEST)) {
            return block;
        } else 
        if (face == BlockFace.EAST && (data == EAST)) {
            return block;
        }
        return null;
    }
}
