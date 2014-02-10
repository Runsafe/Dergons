package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DergonHandler implements IServerReady
{
	public DergonHandler(IConsole console, IServer server)
	{
		this.console = console;
		this.server = server;
	}

	public void spawnDergon(ILocation location)
	{
		IWorld world = location.getWorld();
		if (world == null)
			return;

		ILivingEntity entity = (ILivingEntity) LivingEntity.EnderDragon.spawn(location);
		entity.setCustomName("Dergon");

		String worldName = world.getName();
		if (!tracking.containsKey(worldName))
			tracking.put(worldName, new ArrayList<Integer>(1));

		tracking.get(worldName).add(entity.getEntityId());
	}

	public void removeAllDergons()
	{
		for (Map.Entry<String, List<Integer>> node : tracking.entrySet())
		{
			IWorld world = server.getWorld(node.getKey());
			if (world != null)
			{
				for (Integer entityID : node.getValue())
				{
					IEntity entity = world.getEntityById(entityID);
					if (entity != null)
						entity.remove(); // De-spawn.
				}
			}
		}
	}

	@Override
	public void OnServerReady()
	{
		console.logError("DERGONNNSSS");
	}

	private final IConsole console;
	private final IServer server;
	private final ConcurrentHashMap<String, List<Integer>> tracking = new ConcurrentHashMap<String, List<Integer>>(0);
}
