package no.runsafe.dergons;

import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;

import java.util.Random;

public class DergonHolder
{
	public DergonHolder(IScheduler scheduler, ILocation targetLocation, int min, int max, int steps, int minY, DergonHandler handler, int dergonID)
	{
		this.scheduler = scheduler;
		this.targetLocation = targetLocation;
		this.handler = handler;

		world = targetLocation.getWorld();

		this.minStep = min;
		this.maxStep = max;
		this.stepCount = steps;

		this.minY = minY;

		this.dergonID = dergonID;

		if (world != null)
			processStep();
	}

	private void spawn()
	{
		IPlayer idealPlayer = null;
		float health = 200F;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation != null && playerLocation.distance(targetLocation) < 200)
			{
				if (idealPlayer == null && playerLocation.getY() > minY)
					idealPlayer = player;

				health += 100F;
			}
		}

		if (idealPlayer != null)
		{
			World rawWorld = ObjectUnwrapper.getMinecraft(world);
			if (rawWorld != null)
			{
				Dergon dragon = new Dergon(world, handler, targetLocation, dergonID);
				dragon.setPosition(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
				dragon.setCustomName("Dergon");
				dragon.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
				dragon.setHealth(health);
				rawWorld.addEntity(dragon);
			}
		}
	}

	private void processStep()
	{
		targetLocation.playSound(random.nextInt(2) == 1 ? Sound.Creature.EnderDragon.Growl : Sound.Creature.EnderDragon.Flap, 30, 1);

		if (currentStep == stepCount)
		{
			spawn();
			return;
		}

		scheduler.startSyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				processStep();
			}
		}, random.nextInt(maxStep) + minStep);
		currentStep++;
	}

	private int currentStep = 0;
	private final IScheduler scheduler;
	private final ILocation targetLocation;
	private final IWorld world;
	private final int minStep;
	private final int maxStep;
	private final int minY;
	private final int stepCount;
	private final Random random = new Random();
	private final DergonHandler handler;
	private final int dergonID;
}
