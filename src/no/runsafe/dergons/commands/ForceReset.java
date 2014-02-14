package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class ForceReset extends PlayerCommand
{
	public ForceReset(DergonHandler handler)
	{
		super("forcereset", "Force a dergon to reset", "runsafe.dergons.reset", new RequiredArgument("id"));
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.resetDergon(Integer.parseInt(parameters.get("id")));
		return "&eReset attempting..";
	}

	private final DergonHandler handler;
}
