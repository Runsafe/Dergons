package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.World;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

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

	public void spawnEntity()
	{
		scheduler.cancelTask(stepTimer);
		spawn();
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
			idealPlayer.sendColouredMessage("You are the ideal target.");
			World rawWorld = ObjectUnwrapper.getMinecraft(world);
			if (rawWorld != null)
			{
				idealPlayer.sendColouredMessage("The world was not null.");
				dragon = new CustomDergonEntity(world);
				if (rawWorld.addEntity(dragon, CreatureSpawnEvent.SpawnReason.NATURAL))
				{
					idealPlayer.sendColouredMessage("We spawned the entity");
					ILivingEntity livingDragon = getLivingEntity();
					entityID = livingDragon.getEntityId();
					livingDragon.setCustomName("Dergon");
					livingDragon.setMaxHealth(health);
					livingDragon.setHealth(health);
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
		getLivingEntity().remove();
		if (stepTimer > -1)
			scheduler.cancelTask(stepTimer);

		if (fireballTimer > -1)
			scheduler.cancelTask(fireballTimer);
	}

	public void runCycle()
	{
		ILivingEntity dragon = getLivingEntity();
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
					IEntity fireball = getLivingEntity().Fire(ProjectileEntity.Fireball); // Shoot a fireball.
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

		long pct = Math.round((dragon.getHealth() / dragon.getMaxHealth()) * 100);
		dragon.setCustomName("Dergon (" +  pct + "%)");
	}

	public boolean isDergon(RunsafeEntity entity)
	{
		return isDergon(entity.getEntityId());
	}

	public boolean isDergon(int entityID)
	{
		return entityID == getLivingEntity().getEntityId();
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

	public CustomDergonEntity getEntity()
	{
		if (dragon != null)
			return dragon;

		return (CustomDergonEntity) ObjectUnwrapper.getMinecraft(getLivingEntity());
	}

	public ILivingEntity getLivingEntity()
	{
		return (ILivingEntity) world.getEntityById(entityID);
	}

	private int currentStep = 0;
	private int stepTimer;
	private CustomDergonEntity dragon;
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
}
