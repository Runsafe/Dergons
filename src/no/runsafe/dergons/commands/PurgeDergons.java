package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class PurgeDergons extends PlayerCommand
{
	public PurgeDergons(DergonHandler handler)
	{
		super("purge", "Purge all the dergons!", "runsafe.dergons.purge");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.removeAllDergons();
		return "&eAll dergons have been purged.";
	}

	private final DergonHandler handler;
}
