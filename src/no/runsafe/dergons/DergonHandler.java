package no.runsafe.dergons;

import net.minecraft.server.v1_12_R1.*;
import no.runsafe.dergons.event.*;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.bossBar.*;
import no.runsafe.framework.tools.nms.EntityRegister;

import java.util.*;

import static java.lang.Math.round;

public class DergonHandler implements IConfigurationChanged, IPluginEnabled
{
	public DergonHandler(IScheduler scheduler)
	{
		this.scheduler = scheduler;
	}

	public DergonHandler()
	{
		this(null);
	}

	public int spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null || scheduler == null)
			return -1;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.
		activeDergons.put( // Construct the dergon.
			currentDergonID,
			new DergonHolder(scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY, this, currentDergonID, baseHealth)
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
		if (attackingEntity instanceof EntityPlayer)
		{
			IPlayer attackingPlayer = Dergons.server.getPlayer(attackingEntity.getUniqueID());

			if (source instanceof EntityDamageSourceIndirect && source.i() != null && source.i() instanceof EntitySnowball)
				new DergonSnowballEvent(attackingPlayer).Fire();

			int dergonID = dergon.getDergonID();

			if (dergonID < 0)
				return damage;

			if (!damageCounter.containsKey(dergonID))
				damageCounter.put(dergonID, new HashMap<>(0));

			if (!damageCounter.get(dergonID).containsKey(attackingPlayer))
				damageCounter.get(dergonID).put(attackingPlayer, damage);
			else
				damageCounter.get(dergonID).put(attackingPlayer, damageCounter.get(dergonID).get(attackingPlayer) + damage);
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
		if (random.nextInt(5) == 1)
			world.dropItem(location, DergonItems.getDergonHead(1));

		IPlayer slayer = null;
		float slayerDamage = 0F;

		int dergonID = dergon.getDergonID();

		if (damageCounter.containsKey(dergonID))
		{
			for (Map.Entry<IPlayer, Float> node : damageCounter.get(dergonID).entrySet())
			{
				IPlayer player = node.getKey();
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
		removeBossBar(dergonID);

		if (slayer != null)
			new DergonSlayEvent(slayer).Fire();
	}

	public void handleDergonMount(IPlayer player)
	{
		new DergonMountEvent(player).Fire();
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
			ILocation targetDestination = dergon.getTargetFlyToLocation();
			IPlayer target = dergon.getCurrentTarget();
			info.add(
				"&eID: &r " + id +
				", &eTarget: &r " + ((target == null) ? "&cN/A&r" : target.getPrettyName()) +
				", &eLocation: &r" + ((dergonLocation == null) ? "&cN/A&r" : dergonLocation.toString()) +
				", &eIntendedDestination: &r" + ((targetDestination == null) ? "&cN/A&r" : targetDestination.toString())
			);
		}
		return info;
	}

	public void createBossBar(int dergonID)
	{
		if (dergonID < 0) return;

		dergonBossBars.put(dergonID, new RunsafeBossBar("Dergon", BarColour.PURPLE, BarStyle.SOLID));
	}

	public void updateBossBar(int dergonID, float currentHealth, float maxHealth, List<IPlayer> newBarPlayers)
	{
		if (dergonID < 0) return;

		IBossBar bossBar = dergonBossBars.get(dergonID);

		if (bossBar == null) return;

		// Update the health bar to show the percentage of the dergon
		long pct = round((currentHealth / maxHealth));
		bossBar.setTitle("Dergon (" + (pct * 100) + "%)");
		bossBar.setProgress(pct);

		// Handle which players can see the boss bar
		bossBar.setActivePlayers(newBarPlayers);
	}

	public void removeBossBar(int dergonID)
	{
		if (dergonID < 0 || dergonBossBars.get(dergonID) == null) return;

		dergonBossBars.get(dergonID).removeAllPlayers();
		dergonBossBars.remove(dergonID);
	}

	private final IScheduler scheduler;
	private static int spawnY;
	private static int eventMinTime;
	private static int eventMaxTime;
	private static int stepCount;
	private static int minSpawnY;
	private static float baseDamage;
	private static float baseHealth;
	private static final HashMap<Integer, HashMap<IPlayer, Float>> damageCounter = new HashMap<>(0);
	private static final HashMap<Integer, DergonHolder> activeDergons = new HashMap<>(0);
	private static final HashMap<Integer, IBossBar> dergonBossBars = new HashMap<>(0);
	private static final Random random = new Random();
	private static int currentDergonID = 1;
}
