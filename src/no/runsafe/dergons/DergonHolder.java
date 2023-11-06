package no.runsafe.dergons;

import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.World;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;

import java.util.Random;
import java.util.UUID;

public class DergonHolder
{
	public DergonHolder(ILocation targetLocation, int min, int max, int steps, int minY, DergonHandler handler, int dergonID, float baseHealth)
	{
		this.targetLocation = targetLocation;
		this.handler = handler;

		world = targetLocation.getWorld();

		this.minStep = min;
		this.maxStep = max;
		this.stepCount = steps;
		this.baseHealth = baseHealth;

		this.minY = minY;

		this.dergonID = dergonID;

		if (world != null)
			processStep();
	}

	public DergonHolder(Dergon newDergon, DergonHandler handler, int dergonID, float health)
	{
		this.targetLocation = null;
		this.handler = handler;
		this.heldDergon = newDergon;
		this.world = heldDergon.getDergonWorld();

		this.minStep = 0;
		this.maxStep = 0;
		this.stepCount = 0;
		this.minY = 0;
		this.baseHealth = health;
		this.maxHealth = health;
		this.dergonID = dergonID;

		heldDergon.setCustomName("§4Dergon: " + dergonID);
		heldDergon.getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
		heldDergon.setHealth(maxHealth);
		heldDergon.setDergonID(dergonID);
	}

	private void spawn()
	{
		ILocation dergonRepellentLocation = handler.getDergonRepellentLocation();
		int dergonRepellentRadius = handler.getDergonRepellentRadius();
		dergonRepellentRadius *= dergonRepellentRadius;

		// Check if trying to spawn in anti-dergon bubble
		if (dergonRepellentRadius != 0 && dergonRepellentLocation != null
			&& world.isWorld(dergonRepellentLocation.getWorld())
			&& dergonRepellentLocation.distanceSquared(targetLocation) < dergonRepellentRadius)
		{
			handler.removeDergon(dergonID);
			return;
		}

		IPlayer idealPlayer = null;
		maxHealth = baseHealth;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation == null || playerLocation.distanceSquared(targetLocation) > 40000) // 200 blocks
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
		{
			World rawWorld = ObjectUnwrapper.getMinecraft(world);
			if (rawWorld != null)
			{
				heldDergon = new Dergon(world, handler, targetLocation, dergonID);
				heldDergon.setPosition(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
				heldDergon.setCustomName("Dergon");
				heldDergon.getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
				heldDergon.setHealth(maxHealth);
				rawWorld.addEntity(heldDergon);
				return;
			}
		}
		handler.removeDergon(dergonID); // no dergon was spawned, remove it from list.
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
		return world.getLocation(heldDergon.locX, heldDergon.locY, heldDergon.locZ);
	}

	public IWorld getWorld()
	{
		return world;
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

	public float getHealth()
	{
		return heldDergon.getHealth();
	}

	public float getMaxHealth()
	{
		return heldDergon.getMaxHealth();
	}

	public void setUnloaded()
	{
		isUnloaded = true;
	}

	public boolean isUnloaded()
	{
		return isUnloaded;
	}

	private void processStep()
	{
		targetLocation.playSound(random.nextInt(2) == 1 ? Sound.Creature.EnderDragon.Growl : Sound.Creature.EnderDragon.Flap, 30, 1);

		if (currentStep == stepCount)
		{
			spawn();
			return;
		}

		Dergons.scheduler.startSyncTask(this::processStep, random.nextInt(maxStep) + minStep);
		currentStep++;
	}

	public void reloadDergon(Dergon newDergon)
	{
		if (heldDergon == null)
			return;

		newDergon.setCustomName("§4Dergon: " + dergonID);
		newDergon.getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
		newDergon.setHealth(heldDergon.getHealth());
		newDergon.setTargetLocation(heldDergon.getTargetLocation());
		newDergon.setDergonID(dergonID);

		heldDergon = newDergon;
		isUnloaded = false;
	}

	public UUID getDergonUniqueID()
	{
		return heldDergon.getUniqueID();
	}

	private Dergon heldDergon;
	private boolean isUnloaded = false;
	private int currentStep = 0;
	private float maxHealth = 0;
	private final ILocation targetLocation;
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
