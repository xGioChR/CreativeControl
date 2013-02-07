package me.FurH.CreativeControl.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeBookMeta {
    
    public static String getBookMeta(ItemStack stack) {
        BookMeta meta = (BookMeta) stack.getItemMeta();
        
        int hasTitle = meta.hasTitle() ? 1 : 0;
        String title = CreativeUtil.encode(meta.getTitle());
        
        int hasAuthor = meta.hasAuthor() ? 1 : 0;
        String author = CreativeUtil.encode(meta.getAuthor());
        
        int hasPages = meta.hasPages() ? 1 : 0;
        
        String pages = "";
        for (String page : meta.getPages()) {
            pages += CreativeUtil.encode(page) + "!";
        }
        
        return ":"+hasTitle + ";" + title + ";" + hasAuthor + ";" + author + ";" + hasPages + ";" + pages.substring(0, pages.length());
    }

    public static ItemStack setBookMeta(ItemStack stack, String string) {
        BookMeta meta = (BookMeta) stack.getItemMeta();
        
        try {
            String[] split = string.split(";");
            
            boolean hasTitle = "1".equals(split[0]);
            String title = CreativeUtil.decode(split[1]);
            
            if (hasTitle) {
                meta.setTitle(title);
            }
            
            boolean hasAuthor = "1".equals(split[2]);
            String author = CreativeUtil.decode(split[3]);
            
            if (hasAuthor) {
                meta.setAuthor(author);
            }
            
            boolean hasPages = "1".equals(split[4]);
            if (hasPages) {
                List<String> pages = new ArrayList<String>();
                for (String s : split[5].split("!")) {
                    pages.add(CreativeUtil.decode(s));
                }

                meta.setPages(pages);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }
}
