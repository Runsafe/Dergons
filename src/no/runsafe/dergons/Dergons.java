package no.runsafe.dergons;

import no.runsafe.dergons.commands.SpawnDergon;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.features.Commands;

public class Dergons extends RunsafeConfigurablePlugin
{
	public static IDebug Debugger = null;

	@Override
	protected void pluginSetup()
	{
		Debugger = getComponent(IDebug.class);

		// Framework features
		addComponent(Commands.class);

		// Plugin components
		addComponent(DergonHandler.class);
		addComponent(DergonSpawner.class);
		addComponent(PlayerMonitor.class);

		Command dergonCommand = new Command("dergons", "Dergon related commands", null);
		addComponent(dergonCommand);

		dergonCommand.addSubCommand(getInstance(SpawnDergon.class));
	}
}
