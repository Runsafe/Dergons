package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.event.entity.IItemSpawn;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeItemSpawnEvent;

public class PlayerMonitor implements IItemSpawn
{
	@Override
	public void OnItemSpawn(RunsafeItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().is(Item.Special.DragonEgg))
		{
			ILocation location = event.getLocation();
			location.getWorld().dropItem(location, DergonItems.getEgg(1));
			event.cancel();
			Dergons.Debugger.debugInfo("Dropping dergon egg!");
		}
	}
}
