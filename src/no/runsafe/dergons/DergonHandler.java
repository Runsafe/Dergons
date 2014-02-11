package no.runsafe.dergons;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;

import java.util.ArrayList;
import java.util.List;

public class DergonHandler implements IPluginDisabled, IConfigurationChanged
{
	public DergonHandler(IScheduler scheduler, IConsole console)
	{
		this.scheduler = scheduler;
		this.console = console;
	}

	public void spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null)
			return;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.

		Dergon dergon = new Dergon(scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY); // Construct the dergon.
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

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		spawnY = config.getConfigValueAsInt("spawnY");
		eventMinTime = config.getConfigValueAsInt("eventMinTime");
		eventMaxTime = config.getConfigValueAsInt("eventMaxTime");
		stepCount = config.getConfigValueAsInt("eventSteps");
		minSpawnY = config.getConfigValueAsInt("spawnMinY");
	}

	private final IScheduler scheduler;
	private final IConsole console;
	private final List<Dergon> dergons = new ArrayList<Dergon>(0);
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
}
