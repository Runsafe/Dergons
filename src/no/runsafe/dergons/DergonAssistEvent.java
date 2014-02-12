package no.runsafe.dergons;

import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class DergonAssistEvent extends RunsafeCustomEvent
{
	public DergonAssistEvent(IPlayer player)
	{
		super(player, "runsafe.dergon.kill");
	}

	@Override
	public Object getData()
	{
		return null;
	}
}
