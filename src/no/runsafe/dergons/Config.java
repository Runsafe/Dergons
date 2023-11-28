package no.runsafe.dergons;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.tools.nms.EntityRegister;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Config implements IConfigurationChanged, IPluginEnabled
{
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
		spawnChance = config.getConfigValueAsInt("spawnChance");

		dergonRepellentRadiusSquared = config.getConfigValueAsInt("antiDergonBubble.radius");
		dergonRepellentRadiusSquared *= dergonRepellentRadiusSquared;
		dergonRepellentLocation = config.getConfigValueAsLocation("antiDergonBubble.location");

		worldNames.clear();
		worldNames.addAll(config.getConfigValueAsList("dergonWorlds"));
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

	public static float getBaseHealth()
	{
		return baseHealth;
	}

	public static int getSpawnChance()
	{
		return spawnChance;
	}

	public static boolean isValidSpawnLocation(ILocation location)
	{
		if (location == null)
			return false;

		return dergonRepellentRadiusSquared == 0 || dergonRepellentLocation == null
			|| !location.getWorld().isWorld(dergonRepellentLocation.getWorld())
			|| !(dergonRepellentLocation.distanceSquared(location) < dergonRepellentRadiusSquared);
	}

	public static boolean isDergonWorldListEmpty()
	{
		return worldNames.isEmpty();
	}

	public static boolean isDergonWorld(@Nullable IWorld world)
	{
		if (world == null) return false;

		return worldNames.contains(world.getName());
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
	private static float baseHealth;
	private static int spawnChance;
	private static int dergonRepellentRadiusSquared;
	private static ILocation dergonRepellentLocation;
	private static final List<String> worldNames = new ArrayList<>(0);
}
