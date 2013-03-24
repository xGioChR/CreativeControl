package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bags.BlockBag;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class CreativeEditSessionFactory extends EditSessionFactory {

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, player);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, blockBag, player);
    }

    public static void setup() {
        WorldEdit.getInstance().setEditSessionFactory(new CreativeEditSessionFactory());
    }
}
