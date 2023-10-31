package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.entity.IAreaEffectCloud;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class PlayerMonitor implements IItemSpawn, IPlayerInteractEntityEvent
{
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
		RunsafeMeta item = Item.Brewing.DragonsBreath;
		item.setAmount(1);
		inventory.addItems(item);
		player.updateInventory();

		((IAreaEffectCloud) entity).setRadius(cloudSize - ((IAreaEffectCloud) entity).getRadiusOnUse());
	}
}
