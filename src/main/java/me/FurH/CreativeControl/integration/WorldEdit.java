package me.FurH.CreativeControl.integration;

import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.configuration.CreativeWorldNodes;
import me.FurH.CreativeControl.manager.CreativeBlockManager;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;

public class WorldEdit {

	public void init() {
		com.sk89q.worldedit.WorldEdit.getInstance().getEventBus().register(new Object() {
			@Subscribe(priority = Priority.NORMAL)
			public void wrapForLogging(final EditSessionEvent e) {
				if (e.getStage() != Stage.BEFORE_CHANGE)
					return;

				final Actor actor = e.getActor();

				e.setExtent(new AbstractLoggingExtent(e.getExtent()) {
					@Override
					protected void onBlockChange(Vector pos, BaseBlock newBlock) {

						World world = Bukkit.getWorld(e.getWorld().getName());
						CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
						CreativeBlockManager manager = CreativeControl.getManager();

						if (!config.world_exclude && config.block_worledit) {
							int newType = newBlock.getType();
							if (newType == 0)
								manager.unprotect(world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);
							//System.out.println("unprotecting block @" + pos);

							if (newType != 0) {
								if (config.block_ownblock)
									if (!actor.hasPermission("CreativeControl.OwnBlock.DontSave"))
										//System.out.println("OwnBlock registered " + org.bukkit.Material.getMaterial(newBlock.getType()) + " @ " + pos);
										manager.protect(actor.getName(), world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);

								if (config.block_nodrop)
									if (!actor.hasPermission("CreativeControl.NoDrop.DontSave"))
										//System.out.println("NoDrop registered " + org.bukkit.Material.getMaterial(newBlock.getType()) + " @ " + pos);
										manager.protect(actor.getName(), world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);
							}
						}

					}
				});
			}
		});
	}
}
