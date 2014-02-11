package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;

import java.util.ArrayList;
import java.util.List;

public class DergonHandler implements IPluginDisabled
{
	public DergonHandler(IConsole console)
	{
		this.console = console;
	}

	public void spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null)
			return;

		Dergon dergon = new Dergon(); // Construct the dergon.
		dergon.spawn(location); // Spawn the dergon.
		dergons.add(dergon); // Track the dergon.
	}

	public void removeAllDergons()
	{
		// Loop all dergons and remove them.
		for (Dergon dergon : dergons)
			dergon.remove();

		dergons.clear(); // Clear the tracking list.
	}

	@Override
	public void OnPluginDisabled()
	{
		console.logWarning("Server shut-down detected, purging all dergons.");
		removeAllDergons();
	}

	private final IConsole console;
	private final List<Dergon> dergons = new ArrayList<Dergon>(0);
}
