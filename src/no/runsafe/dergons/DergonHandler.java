package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.DamageSource;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.tools.nms.EntityRegister;

public class DergonHandler implements IConfigurationChanged, IPluginEnabled
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
		new DergonHolder(scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY, this); // Construct the dergon.
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

	@Override
	public void OnPluginEnabled()
	{
		EntityRegister.registerEntity(Dergon.class, "Dergon", 63);
	}

	public void handleDergonTick(Dergon dergon)
	{

	}

	public float handleDergonDamage(DamageSource source, float damage)
	{
		console.logInformation("Taking " + damage + " damage from " + source.p());
		return damage;
	}

	private final IScheduler scheduler;
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
	private IConsole console;
}
