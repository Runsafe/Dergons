package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class SpawnDergon extends PlayerCommand
{
	public SpawnDergon(DergonHandler handler)
	{
		super("spawn", "Spawn a dergon", "runsafe.dergons.spawn");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.spawnDergon(executor.getLocation());
		return "&eDergon spawned.";
	}

	private final DergonHandler handler;
}
