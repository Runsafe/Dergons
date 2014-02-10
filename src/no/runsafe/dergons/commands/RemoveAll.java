package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class RemoveAll extends PlayerCommand
{
	public RemoveAll(DergonHandler handler)
	{
		super("remove", "Remove all dergons", "runsafe.dergons.remove");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.removeAllDergons();
		return "&aDergons removed.";
	}

	private final DergonHandler handler;
}
