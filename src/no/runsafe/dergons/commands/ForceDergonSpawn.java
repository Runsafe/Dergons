package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class ForceDergonSpawn extends PlayerCommand
{
	public ForceDergonSpawn(DergonHandler handler)
	{
		super("force", "Force an instant dergon spawn for debugging", "runsafe.dergons.force");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.spawnDergon(executor.getLocation()).spawnEntity();
		return "&eDone.";
	}

	private final DergonHandler handler;
}
