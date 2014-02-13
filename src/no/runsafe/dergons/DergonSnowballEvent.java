package no.runsafe.dergons;

import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class DergonSnowballEvent extends RunsafeCustomEvent
{
	public DergonSnowballEvent(IPlayer player)
	{
		super(player, "runsafe.dergons.snowball");
	}

	@Override
	public Object getData()
	{
		return null;
	}
}
