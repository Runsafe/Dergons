package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.EntityTypes;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.entity.IEntityDeathEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDeathEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.lang.reflect.Method;
import java.util.*;

public class DergonHandler implements IPluginDisabled, IConfigurationChanged, IEntityDeathEvent
{
	public DergonHandler(IScheduler scheduler, IConsole console, IServer server)
	{
		this.scheduler = scheduler;
		this.console = console;
		this.server = server;
	}

	public List<Dergon> getDergons()
	{
		return dergons;
	}

	public Dergon spawnDergon(ILocation location)
	{
		if (!registered)
		{
			try
			{
				Method a = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
				a.setAccessible(true);
				a.invoke(a, CustomDergonEntity.class, "Dergon", 392);
				registered = true;
			}
			catch (Exception ignored)
			{
				console.logException(ignored);
			}
		}

		IWorld world = location.getWorld();
		if (world == null)
			return null;

		location.offset(0, spawnY, 0); // Set the location to be high in the sky.

		Dergon dergon = new Dergon(scheduler, location, eventMinTime, eventMaxTime, stepCount, minSpawnY); // Construct the dergon.
		dergons.add(dergon); // Track the dergon.
		return dergon;
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

	@Override
	public void OnEntityDeath(RunsafeEntityDeathEvent event)
	{
		RunsafeEntity entity = event.getEntity();

		// Check we have a dragon.
		if (entity.getEntityType() == LivingEntity.EnderDragon)
		{
			for (Dergon dergon : dergons)
			{
				if (dergon.isDergon(entity))
				{
					List<RunsafeMeta> drops = new ArrayList<RunsafeMeta>(2);

					RunsafeMeta egg = Item.Special.DragonEgg.getItem();
					egg.setDisplayName("Dergon Egg");
					egg.addLore("§3A heavy egg that seems to hum with unnatural energy.");
					egg.setAmount(1);

					RunsafeMeta bones = Item.Miscellaneous.Bone.getItem();
					bones.setAmount(random.nextInt(4) + 5); // 4 - 9 bones.
					bones.setDisplayName("Dergon Bones");
					bones.addLore("§3Impressive and heavy bones from the corpse of a Dergon.");

					drops.add(egg);
					drops.add(bones);
					event.setDrops(drops);

					dergon.powerDown();

					IPlayer slayer = null;
					double slayerDamage = 0D;

					HashMap<String, Double> damageDone = dergon.getDamageDone();
					for (Map.Entry<String, Double> node : damageDone.entrySet())
					{
						IPlayer player = server.getPlayerExact(node.getKey());
						new DergonAssistEvent(player).Fire();

						double damage = node.getValue();
						if (damage > slayerDamage)
						{
							slayer = player;
							slayerDamage = damage;
						}
					}

					if (slayer != null)
						new DergonSlayEvent(slayer).Fire();

					break;
				}
			}
		}
	}

	private final IScheduler scheduler;
	private final IConsole console;
	private final List<Dergon> dergons = new ArrayList<Dergon>(0);
	private final IServer server;
	private int spawnY;
	private int eventMinTime;
	private int eventMaxTime;
	private int stepCount;
	private int minSpawnY;
	private final Random random = new Random();
	private boolean registered = false;
}
