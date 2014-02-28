package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class PlayerMonitor implements IItemSpawn
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
	}
}
