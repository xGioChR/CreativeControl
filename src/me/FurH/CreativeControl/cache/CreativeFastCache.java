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

package me.FurH.CreativeControl.cache;

import java.util.HashSet;
import me.FurH.CreativeControl.CreativeControl;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeFastCache {
    private HashSet<String> cache = new HashSet<String>(getMax());
    private int writes = 0;
    private int reads = 0;

    public int getMax() {
        return CreativeControl.getMainConfig().cache_capacity;
    }

    public void add(String node) {
        writes++;
        cache.add(node);
    }

    public void remove(String node) {
        reads++;
        cache.remove(node);
    }
    
    public boolean contains(String node){
        reads++;
        return cache.contains(node);
    }

    public int getWrites() {
        return writes;
    }

    public int getReads() {
        return reads;
    }

    public int getSize() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
