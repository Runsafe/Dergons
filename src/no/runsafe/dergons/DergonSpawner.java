package no.runsafe.dergons;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DergonSpawner implements IConfigurationChanged
{
	public DergonSpawner(DergonHandler handler)
	{
		this.handler = handler;
	}

	private String attemptSpawn()
	{
		if (worldNames.isEmpty())
			return "Failed, no worlds.";

		List<IPlayer> selectedPlayers = new ArrayList<IPlayer>(0);
		for (IPlayer player : Dergons.server.getOnlinePlayers())
		{
			if (player != null && isDergonWorld(player.getWorld()))
			{
				ILocation playerLocation = player.getLocation();
				if (playerLocation == null) // Make sure we have a valid location.
					continue;

				double playerY = playerLocation.getY(); // The player's Y position.
				if (playerY < minSpawnY) // Check the player is above the minimum spawn point.
					continue;

				if (player.getWorld().getHighestBlockYAt(playerLocation) > playerY) // Check nothing is blocking the sky.
					continue;

				if (random.nextInt(100) <= spawnChance + ((playerY - minSpawnY) * 0.5))
					selectedPlayers.add(player);
			}
		}

		if (selectedPlayers.isEmpty())
			return "Failed, no valid/lucky players.";

		IPlayer selectedPlayer = selectedPlayers.get(random.nextInt(selectedPlayers.size()));
		return "Spawning @ " + selectedPlayer.getName() + " with ID: " + handler.spawnDergon(selectedPlayer.getLocation());
	}

	public boolean isDergonWorld(IWorld world)
	{
		return worldNames.contains(world.getName());
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		spawnChance = config.getConfigValueAsInt("spawnChance");
		minSpawnY = config.getConfigValueAsInt("spawnMinY");

		worldNames.clear();
		worldNames.addAll(config.getConfigValueAsList("dergonWorlds"));

		if (timerID > -1)
			Dergons.scheduler.cancelTask(timerID);

		int spawnTimer = config.getConfigValueAsInt("spawnTimer");
		timerID = Dergons.scheduler.startSyncRepeatingTask(() ->
			Dergons.console.logInformation("Attempting to spawn a dergon: " + attemptSpawn()),
			spawnTimer, spawnTimer
		);
	}

	private final DergonHandler handler;
	private int timerID;
	private int spawnChance;
	private int minSpawnY;
	private final List<String> worldNames = new ArrayList<>(0);
	private final Random random = new Random();
}
