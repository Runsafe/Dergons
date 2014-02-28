package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.IBlockBreak;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;

public class PlayerMonitor implements IBlockBreak
{
	@Override
	public boolean OnBlockBreak(IPlayer player, IBlock block)
	{
		if (block.is(Item.Special.DragonEgg))
		{
			ILocation location = block.getLocation();
			location.getWorld().dropItem(location, DergonItems.getEgg(1));
			return false;
		}
		return true;
	}
}
