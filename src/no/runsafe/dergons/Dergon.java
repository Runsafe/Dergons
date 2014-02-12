package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.entity.IEnderDragon;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dergon
{
	public Dergon(IScheduler scheduler, ILocation targetLocation, int min, int max, int steps, int minY)
	{
		this.scheduler = scheduler;
		this.targetLocation = targetLocation;

		world = targetLocation.getWorld();

		this.minStep = min;
		this.maxStep = max;
		this.stepCount = steps;

		this.minY = minY;

		if (world != null)
			processStep();
	}

	private void spawn()
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
		List<IPlayer> targets = new ArrayList<IPlayer>(0);

		for (IPlayer checkPlayer : world.getPlayers())
		{
			ILocation playerLocation = checkPlayer.getLocation();
			if (playerLocation != null && !checkPlayer.isVanished() && playerLocation.distance(entity.getLocation()) < 50)
				targets.add(checkPlayer);
		}

		if (targets.isEmpty())
			return;

		IPlayer target = targets.get(random.nextInt(targets.size()));
		if (target != null && target.isOnline())
		{
			ILocation playerLocation = target.getLocation();
			if (playerLocation != null)
			{
				IEntity fireball = entity.Fire(ProjectileEntity.Fireball); // Shoot a fireball.
				ILocation ballLoc = fireball.getLocation();

				if (ballLoc != null)
					fireball.setVelocity(playerLocation.toVector().subtract(ballLoc.toVector()).normalize());
			}
		}
	}

	public boolean isDergon(RunsafeEntity entity)
	{
		return entity.getEntityId() == this.entity.getEntityId();
	}

	public void powerDown()
	{
		if (fireballTimer > -1)
			scheduler.cancelTask(fireballTimer);
	}

	private int currentStep = 0;
	private int stepTimer;
	private IEnderDragon entity;
	private final IScheduler scheduler;
	private final ILocation targetLocation;
	private final IWorld world;
	private final int minStep;
	private final int maxStep;
	private final int minY;
	private final int stepCount;
	private final Random random = new Random();
	private int fireballTimer;
}
