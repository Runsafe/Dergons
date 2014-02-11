package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class SpawnDergon extends PlayerCommand
{
	public SpawnDergon(DergonHandler handler)
	{
		super("spawn", "Spawn a dergon", "runsafe.dergons.spawn", new OnlinePlayerRequired());
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getPlayer("player");
		if (targetPlayer != null && targetPlayer.isOnline())
		{
			handler.spawnDergon(targetPlayer.getLocation(), targetPlayer);
			return "&eDergon spawned.";
		}
		return "&cInvalid player.";
	}

	private final DergonHandler handler;
}
