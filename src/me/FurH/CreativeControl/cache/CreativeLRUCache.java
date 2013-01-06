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

import java.util.LinkedHashMap;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeLRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    private int cacheSize = 0;
    private int reads = 0;
    private int writes = 0;

    public CreativeLRUCache (int cacheSize) {
        super(cacheSize, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    @Override
    public V get(Object key) {
        reads++;
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        writes++;
        return super.put(key, value);
    }
    
    @Override
    public boolean containsValue(Object value) {
        reads++;
        return super.containsValue(value);
    }
    
    @Override
    public boolean containsKey(Object key) {
        reads++;
        return super.containsKey(key);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return (size() > (cacheSize));
    }
    
    public int getReads() {
        return reads;
    }

    public int getWrites() {
        return writes;
    }

    public int getSize() {
        return super.size();
    }

    public int getMaxSize() {
        return cacheSize;
    }
}