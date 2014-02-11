package no.runsafe.dergons;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Random;

public class DergonSpawner implements IConfigurationChanged
{
	public DergonSpawner(IScheduler scheduler, IConsole console, DergonHandler handler)
	{
		this.scheduler = scheduler;
		this.console = console;
		this.handler = handler;
	}

	private String attemptSpawn()
	{
		if (world == null)
			return "Failed, invalid world.";

		IPlayer selectedPlayer = null;
		for (IPlayer player : world.getPlayers())
		{
			if (player != null && player.isOnline())
			{
				ILocation playerLocation = player.getLocation();
				if (playerLocation == null) // Make sure we have a valid location.
					continue;

				double playerY = playerLocation.getY(); // The player's Y position.
				if (playerY < minSpawnY) // Check the player is above the minimum spawn point.
					continue;

				if (world.getHighestBlockYAt(playerLocation) > playerY) // Check nothing is blocking the sky.
					continue;

				if (random.nextInt(100) <= spawnChance + ((playerY - minSpawnY) * 0.5))
				{
					selectedPlayer = player;
					break;
				}
			}
		}

		if (selectedPlayer == null)
			return "Failed, no valid/lucky players.";

		handler.spawnDergon(selectedPlayer.getLocation());
		return "Spawning @ " + selectedPlayer.getName();
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		spawnChance = config.getConfigValueAsInt("spawnChance");
		spawnTimer = config.getConfigValueAsInt("spawnTimer");
		world = config.getConfigValueAsWorld("dergonWorld");
		minSpawnY = config.getConfigValueAsInt("spawnMinY");

		if (timerID > -1)
			scheduler.cancelTask(timerID);

		timerID = scheduler.startSyncRepeatingTask(new Runnable()
		{
			@Override
			public void run()
			{
				console.logInformation("Attempting to spawn a dergon: " + attemptSpawn());
			}
		}, spawnTimer, spawnTimer);
	}

	private final IScheduler scheduler;
	private final IConsole console;
	private final DergonHandler handler;
	private int timerID;
	private int spawnChance;
	private int spawnTimer;
	private int minSpawnY;
	private IWorld world;
	private final Random random = new Random();
}
