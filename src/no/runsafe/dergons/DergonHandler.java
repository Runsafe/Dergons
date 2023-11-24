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
	public DergonHandler()
	{
		Dergons.scheduler.startSyncRepeatingTask(this::BossBarPlayersInRangeCycle, 2, 2);
	}

	private void BossBarPlayersInRangeCycle()
	{
		if (dergonBossBars.isEmpty())
			return;

		for (Map.Entry<Integer, IBossBar> bossBarEntry : dergonBossBars.entrySet())
		{
			int dergonID = bossBarEntry.getKey();
			IBossBar bossBar = bossBarEntry.getValue();
			DergonHolder dergonHolder = activeDergons.get(dergonID);
			if (!dergonHolder.isHoldingDergon())
			{
				bossBar.removeAllPlayers();
				continue;
			}
			bossBar.setActivePlayers(dergonHolder.getLocation().getPlayersInRange(150));
		}
	}

	public void reloadDergon(int dergonID)
	{
		DergonHolder holder = activeDergons.get(dergonID);
		if (holder == null)
			return;

		holder.reloadDergon();
	}

	public int spawnDergon(ILocation location)
	{
		if (location == null || location.getWorld() == null)
			return -1;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.
		activeDergons.put( // Construct the dergon.
			currentDergonID,
			new DergonHolder(location, eventMinTime, eventMaxTime, stepCount, minSpawnY, this, currentDergonID, baseHealth)
		);
		return currentDergonID++;
	}

	public String killDergon(int ID)
	{
		DergonHolder victim = activeDergons.get(ID);

		if (victim == null)
			return "&cDergon could not be killed, invalid ID.";

		if (victim.isUnloaded())
		{
			EventMonitor.removeDergonFromList(ID);
			removeDergon(ID);
			Dergons.console.logInformation("Silently killing unloaded dergon with ID: " + ID);
			return "&aUnloaded dergon killed.";
		}

		Dergons.console.logInformation("Silently killing dergon with ID: " + ID);
		boolean success = victim.kill();
		if (success)
			return "&aDergon killed.";

		removeDergon(ID);
		return "&cDergon entity does not exist, removing from list.";
	}

	public void removeDergon(int ID)
	{
		damageCounter.remove(ID);
		activeDergons.remove(ID);
		removeBossBar(ID);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		vexChance = config.getConfigValueAsFloat("vexChance");
		spawnY = config.getConfigValueAsInt("spawnY");
		eventMinTime = config.getConfigValueAsInt("eventMinTime");
		eventMaxTime = config.getConfigValueAsInt("eventMaxTime");
		despawnTime = config.getConfigValueAsInt("despawnTimer");
		stepCount = config.getConfigValueAsInt("eventSteps");
		minSpawnY = config.getConfigValueAsInt("spawnMinY");
		baseDamage = config.getConfigValueAsFloat("baseDamage");
		baseHealth = config.getConfigValueAsFloat("baseHealth");

		dergonRepellentRadius = config.getConfigValueAsInt("antiDergonBubble.radius");
		dergonRepellentLocation = config.getConfigValueAsLocation("antiDergonBubble.location");
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

	public void handleDergonDeath(Dergon dergon, boolean quickKill)
	{
		int dergonID = dergon.getDergonID();
		if (quickKill)
		{
			removeDergon(dergonID);
			return;
		}

		IWorld world = dergon.getDergonWorld();
		ILocation location = world.getLocation(dergon.locX, dergon.locY, dergon.locZ);

		world.dropItem(location, DergonItems.getEgg(1));
		world.dropItem(location, DergonItems.getBones(random.nextInt(4) + 5));
		if (random.nextInt(5) == 1)
			world.dropItem(location, DergonItems.getDergonHead(1));

		IPlayer slayer = null;
		float slayerDamage = 0F;

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
		}
		removeDergon(dergonID); // Remove the tracking for this dergon.

		if (slayer != null)
			new DergonSlayEvent(slayer).Fire();
	}

	public void handleDergonMount(IPlayer player)
	{
		new DergonMountEvent(player).Fire();
	}

	public List<String> getAllDergonInfo()
	{
		List<String> info = new ArrayList<>();
		for (Integer id : activeDergons.keySet())
		{
			DergonHolder dergon = activeDergons.get(id);
			boolean isUnloaded = dergon.isUnloaded();
			boolean isNull = !dergon.isHoldingDergon();

			if (!isNull && !isUnloaded)
			{
				ILocation spawnLocation = dergon.getSpawnLocation();
				ILocation dergonLocation = dergon.getLocation();
				ILocation targetDestination = dergon.getTargetFlyToLocation();
				IPlayer target = dergon.getCurrentTarget();
				info.add(
					"&5ID: &r " + id +
					", &9Health: &r (" + dergon.getHealth() + "/" + dergon.getMaxHealth() + ")" +
					", \n&9Target: &r " + target.getPrettyName() +
					", \n&9SpawnLocation: &r" + locationInfo(spawnLocation) +
					", \n&9Location: &r" + locationInfo(dergonLocation) +
					", \n&9Destination: &r" + locationInfo(targetDestination)
				);
			}
			else if (isUnloaded)
			{
				ILocation unloadLocation = dergon.getUnloadLocation();
				info.add(
					"&5ID: &r " + id +
					", &9Health: &r (" + dergon.getHealth() + "/" + dergon.getMaxHealth() + "), &cUnloaded Dergon&r" +
					"\n&9UnloadLocation: &r" + locationInfo(unloadLocation)
				);
			}
			else info.add("&5ID: &r " + id +", &4Null Dergon.");
		}
		return info;
	}

	private String locationInfo(ILocation location)
	{
		if (location == null)
			return "&cN/A&r";

		return String.format(
			"&eWorld:&r %s &eX:&r %.0f &eY:&r %.0f &eZ:&r %.0f",
			location.getWorld().getName(), location.getX(), location.getY(), location.getZ()
		);
	}

	public void createBossBar(int dergonID)
	{
		if (dergonID < 0) return;
		if (!dergonBossBars.containsKey(dergonID))
			dergonBossBars.put(dergonID, new RunsafeBossBar("Dergon", BarColour.PURPLE, BarStyle.SOLID));
	}

	public void updateBossBarHealth(int dergonID, float currentHealth, float maxHealth)
	{
		if (dergonID < 0) return;

		IBossBar bossBar = dergonBossBars.get(dergonID);

		if (bossBar == null) return;

		// Update the health bar to show the percentage of the dergon
		double pct = (currentHealth / maxHealth);
		bossBar.setTitle("Dergon (" + round(pct * 100) + "%)" + (showBarIDs ? " ID: " + dergonID : ""));
		bossBar.setProgress(pct);
	}

	public void setShowBossBarID(boolean showBarID)
	{
		showBarIDs = showBarID;
	}

	public void removeBossBar(int dergonID)
	{
		if (dergonID < 0 || dergonBossBars.get(dergonID) == null) return;

		dergonBossBars.get(dergonID).removeAllPlayers();
		dergonBossBars.remove(dergonID);
	}

	public int unloadIfDergon(UUID id)
	{
		for (Map.Entry<Integer, DergonHolder> dergonHolderEntry : activeDergons.entrySet())
		{
			DergonHolder holder = dergonHolderEntry.getValue();
			if (id == holder.getDergonUniqueID())
			{
				Dergons.Debugger.debugInfo("Unloading dergon with ID: " + id);
				holder.setUnloaded();
				return dergonHolderEntry.getKey();
			}
		}
		return -1;
	}

	public float getVexChance()
	{
		return vexChance;
	}

	public int getDespawnTime()
	{
		return despawnTime;
	}

	public int getDergonRepellentRadius()
	{
		return dergonRepellentRadius;
	}

	public ILocation getDergonRepellentLocation()
	{
		return dergonRepellentLocation;
	}

	private static boolean showBarIDs = false;
	private static float vexChance;
	private static int spawnY;
	private static int eventMinTime;
	private static int eventMaxTime;
	private static int despawnTime;
	private static int stepCount;
	private static int minSpawnY;
	private static float baseDamage;
	private static float baseHealth;
	private static int dergonRepellentRadius;
	private static ILocation dergonRepellentLocation;
	private static final HashMap<Integer, HashMap<IPlayer, Float>> damageCounter = new HashMap<>(0);
	private static final HashMap<Integer, DergonHolder> activeDergons = new HashMap<>(0);
	private static final HashMap<Integer, IBossBar> dergonBossBars = new HashMap<>(0);
	private static final Random random = new Random();
	private static int currentDergonID = 1;
}
