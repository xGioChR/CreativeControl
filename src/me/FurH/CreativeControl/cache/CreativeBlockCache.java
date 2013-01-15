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

import me.FurH.CreativeControl.CreativeControl;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockCache {
    private static CreativeLRUCache<String, String[]> cache;
    public int cacheSize = 0;
    
    public CreativeBlockCache() {
        this.cacheSize = CreativeControl.getMainConfig().cache_capacity;
        cache = new CreativeLRUCache<String, String[]>(cacheSize);
    }

    public boolean contains(String node) {
        return cache.containsKey(node);
    }

    public void add(String node, String[] value) {
        cache.put(node, value);
    }

    public void add(String node) {
        cache.put(node, null);
    }

    public void remove(String node) {
        cache.remove(node);
    }
    
    public String[] get(String node) {
        return cache.get(node);
    }

    public int getWrites() {
        return cache.getWrites();
    }

    public int getReads() {
        return cache.getReads();
    }

    public int getSize() {
        return cache.size();
    }

    public int getMaxSize() {
        return cacheSize;
    }

    public int clear() {
        int total = 0;
        
        total += cache.size();
        cache.clear();
        
        return total;
    }
}
