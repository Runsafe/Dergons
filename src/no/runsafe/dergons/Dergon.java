package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.EntityEnderDragon;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.entity.IEnderDragon;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.tools.reflection.ReflectionHelper;

import java.util.ArrayList;
import java.util.HashMap;
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
		IPlayer idealPlayer = null;
		double health = 200D;

		for (IPlayer player : world.getPlayers())
		{
			ILocation playerLocation = player.getLocation();
			if (playerLocation != null && playerLocation.distance(targetLocation) < 200)
			{
				if (idealPlayer == null && playerLocation.getY() > minY)
					idealPlayer = player;

				health += 100D;
			}
		}

		if (idealPlayer != null)
		{
			dragon = (IEnderDragon) LivingEntity.EnderDragon.spawn(targetLocation);
			entityID = dragon.getEntityId();
			dragon.setCustomName("Dergon");
			dragon.setDragonTarget(idealPlayer);
			dragon.setMaxHealth(health);
			dragon.setHealth(health);

			fireballTimer = scheduler.startSyncRepeatingTask(new Runnable()
			{
				@Override
				public void run()
				{
					runCycle();
				}
			}, 1, 1);
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
		getEntity().remove();
		if (stepTimer > -1)
			scheduler.cancelTask(stepTimer);

		if (fireballTimer > -1)
			scheduler.cancelTask(fireballTimer);
	}

	public void runCycle()
	{
		IEnderDragon dragon = getEntity();

		Dergons.Debugger.debugFine("Dergon cycle running for " + dragon.getEntityId());

		List<IPlayer> targets = new ArrayList<IPlayer>(0);

		for (IPlayer checkPlayer : world.getPlayers())
		{
			ILocation playerLocation = checkPlayer.getLocation();
			if (playerLocation != null && !checkPlayer.isVanished() && playerLocation.distance(dragon.getLocation()) < 50)
			{
				Dergons.Debugger.debugFine("Player within 50 blocks, adding to target list: " + checkPlayer.getName());
				targets.add(checkPlayer);
			}
		}

		if (!targets.isEmpty())
		{
			IPlayer target = targets.get(random.nextInt(targets.size()));
			if (target != null && target.isOnline())
			{
				Dergons.Debugger.debugFine("Valid random target found, shooting fireball at " + target.getName());
				ILocation playerLocation = target.getLocation();
				if (playerLocation != null)
				{
					IEntity fireball = getEntity().Fire(ProjectileEntity.Fireball); // Shoot a fireball.
					ILocation ballLoc = fireball.getLocation();

					if (ballLoc != null)
						fireball.setVelocity(playerLocation.toVector().subtract(ballLoc.toVector()).normalize());
				}
			}
			else
			{
				Dergons.Debugger.debugFine("Invalid fireball target.");
			}
		}

		if (reset)
		{
			setTargetLocation(targetLocation);
			Dergons.Debugger.debugFine("Reset flag detected, sending back to start.");
		}
		else if (getTargetLocation().distance(targetLocation) > 150)
		{
			Dergons.Debugger.debugFine("Over 150 blocks from spawn point, redirecting.");
			boolean redirected = false;
			if (!targets.isEmpty())
			{
				IPlayer target = targets.get(random.nextInt(targets.size()));
				if (target != null && target.isOnline())
				{
					Dergons.Debugger.debugFine("Redirecting dergon to " + target.getName());
					ILocation playerLocation = target.getLocation();
					if (playerLocation != null)
					{
						setTargetLocation(playerLocation);
						redirected = true;
					}
				}
			}

			if (!redirected)
			{
				setTargetLocation(targetLocation);
				Dergons.Debugger.debugFine("Redirection failed, sending to spawn location.");
			}
		}
		else
		{
			Dergons.Debugger.debugFine("Range check: Within 150 blocks of spawn point.");
		}

		long pct = Math.round((dragon.getHealth() / dragon.getMaxHealth()) * 100);
		dragon.setCustomName("Dergon (" +  pct + "%)");
	}

	public IEnderDragon getDragon()
	{
		return getEntity();
	}

	public boolean isDergon(RunsafeEntity entity)
	{
		return isDergon(entity.getEntityId());
	}

	public boolean isDergon(int entityID)
	{
		return entityID == getEntity().getEntityId();
	}

	public void powerDown()
	{
		if (fireballTimer > -1)
			scheduler.cancelTask(fireballTimer);
	}

	public void registerAttack(IPlayer player, double damage)
	{
		String playerName = player.getName();
		damageDone.put(playerName, damageDone.containsKey(playerName) ? damageDone.get(playerName) + damage : damage);
	}

	public HashMap<String, Double> getDamageDone()
	{
		return damageDone;
	}

	private IEnderDragon getEntity()
	{
		if (dragon != null)
			return dragon;

		return (IEnderDragon) world.getEntityById(entityID);
	}

	private EntityEnderDragon getRawDragon()
	{
		return (EntityEnderDragon) ObjectUnwrapper.getMinecraft(getEntity());
	}

	private ILocation getTargetLocation()
	{
		EntityEnderDragon rawDragon = getRawDragon();
		return world.getLocation(
				(Double) ReflectionHelper.getObjectField(rawDragon, "h"),
				(Double) ReflectionHelper.getObjectField(rawDragon, "i"),
				(Double) ReflectionHelper.getObjectField(rawDragon, "j")
		);
	}

	private void setTargetLocation(ILocation location)
	{
		EntityEnderDragon rawDragon = getRawDragon();
		rawDragon.getControllerMove().a(location.getX(),  location.getY(), location.getZ(), 0);
	}

	public void reset()
	{
		reset = true;
	}

	private int currentStep = 0;
	private int stepTimer;
	private IEnderDragon dragon;
	private final IScheduler scheduler;
	private final ILocation targetLocation;
	private final IWorld world;
	private int entityID;
	private final int minStep;
	private final int maxStep;
	private final int minY;
	private final int stepCount;
	private final Random random = new Random();
	private int fireballTimer;
	private final HashMap<String, Double> damageDone = new HashMap<String, Double>(0);
	private boolean reset = false;
}
