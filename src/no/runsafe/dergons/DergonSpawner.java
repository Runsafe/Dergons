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
		if (Config.isDergonWorldListEmpty())
			return "Failed, no worlds.";

		List<IPlayer> selectedPlayers = new ArrayList<>(0);
		for (IPlayer player : Dergons.server.getOnlinePlayers())
		{
			if (player == null || !Config.isDergonWorld(player.getWorld()))
				continue;

			ILocation playerLocation = player.getLocation();
			if (playerLocation == null) // Make sure we have a valid location.
				continue;

			double playerY = playerLocation.getY(); // The player's Y position.
			if (playerY < Config.getMinSpawnY()) // Check the player is above the minimum spawn point.
				continue;

			if (player.getWorld().getHighestBlockYAt(playerLocation) > playerY) // Check nothing is blocking the sky.
				continue;

			// Check if we're in the anti dergon bubble
			ILocation antiDergonBubbleLocation = Config.getDergonRepellentLocation();
			if (Config.getDergonRepellentRadiusSquared() != 0 && antiDergonBubbleLocation != null
					&& playerLocation.getWorld().isWorld(antiDergonBubbleLocation.getWorld())
					&& antiDergonBubbleLocation.distanceSquared(playerLocation) < Config.getDergonRepellentRadiusSquared())
				continue;

			if (random.nextInt(100) <= Config.getSpawnChance() + ((playerY - Config.getMinSpawnY()) * 0.5))
				selectedPlayers.add(player);
		}

		if (selectedPlayers.isEmpty())
			return "Failed, no valid/lucky players.";

		IPlayer selectedPlayer = selectedPlayers.get(random.nextInt(selectedPlayers.size()));
		return "Spawning @ " + selectedPlayer.getName() + " with ID: " + handler.spawnDergon(selectedPlayer.getLocation());
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
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
	private final Random random = new Random();
}
