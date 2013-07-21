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

package me.FurH.CreativeControl.manager;

import java.util.HashSet;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBlockData {
    
    public String owner;
    public int type;
    public HashSet<String> allowed;
    public String date;
    
    public CreativeBlockData(int type) {
        this.type = type;
    }
    
    public CreativeBlockData(String owner, int type, HashSet<String> allowed) {
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
    }
    
    public CreativeBlockData(String owner, int type, HashSet<String> allowed, String date) {
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
        this.date = date;
    }
}
