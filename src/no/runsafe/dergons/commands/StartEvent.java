package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class StartEvent extends PlayerCommand
{
	public StartEvent(DergonHandler handler)
	{
		super("spawndergon", "Start a dergon event", "runsafe.dergons.start", new OnlinePlayerRequired());
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getPlayer("player");
		if (targetPlayer != null && targetPlayer.isOnline())
		{
			handler.spawnDergon(targetPlayer.getLocation());
			return "&aDergon spawned.";
		}
		return "&cUnable to spawn dergon.";
	}

	private final DergonHandler handler;
}
