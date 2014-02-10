package no.runsafe.dergons;

import no.runsafe.dergons.commands.Test;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;
import no.runsafe.framework.features.FrameworkHooks;

public class Dergons extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		// Framework features
		addComponent(Commands.class);
		addComponent(Events.class);
		addComponent(FrameworkHooks.class);

		// Plugin components
		addComponent(DergonHandler.class);

		addComponent(Test.class);
	}
}
