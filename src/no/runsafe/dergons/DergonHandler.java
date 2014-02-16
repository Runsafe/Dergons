package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityDamageSourceIndirect;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.tools.nms.EntityRegister;

import java.util.HashMap;
import java.util.Random;

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

	public float handleDergonDamage(DamageSource source, float damage)
	{
		if (source.p().equalsIgnoreCase("arrow"))
			damage = 6.0F;

		Entity attackingEntity = source.getEntity();
		if (attackingEntity != null && attackingEntity instanceof EntityPlayer)
		{
			String playerName = attackingEntity.getName();
			if (!damageCounter.containsKey(playerName))
				damageCounter.put(playerName, damage);
			else
				damageCounter.put(playerName, damageCounter.get(playerName) + damage);

			console.logInformation("Current damage for " + playerName + " = " + damageCounter.get(playerName));
		}

		return damage;
	}

	public void handleDergonDeath(Dergon dergon)
	{

	}

	private final IScheduler scheduler;
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
	private HashMap<String, Float> damageCounter = new HashMap<String, Float>(0);
	private final IConsole console;
	private final Random random = new Random();
}
