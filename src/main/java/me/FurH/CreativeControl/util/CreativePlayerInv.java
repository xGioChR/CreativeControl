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

package me.FurH.CreativeControl.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author FurmigaHumana
 */
public class CreativePlayerInv implements Serializable {
    private final Map<Integer, Integer> enchantments = new HashMap<Integer, Integer>();
    private static final long serialVersionUID = -2586926842675218375L;
    private final short durability;
    private final int typeId;
    private final int amount;

    public CreativePlayerInv(ItemStack item) {
        if (item == null) {
            this.typeId = -1;
            this.amount = 0;
            this.durability = 0;
        } else {
            this.typeId = item.getTypeId();
            this.amount = item.getAmount();
            this.durability = item.getDurability();
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                enchantments.put(entry.getKey().getId(), entry.getValue());
            }
        }
    }

    public ItemStack getItem() {
        if (typeId == -1) {
            return null;
        }
        ItemStack item = new ItemStack(typeId, amount, durability);
        if (enchantments == null) {
            return item;
        }
        for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = Enchantment.getById(entry.getKey());
            if (enchantment == null) {
                continue;
            }
            try {
                item.addEnchantment(enchantment, entry.getValue());
            } catch (IllegalArgumentException iae) {
                continue;
            }
        }
        return item;
    }
}
