package no.runsafe.dergons;

import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.World;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class DergonHolder
{
	public DergonHolder(ILocation spawnLocation, int min, int max, int steps, int minY, DergonHandler handler, int dergonID, float baseHealth)
	{
		this.spawnLocation = spawnLocation;
		this.handler = handler;

		world = spawnLocation.getWorld();

		this.minStep = min;
		this.maxStep = max;
		this.stepCount = steps;
		this.baseHealth = baseHealth;

		this.minY = minY;

		this.dergonID = dergonID;

		if (world != null)
			processStep();
	}

	private void attemptSpawn()
	{
		ILocation dergonRepellentLocation = handler.getDergonRepellentLocation();
		int dergonRepellentRadius = handler.getDergonRepellentRadius();
		dergonRepellentRadius *= dergonRepellentRadius;

		// Check if trying to spawn in anti-dergon bubble
		if (dergonRepellentRadius != 0 && dergonRepellentLocation != null
			&& world.isWorld(dergonRepellentLocation.getWorld())
			&& dergonRepellentLocation.distanceSquared(spawnLocation) < dergonRepellentRadius)
		{
			handler.removeDergon(dergonID);
			return;
		}

		IPlayer idealPlayer = null;
		maxHealth = baseHealth;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation == null || playerLocation.distanceSquared(spawnLocation) > 40000) // 200 blocks
				continue;

			if (dergonRepellentRadius != 0 && dergonRepellentLocation != null
				&& playerLocation.getWorld().isWorld(dergonRepellentLocation.getWorld())
				&& dergonRepellentLocation.distanceSquared(playerLocation) < dergonRepellentRadius)
				continue;

			if (idealPlayer == null && playerLocation.getY() > minY)
				idealPlayer = player;

			maxHealth += (baseHealth / 2);
		}

		if (idealPlayer != null)
			if (spawn(spawnLocation, maxHealth))
				return;

		handler.removeDergon(dergonID); // no dergon was spawned, remove it from list.
	}

	private boolean spawn(ILocation setLocation, float health)
	{
		World rawWorld = ObjectUnwrapper.getMinecraft(world);
		if (rawWorld != null)
		{
			heldDergon = new Dergon(world, handler, spawnLocation, dergonID);
			heldDergon.setPosition(setLocation.getX(), setLocation.getY(), setLocation.getZ());
			heldDergon.setCustomName("Dergon");
			heldDergon.getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
			heldDergon.setHealth(health);
			rawWorld.addEntity(heldDergon);
			return true;
		}
		return false;
	}

	boolean kill()
	{
		if (heldDergon == null)
			return false;

		heldDergon.setHealth(0);
		heldDergon.die();
		handler.handleDergonDeath(heldDergon, true);
		return true;
	}

	public ILocation getLocation()
	{
		if (heldDergon == null)
			return null;
		return heldDergon.getLocation();
	}

	public ILocation getUnloadLocation()
	{
		return unloadLocation;
	}

	public Boolean isHoldingDergon()
	{
		return (heldDergon != null && !heldDergon.isNoAI());
	}

	public IPlayer getCurrentTarget()
	{
		if (heldDergon == null)
			return null;
		return heldDergon.getCurrentTarget();
	}

	public ILocation getTargetFlyToLocation()
	{
		if (heldDergon == null)
			return null;
		return heldDergon.getTargetFlyToLocation();
	}

	public ILocation getSpawnLocation()
	{
		if (heldDergon == null)
			return null;
		return heldDergon.getSpawnLocation();
	}

	public float getHealth()
	{
		return heldDergon.getHealth();
	}

	public float getMaxHealth()
	{
		if (!isUnloaded)
			return heldDergon.getMaxHealth();
		else return unloadedHealth;
	}

	public void setUnloaded()
	{
		unloadLocation = heldDergon.getLocation();
		unloadedHealth = heldDergon.getHealth();
		isUnloaded = true;
		heldDergon.setHealth(0);
		heldDergon.die();
		heldDergon = null;
	}

	public boolean isUnloaded()
	{
		return isUnloaded;
	}

	private void processStep()
	{
		spawnLocation.playSound(random.nextInt(2) == 1 ? Sound.Creature.EnderDragon.Growl : Sound.Creature.EnderDragon.Flap, 30, 1);

		if (currentStep == stepCount)
		{
			attemptSpawn();
			return;
		}

		Dergons.scheduler.startSyncTask(this::processStep, random.nextInt(maxStep) + minStep);
		currentStep++;
	}

	public void reloadDergon()
	{
		spawn(unloadLocation, unloadedHealth);
	}

	@Nullable
	public UUID getDergonUniqueID()
	{
		if (heldDergon == null)
			return null;
		return heldDergon.getUniqueID();
	}

	private Dergon heldDergon;
	private boolean isUnloaded = false;
	private int currentStep = 0;
	private float maxHealth = 0;
	private float unloadedHealth = 0;
	private final ILocation spawnLocation;
	private ILocation unloadLocation;
	private final IWorld world;
	private final int minStep;
	private final int maxStep;
	private final int minY;
	private final int stepCount;
	private final float baseHealth;
	private final Random random = new Random();
	private final DergonHandler handler;
	private final int dergonID;
}
