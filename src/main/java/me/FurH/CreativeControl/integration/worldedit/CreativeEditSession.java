package me.FurH.CreativeControl.integration.worldedit;


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

@SuppressWarnings("deprecation")
public class CreativeEditSession  {

	public void init() {
		com.sk89q.worldedit.WorldEdit.getInstance().getEventBus().register(new Object() {
			@Subscribe(priority=Priority.VERY_LATE)
			public void wrapForLogging(final EditSessionEvent e) {
				if(e.getStage() != Stage.BEFORE_HISTORY)
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
							if (newType == 0) {
								manager.unprotect(world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);
								System.out.println("unprotecting block @" + pos);
							}

							if (newType != 0) {
								if (config.block_ownblock) {
									if (!actor.hasPermission("CreativeControl.OwnBlock.DontSave")) {
										System.out.println("OwnBlock registered " + org.bukkit.Material.getMaterial(newBlock.getType()) + " @ " + pos);
										manager.protect(actor.getName(), world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);
									}
								}

								if (config.block_nodrop) {
									if (!actor.hasPermission("CreativeControl.NoDrop.DontSave")) {
										System.out.println("NoDrop registered " + org.bukkit.Material.getMaterial(newBlock.getType()) + " @ " + pos);
										manager.protect(actor.getName(), world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newType);
									}
								}   
							}
						}

					}
				});
			}
		});
	}

	/*public void init() {
		com.sk89q.worldedit.WorldEdit.getInstance().getEventBus().register(new Object() {
			@Subscribe
			public void wrapForLogging(final EditSessionEvent event) {
				final Actor actor = event.getActor();
				if (actor == null || !(actor instanceof Player)) return;

				String worldName = event.getWorld().getName();
				final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
				if (bukkitWorld == null)
					return;

				final CreativeWorldNodes config = CreativeControl.getWorldNodes(bukkitWorld);
				final CreativeBlockManager manager = CreativeControl.getManager();

				event.setExtent(new AbstractLoggingExtent(event.getExtent()) {
					@Override
					protected void onBlockChange(Vector pt, BaseBlock block) {

						if (event.getStage() != EditSession.Stage.BEFORE_CHANGE) {
							return;
						}

						Location location = new Location(bukkitWorld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
						Block origin = location.getBlock();
						int oldType = origin.getTypeId();
						byte oldData = origin.getData();

						BlockState oldState = null;
						if (oldType == Material.SIGN_POST.getId() || oldType == Material.SIGN.getId()) {
							oldState = origin.getState();
						}

						Block block_ = ((BukkitWorld)actor.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

						BlockState block_state = block_.getState();
						ItemStack[] container_contents = getContainerContents(block_);

						boolean success;
						try {
							success = super.setBlock(pt, block);
						} catch (WorldEditException e) {
							throw new Error("Error on set block by worldedit", e);
						}

						if (success) {

							logBlock(actor, pt, block, oldType, oldData, oldState);
							prism(actor, pt, block, oldType, oldData);
							coreprotect(actor, pt, block, block_state, block_, container_contents);

							if (!config.world_exclude && config.block_worledit) {
								int newType = block.getType();

								if (newType == 0 || oldType != 0) {
									manager.unprotect(bukkitWorld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
								}

								if (newType != 0) {

									if (config.block_ownblock) {
										if (!actor.hasPermission("CreativeControl.OwnBlock.DontSave")) {
											manager.protect(actor.getName(), bukkitWorld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
										}
									}

									if (config.block_nodrop) {
										if (!actor.hasPermission("CreativeControl.NoDrop.DontSave")) {
											manager.protect(actor.getName(), bukkitWorld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
										}
									}   
								}
							}
						}
					}
				});
			}
		});
	}

	public void coreprotect(Actor actor, Vector vector, BaseBlock base_block, BlockState block_state, Block block, ItemStack[] container_contents) {

		CoreProtectAPI protect = CreativeControl.getCoreProtect();

		if (protect == null) {
			return;
		}

		if (Functions.checkConfig(block.getWorld(), "worldedit") == 0) {
			return;
		}

		try {
			Method log = WorldEdit.class.getDeclaredMethod("logData", LocalPlayer.class, Vector.class, Block.class, BlockState.class, ItemStack[].class);
			log.setAccessible(true);
			log.invoke(null, actor, vector, block, block_state, container_contents);
		} catch (Exception ex) {

			String methods = "";

			for (Method m : WorldEdit.class.getDeclaredMethods()) {
				methods += m.getName() + ", ";
			}

			System.out.println("[ " + ex.getClass().getSimpleName() + " ]: Failed to invoke logData method, Available: " + methods);
		}
	}

	private ItemStack[] getContainerContents(Block block) {
		CoreProtectAPI protect = CreativeControl.getCoreProtect();

		if (protect == null) {
			return null;
		}

		return Functions.getContainerContents(block);
	}

	public void prism(Actor actor, Vector pt, BaseBlock block, int typeBefore, byte dataBefore) {
		if (CreativeControl.getPrism()) {

			if (!Prism.config.getBoolean("prism.tracking.world-edit")) {
				return;
			}

			Location loc = new Location(Bukkit.getWorld(actor.getWorld().getName()), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
			RecordingQueue.addToQueue(ActionFactory.create("world-edit", loc, typeBefore, dataBefore, loc.getBlock().getTypeId(), loc.getBlock().getData(), actor.getName()));
		}
	}

	public void logBlock(Actor actor, Vector pt, BaseBlock block, int typeBefore, byte dataBefore, BlockState stateBefore) {
		Consumer consumer = CreativeControl.getLogBlock();

		if (consumer != null) {

			if (!(Config.isLogging(actor.getWorld().getName(), Logging.WORLDEDIT))) {
				return;
			}

			Location location = new Location(((BukkitWorld) actor.getWorld()).getWorld(), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

			if (Config.isLogging(location.getWorld().getName(), Logging.SIGNTEXT) && (typeBefore == Material.SIGN_POST.getId() || typeBefore == Material.SIGN.getId())) {
				consumer.queueSignBreak(actor.getName(), (Sign) stateBefore);
				if (block.getType() != Material.AIR.getId()) {
					consumer.queueBlockPlace(actor.getName(), location, block.getType(), (byte) block.getData());
				}
			} else {
				if (dataBefore != 0) {
					consumer.queueBlockBreak(actor.getName(), location, typeBefore, dataBefore);
					if (block.getType() != Material.AIR.getId()) {
						consumer.queueBlockPlace(actor.getName(), location, block.getType(), (byte) block.getData());
					}
				} else {
					consumer.queueBlock(actor.getName(), location, typeBefore, block.getType(), (byte) block.getData());
				}
			}
		}
	}*/
}
