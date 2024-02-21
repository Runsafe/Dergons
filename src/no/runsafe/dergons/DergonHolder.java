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
	public DergonHolder(ILocation spawnLocation, DergonHandler handler, int dergonID)
	{
		this.spawnLocation = spawnLocation;
		this.handler = handler;

		world = spawnLocation.getWorld();

		this.dergonID = dergonID;

		if (world != null)
			processStep();
	}

	private void attemptSpawn()
	{
		// Check if trying to spawn in anti-dergon bubble
		if (Config.isInvalidSpawnLocation(spawnLocation))
		{
			handler.removeDergon(dergonID);
			return;
		}

		IPlayer idealPlayer = null;
		float baseHealth = Config.getBaseHealth();
		maxHealth = baseHealth;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation == null || playerLocation.distanceSquared(spawnLocation) > 40000) // 200 blocks
				continue;

			if (idealPlayer == null && playerLocation.getY() > Config.getMinSpawnY())
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

	public void heal(float healAmount)
	{
		heldDergon.heal(healAmount);
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
		if (!isUnloaded && heldDergon != null)
			return heldDergon.getHealth();
		else return unloadedHealth;
	}

	public float getMaxHealth()
	{
		return maxHealth;
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

		if (currentStep == Config.getStepCount())
		{
			attemptSpawn();
			return;
		}

		Dergons.scheduler.startSyncTask(this::processStep, random.nextInt(Config.getEventMaxTime()) + Config.getEventMinTime());
		currentStep++;
	}

	public void reloadDergon()
	{
		isUnloaded = false;
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
	private float maxHealth = -1;
	private float unloadedHealth = -1;
	private final ILocation spawnLocation;
	private ILocation unloadLocation;
	private final IWorld world;
	private final Random random = new Random();
	private final DergonHandler handler;
	private final int dergonID;
}
