package no.runsafe.dergons.event;

import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class DergonMountEvent extends RunsafeCustomEvent
{
	public DergonMountEvent(IPlayer player)
	{
		super(player, "runsafe.dergon.mount");
	}

	@Override
	public Object getData()
	{
		return null;
	}
}
