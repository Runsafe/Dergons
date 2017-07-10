package no.runsafe.dergons.event;

import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class DergonSlayEvent extends RunsafeCustomEvent
{
	public DergonSlayEvent(IPlayer player)
	{
		super(player, "runsafe.dergon.slay");
	}

	@Override
	public Object getData()
	{
		return null;
	}
}
