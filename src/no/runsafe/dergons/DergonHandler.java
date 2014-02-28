package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.*;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.tools.nms.EntityRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DergonHandler implements IConfigurationChanged, IPluginEnabled
{
	public DergonHandler(IScheduler scheduler, IServer server)
	{
		this.scheduler = scheduler;
		this.server = server;
	}

	public void spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null)
			return;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.
		new DergonHolder(scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY, this, currentDergonID); // Construct the dergon.
		currentDergonID++;
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

	public float handleDergonDamage(Dergon dergon, DamageSource source, float damage)
	{
		if (source.p().equalsIgnoreCase("arrow"))
			damage = 6.0F;

		Entity attackingEntity = source.getEntity();
		if (attackingEntity != null && attackingEntity instanceof EntityPlayer)
		{
			String playerName = attackingEntity.getName();

			if (source instanceof EntityDamageSourceIndirect && source.i() != null && source.i() instanceof EntitySnowball)
				new DergonSnowballEvent(server.getPlayerExact(playerName)).Fire();

			int dergonID = dergon.getDergonID();

			if (!damageCounter.containsKey(dergonID))
				damageCounter.put(dergonID, new HashMap<String, Float>(0));

			if (!damageCounter.get(dergonID).containsKey(playerName))
				damageCounter.get(dergonID).put(playerName, damage);
			else
				damageCounter.get(dergonID).put(playerName, damageCounter.get(dergonID).get(playerName) + damage);
		}

		return damage;
	}

	public void handleDergonDeath(Dergon dergon)
	{
		IWorld world = dergon.getWorld();
		ILocation location = world.getLocation(dergon.locX, dergon.locY, dergon.locZ);

		world.dropItem(location, DergonItems.getEgg(1));
		world.dropItem(location, DergonItems.getBones(random.nextInt(4) + 5));

		IPlayer slayer = null;
		float slayerDamage = 0F;

		int dergonID = dergon.getDergonID();

		if (damageCounter.containsKey(dergonID))
		{
			for (Map.Entry<String, Float> node : damageCounter.get(dergonID).entrySet())
			{
				IPlayer player = server.getPlayerExact(node.getKey());
				new DergonAssistEvent(player).Fire();

				float damage = node.getValue();
				if (damage > slayerDamage)
				{
					slayer = player;
					slayerDamage = damage;
				}
			}
			damageCounter.remove(dergonID); // Remove the tracking for this dergon.
		}

		if (slayer != null)
			new DergonSlayEvent(slayer).Fire();
	}

	public void handleDergonMount(String playerName)
	{
		new DergonMountEvent(server.getPlayerExact(playerName)).Fire();
	}

	private final IScheduler scheduler;
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
	private HashMap<Integer, HashMap<String, Float>> damageCounter = new HashMap<Integer, HashMap<String, Float>>(0);
	private final IServer server;
	private final Random random = new Random();
	private int currentDergonID = 1;
}
