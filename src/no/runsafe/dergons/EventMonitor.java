package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.entity.*;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.event.world.IChunkLoad;
import no.runsafe.framework.api.event.world.IChunkUnload;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventMonitor implements IItemSpawn, IPlayerInteractEntityEvent, IChunkUnload, IChunkLoad
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
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		// Handle players collecting dragon's breath.
		IPlayer player = event.getPlayer();
		RunsafeMeta usingItem = player.getItemInMainHand();
		if (usingItem == null || !usingItem.is(Item.Brewing.GlassBottle))
			return;

		IEntity entity = event.getRightClicked();
		if (!(entity instanceof IAreaEffectCloud))
			return;

		Dergons.Debugger.debugFine("Player right clicking area effect cloud with a bottle: " + player.getName());

		// Check that fireball was launched something other than a Dergon
		IProjectileSource source = ((IAreaEffectCloud) entity).getSource();
		if (source instanceof IPlayer || source instanceof IBlockProjectileSource)
			return;

		float cloudSize = ((IAreaEffectCloud) entity).getRadius();
		if (cloudSize <= 0)
			return;

		RunsafeInventory inventory = player.getInventory();
		if (inventory.getContents().size() >= inventory.getSize())
		{
			player.sendColouredMessage("&cYour inventory is a bit too full to do that.");
			return;
		}

		inventory.removeExact(usingItem, 1);
		RunsafeMeta item = Item.Brewing.DragonsBreath.getItem();
		item.setAmount(1);
		inventory.addItems(item);
		player.updateInventory();

		((IAreaEffectCloud) entity).setRadius(cloudSize - ((IAreaEffectCloud) entity).getRadiusOnUse());
	}

	@Override
	public boolean OnChunkUnload(IChunk chunk)
	{
		// Check if player unloaded a chunk with a dergon in it so we can register it.
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

		List<Integer> reloadedDergons = new ArrayList<>();
		for (Map.Entry<Integer, IChunk> dergonChunkEntry : dergonChunks.entrySet())
		{
			IChunk dergonChunk = dergonChunkEntry.getValue();
			if (chunk.getX() != dergonChunk.getX() || chunk.getZ() != dergonChunk.getZ())
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

	private final DergonHandler handler;
	private static final HashMap<Integer, IChunk> dergonChunks = new HashMap<>(0);
}
