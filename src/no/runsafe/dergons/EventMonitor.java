package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.entity.*;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClick;
import no.runsafe.framework.api.event.world.IChunkLoad;
import no.runsafe.framework.api.event.world.IChunkUnload;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventMonitor implements IItemSpawn, IPlayerRightClick, IChunkUnload, IChunkLoad, IPlayerDeathEvent
{
	public EventMonitor(DergonHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void OnItemSpawn(RunsafeItemSpawnEvent event)
	{
		RunsafeMeta item = event.getEntity().getItemStack();
		if (item.is(Item.Special.DragonEgg) && !DergonItems.isEgg(item))
		{
			ILocation location = event.getLocation();
			location.getWorld().dropItem(location, DergonItems.getEgg(1));
			event.cancel();
		}
		else if (item.is(Item.Decoration.Head.Dragon) && !DergonItems.isDergonHead(item))
		{
			ILocation location = event.getLocation();
			location.getWorld().dropItem(location, DergonItems.getDergonHead(1));
			event.cancel();
		}
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		// Handle players collecting dragon's breath.
		if (usingItem == null || !usingItem.is(Item.Brewing.GlassBottle) || player.getWorld() == null)
			return true;

		// If there aren't any active dergons there probably isn't a cloud nearby
		if (handler.getActiveDergons().isEmpty())
			return true;

		// Check if a cloud is near the player
		IAreaEffectCloud cloud = null;
		List<IEntity> entities = player.getWorld().getEntities();
		for (IEntity entity : entities)
		{
			// Check if entity is a cloud
			if (!(entity instanceof IAreaEffectCloud))
				continue;

			// Make sure cloud is near the player
			if (player.getLocation().distance(entity.getLocation()) > 5)
				continue;

			Dergons.Debugger.debugFine("Player %s right clicking near area effect cloud with a bottle", player.getName());

			// Check if fireball was launched something other than a Dergon
			IProjectileSource source = ((IAreaEffectCloud) entity).getSource();
			Dergons.Debugger.debugFine("Area of effect source: " + source);
			if (source != null)
				continue;

			// Make sure cloud isn't empty
			if (((IAreaEffectCloud) entity).getRadius() <= 0)
				continue;

			cloud = (IAreaEffectCloud) entity;
			break;
		}

		if (cloud == null)
			return true;

		float cloudRadius = cloud.getRadius();
		Dergons.Debugger.debugFine("Area Effect selected with radius: " + cloudRadius);
		RunsafeInventory inventory = player.getInventory();
		if (inventory.getContents().size() >= inventory.getSize())
		{
			player.sendColouredMessage("&cYour inventory is a bit too full to do that.");
			return true;
		}

		inventory.removeExact(usingItem, 1);
		RunsafeMeta item = Item.Brewing.DragonsBreath.getItem();
		item.setAmount(1);
		inventory.addItems(item);
		player.updateInventory();

		if (cloudRadius < 0.25F)
			cloud.setRadius(0);
		else
			cloud.setRadius(cloudRadius - 0.25F);
		Dergons.Debugger.debugFine("Area Effect new radius: " + cloud.getRadius());
		return false;
	}

	@Override
	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
	{
		if (handler.getActiveDergons().isEmpty())
			return;

		IEntity killer = event.getEntity().getKiller();
		if (killer == null)
		{
			Dergons.Debugger.debugFine("Player %s killed by a null entity.", event.getEntity().getName());
			return;
		}

		Dergons.Debugger.debugFine("Player %s killed by an entity with UUID: " + killer.getUniqueId(), event.getEntity().getName());
		int dergonID = handler.healIfDergon(killer.getUniqueId());
		if (dergonID <= 0)
			return;

		event.getEntity().sendColouredMessage("&4The Dergon steals away a bit of your essence.");
	}

	@Override
	public boolean OnChunkUnload(IChunk chunk)
	{
		// Check if player unloaded a chunk with a dergon in it so we can register it.
		if (handler.getActiveDergons().isEmpty())
			return true;

		if (!Config.isDergonWorld(chunk.getWorld()))
			return true;

		for (IEntity entity : chunk.getEntities())
			if (entity instanceof ILivingEntity)
			{
				int dergonID = handler.unloadIfDergon(entity.getUniqueId());
				if (dergonID <= 0)
					continue;

				dergonChunks.put(dergonID, chunk);
				Dergons.Debugger.debugInfo("Unloading dergon with ID: " + dergonID +
					" at *chunk coordinates* X: " + chunk.getX() + " Z: " + chunk.getZ()
				);
			}

		return true;
	}

	@Override
	public void OnChunkLoad(IChunk chunk)
	{
		// Check if re-loading a chunk that had a dergon in it, if so spawn it back in.
		if (dergonChunks.isEmpty())
			return;

		if (!Config.isDergonWorld(chunk.getWorld()))
			return;

		List<Integer> reloadedDergons = new ArrayList<>();
		for (Map.Entry<Integer, IChunk> dergonChunkEntry : dergonChunks.entrySet())
		{
			IChunk dergonChunk = dergonChunkEntry.getValue();
			if (chunk.getX() != dergonChunk.getX()
				|| chunk.getZ() != dergonChunk.getZ()
				|| !chunk.getWorld().isWorld(dergonChunk.getWorld())
			)
				continue;

			int dergonID = dergonChunkEntry.getKey();
			handler.reloadDergon(dergonID);
			reloadedDergons.add(dergonID);
			Dergons.Debugger.debugInfo("Reloading dergon with ID: " + dergonID +
				" at *chunk coordinates* X: " + chunk.getX() + " Z: " + chunk.getZ()
			);
		}

		// Remove loaded dergons from list.
		if (!reloadedDergons.isEmpty())
			for (int dergonID : reloadedDergons)
				dergonChunks.remove(dergonID);
	}

	public static void removeDergonFromList(int id)
	{
		dergonChunks.remove(id);
	}

	private final DergonHandler handler;
	private static final HashMap<Integer, IChunk> dergonChunks = new HashMap<>(0);
}
