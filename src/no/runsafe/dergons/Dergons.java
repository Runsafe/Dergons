package no.runsafe.dergons;

import no.runsafe.dergons.commands.RemoveAll;
import no.runsafe.dergons.commands.StartEvent;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;

public class Dergons extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		// Framework features
		addComponent(Commands.class);
		addComponent(Events.class);

		// Plugin components
		addComponent(DergonHandler.class);

		addComponent(RemoveAll.class);
		addComponent(StartEvent.class);
	}
}
