package no.runsafe.dergons;

import net.minecraft.server.v1_8_R3.*;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.RunsafeFallingBlock;
import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Names of obfuscated variables in various spigot versions:
 *
 * Variables in EntityEnderDragon:
 * Type			v1_7_R3		v1_8_R3		v1_9_R2
 * public double		h			a			?		DergonX
 * public double		i			b			?		DergonY
 * public double		bm			c			?		DergonZ
 * public double[][]		bn			bk			b
 * public int			bo			bl			c
 * public float		bx			bu			bD		Radius?
 * public float		by			bv			bE		Radius?
 * public boolean		bz			bw			?	Currently has a selected target?
 * public boolean		bA			bx			bF
 * public int			bB			by			bG
 *
 * Entity.class:
 * public double		j			j			?
 * public boolean		G			F			C
 *
 * EntityLiving.Class:
 * protected int		bg			bc			bh
 * protected double		bh			bd			bi		Might have something to do with coordintes.
 * protected double		bi			be			bj		Might have something to do with coordintes.
 * protected double		bj			bf			bk		Might have something to do with coordintes.
 * protected double		bk			bg			bl
 * protected double		bl			bh			bm
 * public float		aN			aJ			aO		Yaw?
 * protected float		bf			bb			Either be, bf, or bg.
 */

public class Dergon extends EntityEnderDragon
{
	public Dergon(IWorld world, DergonHandler handler, ILocation targetLocation, int dergonID)
	{
		super(ObjectUnwrapper.getMinecraft(world));
		this.handler = handler;
		this.targetLocation = targetLocation;
		this.targetWorld = targetLocation.getWorld();
		this.dergonID = dergonID;
	}

	static final float PI = (float) Math.PI;

	//Dergon X Accessor and Mutator
	private double getDergonX()
	{
		return a;
	}

	private void setDergonX(double x)
	{
		a = x;
	}

	//Dergon Y Accessor and Mutator
	private double getDergonY()
	{
		return b;
	}

	private void setDergonY(double y)
	{
		b = y;
	}

	//Dergon Z Accessor and Mutator
	private double getDergonZ()
	{
		return c;
	}

	private void setDergonZ(double z)
	{
		c = z;
	}

	/*
	 * Dergon bodily appendages.
	 * Only their hitboxes.
	 * Names in various spigot versions:
	 * v1_7_R3		v1_8_R3		v1_9_R2
	 * bq			bn			bv		Head
	 * br			bo			bx		Body
	 * bv			bs			bB		Wing
	 * bw			bt			bC		Wing
	 * bs			bp			by		Tail section closest to body
	 * bt			bq			bz		Middle tail section
	 * bu			br			bA		Last tail section
	 * N/A			N/A			bw		Neck (Only in 1.9+)
	 */
	private EntityComplexPart dergonHead = bn;
	private EntityComplexPart dergonBody = bo;
	private EntityComplexPart dergonWing0 = bs;
	private EntityComplexPart dergonWing1 = bt;
	private EntityComplexPart dergonTailSection0 = bp;
	private EntityComplexPart dergonTailSection1 = bq;
	private EntityComplexPart dergonTailSection2 = br;

	/**
	 * Selects new player target.
	 */
	private void updateCurrentTarget()
	{
		bw = false;

		ILocation dergonLocation = targetWorld.getLocation(locX, locY, locZ);

		if (dergonLocation != null && flyOffLocation != null && random.nextFloat() == 0.1F)
			return;
		else
			flyOffLocation = null;

		// Check if we have any close players, if we do, fly away.
		if (dergonLocation != null && !dergonLocation.getPlayersInRange(10).isEmpty())
		{
			if (ridingPlayer == null && random.nextFloat() < 0.5F)
			{
				List<IPlayer> closePlayers = dergonLocation.getPlayersInRange(10);
				IPlayer unluckyChum = closePlayers.get(random.nextInt(closePlayers.size()));

				if (!unluckyChum.isVanished()
					&& !unluckyChum.isDead()
					&& unluckyChum.getGameMode() != GameMode.CREATIVE
					&& unluckyChum.getGameMode() != GameMode.SPECTATOR
				)
				{
					EntityHuman rawChum = ObjectUnwrapper.getMinecraft(unluckyChum);

					if (rawChum != null)
					{
						rawChum.mount(this);
						ridingPlayer = rawChum;
						handler.handleDergonMount(ridingPlayer.getName());
					}
				}
			}

			targetEntity = null;
			setDergonX(locX + random.nextInt(200) + -100);
			setDergonY(random.nextInt(100) + 70); // Somewhere above 70 to prevent floor clipping.
			setDergonZ(locZ + random.nextInt(200) + -100);
			flyOffLocation = targetWorld.getLocation(getDergonX(), getDergonY(), getDergonZ()); // Store the target fly-off location.
			return;
		}
		else
		{
			List<IPlayer> players = targetLocation.getPlayersInRange(200); // Grab all players in 200 blocks.
			List<IPlayer> targets = new ArrayList<IPlayer>(0);

			for (IPlayer player : players)
			{
				// Skip the player if we're vanished, in creative mode, or in spectator mode.
				if (player.isVanished()
					|| player.getGameMode() == GameMode.CREATIVE
					|| player.getGameMode() == GameMode.SPECTATOR
					|| isRidingPlayer(player.getName())
				)
					continue;

				ILocation playerLocation = player.getLocation();

				// If the player is greater than 50 blocks, we can target them.
				if (playerLocation != null && playerLocation.distance(targetLocation) > 50)
					targets.add(player);
			}

			if (!targets.isEmpty())
			{
				// Target a random player in 200 blocks.
				targetEntity = ObjectUnwrapper.getMinecraft(players.get(random.nextInt(players.size())));
				return;
			}
		}

		// Send the dergon back to the start point.
		setDergonX(targetLocation.getX());
		setDergonY(targetLocation.getY());
		setDergonZ(targetLocation.getZ());

		targetEntity = null;
	}

	/**
	 * Update method for Dergons.
	 * Names of this function in various spigot versions:
	 * v1_7_R3: e
	 * v1_8_R3: m
	 * v1_9_R2: n
	 */
	@Override
	public void m()
	{
		// Throw a player off it's back if we're high up.
		if (ridingPlayer != null && locY >= 90)
		{
			ridingPlayer.mount(null);
			ridingPlayer = null;
		}


		// Update the health bar to show the percentage of the dergon
		long pct = Math.round((getHealth() / getMaxHealth()) * 100);
		setCustomName("Dergon (" + pct + "%)");

		ILocation dergonLocation = targetWorld.getLocation(locX, locY, locZ);
		if (targetEntity != null && dergonLocation != null && random.nextFloat() < 0.2F)
			((RunsafeFallingBlock) targetWorld.spawnFallingBlock(dergonLocation, Item.Unavailable.Fire)).setDropItem(false);

		if (world.isClientSide)
		{
			float f = MathHelper.cos(bv * PI * 2.0F);
			float f1 = MathHelper.cos(bu * PI * 2.0F);
			if (f1 <= -0.3F && f >= -0.3F)
				world.a(locX, locY, locZ, "mob.enderdragon.wings", 5.0F, 0.8F + random.nextFloat() * 0.3F, false);
		}

		bu = bv;

		if (getHealth() <= 0.0F) // Check if the dragon is dead.
		{
			// If we're dead, play a random explosion effect at a random offset to it's corpse.
			world.addParticle(
					EnumParticle.EXPLOSION_LARGE,
					locX + (double) (random.nextFloat() - 0.5F) * 8.0F,
					locY + (double) (random.nextFloat() - 0.5F) * 4.0F + 2.0D,
					locZ + (double) (random.nextFloat() - 0.5F) * 8.0F,
					0.0D,
					0.0D,
					0.0D
			);
		}
		else
		{
			this.enderCrystalInteraction();
			float f = 0.2F / (MathHelper.sqrt(motX * motX + motZ * motZ) * 10.0F + 1.0F);
			f *= (float) Math.pow(2.0D, motY);
			bv += (bx ? f * 0.5F : f);

			yaw = (float) trimDegrees(yaw);
			if (bl < 0)
			{
				for (int d05 = 0; d05 < bk.length; ++d05)
				{
					bk[d05][0] = (double) yaw;
					bk[d05][1] = locY;
				}
			}

			if (++bl == bk.length)
				bl = 0;

			bk[bl][0] = (double) yaw;
			bk[bl][1] = locY;

			if (world.isClientSide)
			{
				if (bc > 0)
				{
					double d0 = locX + (bd - locX) / bc;
					double d1 = locY + (be - locY) / bc;
					double d2 = locZ + (bf - locZ) / bc;
					double d3 = trimDegrees(bg - (double) yaw);
					yaw = (float) ((double) yaw + d3 / bc);
					pitch = (float) ((double) pitch + (bh - (double) pitch) / bc);
					--bc;
					setPosition(d0, d1, d2);
					setYawPitch(yaw, pitch);
				}
			}
			else
			{
				//Get target position relative to Dergon
				double targetPosX = getDergonX() - locX;
				double targetPosY = getDergonY() - locY;
				double targetPosZ = getDergonZ() - locZ;
				double targetDistance = targetPosX * targetPosX + targetPosY * targetPosY + targetPosZ * targetPosZ;
				if (targetEntity != null)
				{
					setDergonX(targetEntity.locX);
					setDergonZ(targetEntity.locZ);
					double xDistanceToTarget = getDergonX() - locX;
					double yDistanceToTarget = getDergonZ() - locZ;
					double distanceToTarget = Math.sqrt(xDistanceToTarget * xDistanceToTarget + yDistanceToTarget * yDistanceToTarget);
					double ascendDistance = 0.4000000059604645D + distanceToTarget / 80.0D - 1.0D;

					if (ascendDistance > 10.0D)
						ascendDistance = 10.0D;

					setDergonY(targetEntity.getBoundingBox().b + ascendDistance);
				}
				else
				{
					setDergonX(getDergonX() + random.nextGaussian() * 2.0D);
					setDergonZ(getDergonZ() + random.nextGaussian() * 2.0D);
				}

				if (bw || targetDistance < 100.0D || targetDistance > 22500.0D || positionChanged || F)
					updateCurrentTarget();

				targetPosY /= (double) MathHelper.sqrt(targetPosX * targetPosX + targetPosZ * targetPosZ);
				float f3 = 0.6F;
				if (targetPosY < (double) (-f3))
					targetPosY = (double) (-f3);

				if (targetPosY > (double) f3)
					targetPosY = (double) f3;

				motY += targetPosY * 0.10000000149011612D;
				yaw = (float) trimDegrees(yaw);
				double targetDirection = 180.0D - Math.atan2(targetPosX, targetPosZ) * 180.0D / Math.PI;
				double d9 = trimDegrees(targetDirection - (double) yaw);

				if (d9 > 50.0D)
					d9 = 50.0D;

				if (d9 < -50.0D)
					d9 = -50.0D;

				Vec3D vec3d = new Vec3D(getDergonX() - locX, getDergonY() - locY, getDergonZ() - locZ).a();
				Vec3D vec3d1 = new Vec3D((double) MathHelper.sin(yaw * PI / 180.0F), motY, (double) (-MathHelper.cos(yaw * PI / 180.0F))).a();
				float f4 = (float) (vec3d1.b(vec3d) + 0.5D) / 1.5F;

				if (f4 < 0.0F)
					f4 = 0.0F;

				bb *= 0.8F;
				float f5 = MathHelper.sqrt(motX * motX + motZ * motZ) * 1.0F + 1.0F;
				double d10 = Math.sqrt(motX * motX + motZ * motZ) * 1.0D + 1.0D;

				if (d10 > 40.0D)
					d10 = 40.0D;

				bb = (float) ((double) bb + d9 * (0.699999988079071D / d10 / (double) f5));
				yaw += bb * 0.1F;
				float f6 = (float) (2.0D / (d10 + 1.0D));
				float f7 = 0.06F;

				a(0.0F, -1.0F, f7 * (f4 * f6 + (1.0F - f6)));
				if (bx)
					move(motX * 0.800000011920929D, motY * 0.800000011920929D, motZ * 0.800000011920929D);
				else
					move(motX, motY, motZ);

				Vec3D vec3d2 = new Vec3D(motX, motY, motZ).a();
				float f8 = (float) (vec3d2.b(vec3d1) + 1.0D) / 2.0F;

				f8 = 0.8F + 0.15F * f8;
				motX *= (double) f8;
				motZ *= (double) f8;
				motY *= 0.9100000262260437D;
			}

			aJ = yaw;
			dergonHead.width = dergonHead.length = 3.0F;
			dergonTailSection0.width = dergonTailSection0.length = 2.0F;
			dergonTailSection1.width = dergonTailSection1.length = 2.0F;
			dergonTailSection2.width = dergonTailSection2.length = 2.0F;
			dergonBody.length = 3.0F;
			dergonBody.width = 5.0F;
			dergonWing0.length = 2.0F;
			dergonWing0.width = 4.0F;
			dergonWing1.length = 3.0F;
			dergonWing1.width = 4.0F;
			float f1 = (float) (b(5, 1.0F)[1] - b(10, 1.0F)[1]) * 10.0F / 180.0F * PI;
			float f2 = MathHelper.cos(f1);
			float f9 = -MathHelper.sin(f1);
			float f10 = yaw * PI / 180.0F;
			float f11 = MathHelper.sin(f10);
			float f12 = MathHelper.cos(f10);

			dergonBody.t_();
			dergonBody.setPositionRotation(
					locX + (double) (f11 * 0.5F),
					locY,
					locZ - (double) (f12 * 0.5F),
					0.0F,
					0.0F
			);

			dergonWing0.t_();
			dergonWing0.setPositionRotation(
					locX + (double) (f12 * 4.5F),
					locY + 2.0D,
					locZ + (double) (f11 * 4.5F),
					0.0F,
					0.0F
			);

			dergonWing1.t_();
			dergonWing1.setPositionRotation(
					locX - (double) (f12 * 4.5F),
					locY + 2.0D,
					locZ - (double) (f11 * 4.5F),
					0.0F, 0.0F
			);

			if (!world.isClientSide && hurtTicks == 0)
			{
				launchEntities(world.getEntities(this, dergonWing0.getBoundingBox().grow(4.0D, 2.0D, 4.0D).shrink(0.0D, -2.0D, 0.0D)));
				launchEntities(world.getEntities(this, dergonWing1.getBoundingBox().grow(4.0D, 2.0D, 4.0D).shrink(0.0D, -2.0D, 0.0D)));
				hitEntities(world.getEntities(this, dergonHead.getBoundingBox().grow(1.0D, 1.0D, 1.0D)));
			}

			double[] adouble = b(5, 1.0F);
			double[] adouble1 = b(0, 1.0F);

			float f3 = MathHelper.sin(yaw * PI / 180.0F - bc * 0.01F);
			float f13 = MathHelper.cos(yaw * PI / 180.0F - bc * 0.01F);

			dergonHead.t_();
			dergonHead.setPositionRotation(locX + (double) (f3 * 5.5F * f2), locY + (adouble1[1] - adouble[1]) * 1.0D + (double) (f9 * 5.5F), locZ - (double) (f13 * 5.5F * f2), 0.0F, 0.0F);

			//Move the tail
			for (int tailNumber = 0; tailNumber < 3; ++tailNumber)
			{
				EntityComplexPart tailSection = null;

				switch (tailNumber)
				{
					case 0: tailSection = dergonTailSection0; break;
					case 1: tailSection = dergonTailSection1; break;
					case 2: tailSection = dergonTailSection2; break;
				}

				double[] adouble2 = b(12 + tailNumber * 2, 1.0F);
				float f14 = yaw * PI / 180.0F + (float) trimDegrees(adouble2[0] - adouble[0]) * PI / 180.0F * 1.0F;
				float f15 = MathHelper.sin(f14);
				float f16 = MathHelper.cos(f14);
				float f17 = 1.5F;
				float f18 = (float) (tailNumber + 1) * 2.0F;

				tailSection.t_();
				tailSection.setPositionRotation(locX - (double) ((f11 * f17 + f15 * f18) * f2), locY + (adouble2[1] - adouble[1]) * 1.0D - (double) ((f18 + f17) * f9) + 1.5D, locZ + (double) ((f12 * f17 + f16 * f18) * f2), 0.0F, 0.0F);
			}

			if (!world.isClientSide)
				bx = breakBlocks(dergonHead.getBoundingBox()) | breakBlocks(dergonBody.getBoundingBox());
		}
	}

	/**
	 * Launches entities a short distance.
	 * @param list Entities to launch
	 */
	private void launchEntities(List list)
	{
		double bodyBoundingBoxValue0 = (dergonBody.getBoundingBox().a + dergonBody.getBoundingBox().d) / 2.0D;
		double bodyBoundingBoxValue1 = (dergonBody.getBoundingBox().c + dergonBody.getBoundingBox().f) / 2.0D;

		for (Object rawEntity : list)
		{
			Entity entity = (Entity) rawEntity;
			if (entity instanceof EntityLiving)
			{
				double xDistance = entity.locX - bodyBoundingBoxValue0;
				double zDistance = entity.locZ - bodyBoundingBoxValue1;
				double distanceSquared = xDistance * xDistance + zDistance * zDistance;

				entity.g(xDistance / distanceSquared * 4.0D, 0.20000000298023224D, zDistance / distanceSquared * 4.0D);
			}
		}
	}

	/**
	 * Attack list of EntityLiving with 20.0F damage.
	 * @param list Entities to hit
	 */
	private void hitEntities(List list)
	{
		for (Object rawEntity : list)
		{
			Entity entity = (Entity) rawEntity;

			if (entity instanceof EntityLiving)
				entity.damageEntity(DamageSource.mobAttack(this), 20.0F);
		}
	}

	/**
	 * Handles breaking blocks; blocks are not to be broken.
	 * @param axisalignedbb Does nothing.
	 * @return True if blocks destroyed, false if no blocks destroyed. Always returns false.
	 */
	private boolean breakBlocks(AxisAlignedBB axisalignedbb)
	{
		return false;
	}

	/**
	 * Trims down a degree value to between -180 and 180.
	 * @param degreeValue Number to trim.
	 * @return Trimmed degree value.
	 */
	private double trimDegrees(double degreeValue)
	{
		return MathHelper.g(degreeValue);
	}

	/**
	 * Damage the dergon.
	 * Overrides method in EntityLiving.class
	 * Names of this function in various spigot versions:
	 * v1_7_R3: d, returns void
	 * v1_8_R3: d, returns boolean and is in EntityLiving
	 * v1_9_R2: damageEntity0
	 * @param source damage source
	 * @param f Damage amount
	 * @return True if damaged, false if not damaged.
	 */
	@Override
	protected boolean d(DamageSource source, float f)
	{
		if (ridingPlayer == null || !isRidingPlayer(source.getEntity().getName()))
			return super.d(source, handler.handleDergonDamage(this, source, f));

		return false;
	}

	/**
	 * Handle ender crystal interactions.
	 * Does nothing.
	 */
	private void enderCrystalInteraction()
	{
		//Unlike regular ender dragons, dergons do not associate with the likes of ender crystals.
	}

	/**
	 * Handle dergon death.
	 * Names of this function in various spigot versions:
	 * v1_7_R3: aE
	 * v1_8_R3: aZ
	 */
	@Override
	protected void aZ()
	{
		super.aZ();
		if (this.by == 200)
			handler.handleDergonDeath(this);
	}

	/**
	 * Gets the world the dergon is in.
	 * @return World the dergon is in.
	 */
	public IWorld getDergonWorld()
	{
		return targetWorld;
	}

	private boolean isRidingPlayer(String playerName)
	{
		return ridingPlayer != null && ridingPlayer.getName().equals(playerName);
	}

	public int getDergonID()
	{
		return dergonID;
	}

	private Entity targetEntity;
	private final DergonHandler handler;
	private final ILocation targetLocation;
	private ILocation flyOffLocation;
	private final IWorld targetWorld;
	private final Random random = new Random();
	private EntityHuman ridingPlayer = null;
	private final int dergonID;
}
