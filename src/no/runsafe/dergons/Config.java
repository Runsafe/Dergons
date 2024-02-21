package no.runsafe.dergons;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.tools.nms.EntityRegister;
import no.runsafe.worldguardbridge.IRegionControl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config implements IConfigurationChanged, IPluginEnabled
{
	public Config(IRegionControl worldGuard)
	{
		Config.worldGuard = worldGuard;
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
		pickupDamage = config.getConfigValueAsFloat("pickupDamage");
		baseHealth = config.getConfigValueAsFloat("baseHealth");
		healAmount = config.getConfigValueAsFloat("healAmount");
		spawnChance = config.getConfigValueAsInt("spawnChance");

		dergonRepellentRadiusSquared = config.getConfigValueAsInt("antiDergonBubble.radius");
		dergonRepellentRadiusSquared *= dergonRepellentRadiusSquared;
		dergonRepellentLocation = config.getConfigValueAsLocation("antiDergonBubble.location");

		antiDergonRegions.putAll(config.getConfigSectionsAsList("antiDergonRegions"));

		Message.dergonPunchback = config.getConfigValueAsString("message.dergonPunchback");
		Message.dergonElytraPickup = config.getConfigValueAsString("message.dergonElytraPickup");
		Message.dergonKillPlayer = config.getConfigValueAsString("message.dergonKillPlayer");
		Message.inventoryFull = config.getConfigValueAsString("message.inventoryFull");

		worldNames.clear();
		worldNames.addAll(config.getConfigValueAsList("dergonWorlds"));
	}

	public static final class Message
	{
		public static String getDergonPunchback()
		{
			return dergonPunchback;
		}

		public static String getDergonElytraPickup()
		{
			return dergonElytraPickup;
		}

		public static String getDergonKillPlayer()
		{
			return dergonKillPlayer;
		}

		public static String getInventoryFull()
		{
			return inventoryFull;
		}

		private static String dergonPunchback;
		private static String dergonElytraPickup;
		private static String dergonKillPlayer;
		private static String inventoryFull;
	}

	public static float getVexChance()
	{
		return vexChance;
	}

	public static int getSpawnY()
	{
		return spawnY;
	}

	public static int getEventMinTime()
	{
		return eventMinTime;
	}

	public static int getEventMaxTime()
	{
		return eventMaxTime;
	}

	public static int getDespawnTime()
	{
		return despawnTime;
	}

	public static int getStepCount()
	{
		return stepCount;
	}

	public static int getMinSpawnY()
	{
		return minSpawnY;
	}

	public static float getBaseDamage()
	{
		return baseDamage;
	}

	public static float getPickupDamage()
	{
		return pickupDamage;
	}

	public static float getBaseHealth()
	{
		return baseHealth;
	}

	public static float getHealAmount()
	{
		return healAmount;
	}

	public static int getSpawnChance()
	{
		return spawnChance;
	}

	public static boolean isInvalidSpawnLocation(ILocation location)
	{
		if (location == null)
		{
			Dergons.Debugger.debugFine("Failing to spawn dergon due to null location.");
			return true;
		}

		IWorld world = location.getWorld();
		// Check if in anti dergon bubble.
		if(!(dergonRepellentRadiusSquared == 0 || dergonRepellentLocation == null
			|| !world.isWorld(dergonRepellentLocation.getWorld())
			|| !(dergonRepellentLocation.distanceSquared(location) < dergonRepellentRadiusSquared)))
		{
			Dergons.Debugger.debugFine("Failing to spawn dergon due it being inside the anti dergon bubble.");
			return true;
		}

		// Check if in a region it shouldn't spawn in.
		if(worldGuard.worldGuardIsMissing())
			return false;

		List<String> antiDergonRegionsInWorld = antiDergonRegions.get(world.getName());
		if (antiDergonRegionsInWorld.isEmpty())
			return false;

		List<String> insideRegions = worldGuard.getRegionsAtLocation(location);
		if (insideRegions == null || insideRegions.isEmpty())
			return false;

		for (String antiDergonRegionName : antiDergonRegionsInWorld)
			if (insideRegions.contains(antiDergonRegionName))
			{
				Dergons.Debugger.debugFine("Failing to spawn dergon due it being inside an anti dergon region.");
				return true;
			}

		return false;
	}

	public static boolean isDergonWorldListEmpty()
	{
		return worldNames.isEmpty();
	}

	public static boolean isNotDergonWorld(@Nullable IWorld world)
	{
		if (world == null) return true;

		return !worldNames.contains(world.getName());
	}

	@Override
	public void OnPluginEnabled()
	{
		EntityRegister.registerEntity(Dergon.class, "Dergon", 63);
	}

	private static float vexChance;
	private static int spawnY;
	private static int eventMinTime;
	private static int eventMaxTime;
	private static int despawnTime;
	private static int stepCount;
	private static int minSpawnY;
	private static float baseDamage;
	private static float pickupDamage;
	private static float baseHealth;
	private static float healAmount;
	private static int spawnChance;
	private static int dergonRepellentRadiusSquared;
	private static ILocation dergonRepellentLocation;
	private static final Map<String, List<String>> antiDergonRegions = new HashMap<>();
	private static final List<String> worldNames = new ArrayList<>(0);
	private static IRegionControl worldGuard;
}
