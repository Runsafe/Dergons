package no.runsafe.dergons;

import no.runsafe.dergons.commands.KillDergon;
import no.runsafe.dergons.commands.ListDergons;
import no.runsafe.dergons.commands.SpawnDergon;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;

public class Dergons extends RunsafeConfigurablePlugin
{
	public static IDebug Debugger = null;
	public static IServer server;

	@Override
	protected void pluginSetup()
	{
		Debugger = getComponent(IDebug.class);
		server = getComponent(IServer.class);

		// Framework features
		addComponent(Commands.class);
		addComponent(Events.class);

		// Plugin components
		addComponent(DergonHandler.class);
		addComponent(DergonSpawner.class);
		addComponent(PlayerMonitor.class);

		Command dergonCommand = new Command("dergons", "Dergon related commands", null);
		addComponent(dergonCommand);

		dergonCommand.addSubCommand(getInstance(SpawnDergon.class));
		dergonCommand.addSubCommand(getInstance(KillDergon.class));
		dergonCommand.addSubCommand(getInstance(ListDergons.class));
	}
}
