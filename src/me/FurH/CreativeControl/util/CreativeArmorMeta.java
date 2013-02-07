package me.FurH.CreativeControl.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeArmorMeta {
    
    public static String getArmorMeta(ItemStack stack) {
        return ":"+CreativeFireworkMeta.getColor(((LeatherArmorMeta) stack.getItemMeta()).getColor());
    }

    public static ItemStack setArmorMeta(ItemStack stack, String string) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        
        try {
            meta.setColor(CreativeFireworkMeta.getColor(Integer.parseInt(string)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }
}
