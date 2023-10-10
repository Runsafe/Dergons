package no.runsafe.dergons;

import net.minecraft.server.v1_12_R1.*;
import no.runsafe.dergons.event.*;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.tools.nms.EntityRegister;

import java.util.*;

public class DergonHandler implements IConfigurationChanged, IPluginEnabled
{
	public DergonHandler(IScheduler scheduler, IConsole console, IServer server)
	{
		this.scheduler = scheduler;
		this.console = console;
		this.server = server;
	}

	public int spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null)
			return -1;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.
		activeDergons.put( // Construct the dergon.
			currentDergonID,
			new DergonHolder(console, scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY, this, currentDergonID, baseHealth)
		);
		return currentDergonID++;
	}

	public String killDergon(int ID)
	{
		DergonHolder victim = activeDergons.get(ID);

		if (victim == null)
			return "&cDergon could not be killed, invalid ID.";

		boolean success = victim.kill();
		if (success)
			return "&aDergon killed.";

		damageCounter.remove(ID);
		activeDergons.remove(ID);
		return "&cDergon entity does not exist, removing from list.";
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		spawnY = config.getConfigValueAsInt("spawnY");
		eventMinTime = config.getConfigValueAsInt("eventMinTime");
		eventMaxTime = config.getConfigValueAsInt("eventMaxTime");
		stepCount = config.getConfigValueAsInt("eventSteps");
		minSpawnY = config.getConfigValueAsInt("spawnMinY");
		baseDamage = config.getConfigValueAsFloat("baseDamage");
		baseHealth = config.getConfigValueAsFloat("baseHealth");
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

	public float getDergonAttackingDamage()
	{
		return baseDamage;
	}

	public void handleDergonDeath(Dergon dergon)
	{
		IWorld world = dergon.getDergonWorld();
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
		activeDergons.remove(dergonID);

		if (slayer != null)
			new DergonSlayEvent(slayer).Fire();
	}

	public void handleDergonMount(String playerName)
	{
		new DergonMountEvent(server.getPlayerExact(playerName)).Fire();
	}

	public Set<Integer> getAllDergonIDs()
	{
		return activeDergons.keySet();
	}

	public List<String> getAllDergonInfo()
	{
		List<String> info = new ArrayList<>();
		for (Integer id : activeDergons.keySet())
		{
			DergonHolder dergon = activeDergons.get(id);
			ILocation dergonLocation = dergon.getLocation();
			IPlayer target = dergon.getCurrentTarget();
			info.add(
				"&eID:&r " + id +
				", &eTarget:&r " + ((target == null) ? "&cN/A&r" : target.getPrettyName())   +
				", &eLocation:&r" + ((dergonLocation == null) ? "&cN/A&r" : dergonLocation.toString())
			);
		}
		return info;
	}

	private final IScheduler scheduler;
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
	private float baseDamage;
	private float baseHealth;
	private HashMap<Integer, HashMap<String, Float>> damageCounter = new HashMap<Integer, HashMap<String, Float>>(0);
	private HashMap<Integer, DergonHolder> activeDergons = new HashMap<>(0);
	private final IConsole console;
	private final IServer server;
	private final Random random = new Random();
	private int currentDergonID = 1;
}
