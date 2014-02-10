package no.runsafe.dergons;

import no.runsafe.dergons.commands.PurgeDergons;
import no.runsafe.dergons.commands.SpawnDergon;
import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;

public class Dergons extends RunsafePlugin
{
	@Override
	protected void pluginSetup()
	{
		// Framework features
		addComponent(Commands.class);
		addComponent(Events.class);

		// Plugin components
		addComponent(DergonHandler.class);

		Command dergonCommand = new Command("dergons", "Dergon related commands", null);
		addComponent(dergonCommand);

		dergonCommand.addSubCommand(getInstance(PurgeDergons.class));
		dergonCommand.addSubCommand(getInstance(SpawnDergon.class));
	}
}
