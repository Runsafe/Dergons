package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.entity.*;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.event.world.IChunkUnload;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class PlayerMonitor implements IItemSpawn, IPlayerInteractEntityEvent, IChunkUnload
{
	public PlayerMonitor(DergonHandler handler)
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
			if (entity instanceof ILivingEntity && ((ILivingEntity) entity).getCustomName().matches("§4Dergon:\\s[0-9]+"))
			{
				String dergonIDString = ((ILivingEntity) entity).getCustomName();
				handler.setDergonUnloaded(Integer.getInteger(dergonIDString.substring(10)));
			}

		return true;
	}

	private final DergonHandler handler;
}
