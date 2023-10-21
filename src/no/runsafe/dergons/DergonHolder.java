package no.runsafe.dergons;

import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.World;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;

import java.util.Random;

public class DergonHolder
{
	public DergonHolder(IScheduler scheduler, ILocation targetLocation, int min, int max, int steps, int minY, DergonHandler handler, int dergonID, float baseHealth)
	{
		this.scheduler = scheduler;
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

	private void spawn()
	{
		IPlayer idealPlayer = null;
		maxHealth = baseHealth;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation != null && playerLocation.distance(targetLocation) < 200)
			{
				if (idealPlayer == null && playerLocation.getY() > minY)
					idealPlayer = player;

				maxHealth += (baseHealth / 2);
			}
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

		heldDergon.dead = true;
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
		return heldDergon != null;
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

	private void processStep()
	{
		targetLocation.playSound(random.nextInt(2) == 1 ? Sound.Creature.EnderDragon.Growl : Sound.Creature.EnderDragon.Flap, 30, 1);

		if (currentStep == stepCount)
		{
			spawn();
			return;
		}

		scheduler.startSyncTask(this::processStep, random.nextInt(maxStep) + minStep);
		currentStep++;
	}

	public void setHeldDergon(Dergon newDergon, float damageDealt)
	{
		heldDergon = newDergon;
		heldDergon.setCustomName("Dergon");
		heldDergon.getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
		heldDergon.setHealth(maxHealth - damageDealt);
		heldDergon.setDergonID(dergonID);
	}

	private Dergon heldDergon;
	private int currentStep = 0;
	private float maxHealth = 0;
	private final IScheduler scheduler;
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
