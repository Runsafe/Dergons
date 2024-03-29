package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.dergons.Dergons;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class SpawnDergon extends PlayerCommand
{
	public SpawnDergon(DergonHandler handler)
	{
		super("spawn", "Spawn a dergon", "runsafe.dergons.spawn", new Player().onlineOnly().require());
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getValue("player");
		if (targetPlayer != null && targetPlayer.isOnline())
		{
			int dergonID = handler.spawnDergon(targetPlayer.getLocation());
			Dergons.console.logInformation("Manually spawning dergon with ID: " + dergonID);
			return "&aDergon spawned with ID:" + dergonID;
		}
		return "&cInvalid player.";
	}

	private final DergonHandler handler;
}
