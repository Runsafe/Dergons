package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.IBlockPhysics;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.block.RunsafeBlockPhysicsEvent;

public class PlayerMonitor implements IBlockPhysics
{
	@Override
	public void OnBlockPhysics(RunsafeBlockPhysicsEvent event)
	{
		IBlock block = event.getBlock();
		if (block.is(Item.Special.DragonEgg))
		{
			ILocation location = block.getLocation();
			location.getWorld().dropItem(location, DergonItems.getEgg(1));
			event.cancel();
		}
	}
}
