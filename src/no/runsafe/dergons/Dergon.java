package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.entity.IEnderDragon;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;

import java.util.Random;

public class Dergon
{
	public Dergon(IScheduler scheduler, ILocation targetLocation, int min, int max, int steps, int minY)
	{
		this.scheduler = scheduler;
		this.targetLocation = targetLocation;

		this.minStep = min;
		this.maxStep = max;
		this.stepCount = steps;

		this.minY = minY;

		processStep();
	}

	private void spawn()
	{
		IWorld world = targetLocation.getWorld();
		if (world != null)
		{
			for (IPlayer player : world.getPlayers())
			{
				ILocation playerLocation = player.getLocation();
				if (playerLocation != null && playerLocation.distance(targetLocation) < 200 && playerLocation.getY() > minY)
				{
					entity = (IEnderDragon) LivingEntity.EnderDragon.spawn(targetLocation);
					entity.setCustomName("Dergon");
					entity.setDragonTarget(player);
					fireballTimer = scheduler.startSyncRepeatingTask(new Runnable()
					{
						@Override
						public void run()
						{
							shootFireball();
						}
					}, 1, 1);
					return;
				}
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

		stepTimer = scheduler.startSyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				processStep();
			}
		}, random.nextInt(maxStep) + minStep);
		currentStep++;
	}

	public void remove()
	{
		entity.remove();
		if (stepTimer > -1)
			scheduler.cancelTask(stepTimer);

		if (fireballTimer > -1)
			scheduler.cancelTask(fireballTimer);
	}

	public void shootFireball()
	{
		entity.Fire(ProjectileEntity.Fireball); // Shoot a fireball.
	}

	private int currentStep = 0;
	private int stepTimer;
	private IEnderDragon entity;
	private final IScheduler scheduler;
	private final ILocation targetLocation;
	private final int minStep;
	private final int maxStep;
	private final int minY;
	private final int stepCount;
	private final Random random = new Random();
	private int fireballTimer;
}
