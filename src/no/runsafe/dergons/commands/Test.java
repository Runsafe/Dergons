package no.runsafe.dergons.commands;

import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class Test extends PlayerCommand
{
	public Test()
	{
		super("test", "Test command", "runsafe.test");
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		return "Test";
	}
}
