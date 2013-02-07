package me.FurH.CreativeControl.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeItemMeta {
    
    public static String getItemMeta(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        
        int hasDisplayName = meta.hasDisplayName() ? 1 : 0;
        String displayName = CreativeUtil.encode(meta.getDisplayName());
        
        int hasLore = meta.hasLore() ? 1 : 0;
        
        String lore = "";
        for (String page : meta.getLore()) {
            lore += CreativeUtil.encode(page) + "!";
        }
        
        return ":"+hasDisplayName + ";" + displayName + ";" + hasLore + ";" + lore.substring(0, lore.length());
    }

    public static ItemStack setItemMeta(ItemStack stack, String string) {
        ItemMeta meta = stack.getItemMeta();
        
        try {
            String[] split = string.split(";");
            
            boolean hasDisplayName = "1".equals(split[0]);
            String displayName = CreativeUtil.decode(split[1]);
            
            if (hasDisplayName) {
                meta.setDisplayName(displayName);
            }

            boolean hasLore = "1".equals(split[2]);
            if (hasLore) {
                List<String> lore = new ArrayList<String>();
                for (String s : split[3].split("!")) {
                    lore.add(CreativeUtil.decode(s));
                }

                meta.setLore(lore);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }
}
