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
 * public double		h			a			?	//DergonX
 * public double		i			b			?	//DergonY
 * public double		bm			c			?	//DergonZ
 * public double[][]		bn			bk			b
 * public int			bo			bl			c
 * public float		bx			bu			bD	//Radius?
 * public float		by			bv			bE	//Radius?
 * public boolean		bz			bw			?	//Currently has a selected target?
 * public boolean		bA			bx			bF
 * public int			bB			by			bG
 *
 * Entity.class:
 * public double		j			j			?
 * public boolean		G			F			C
 *
 * EntityLiving.Class:
 * protected int		bg			bc			bh
 * protected double		bh			bd			bi
 * protected double		bi			be			bj
 * protected double		bj			bf			bk
 * protected double		bk			bg			bl
 * protected double		bl			bh			bm
 * public float		aN			aJ			aO	//Yaw?
 * protected float		bf			bb			Either be, bf, or bg.
 *
 */


public class Dergon extends EntityEnderDragon
{
	private final float PI_FLOAT = 3.1415927F;
	private final double PI_DOUBLE = 3.1415927410125732D;

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
	 * Dergon constructor.
	 * @param world World to spawn in.
	 * @param handler
	 * @param targetLocation Coordinates of the dergon's dinner.
	 * @param dergonID Dergon's ID.
	 */
	public Dergon(IWorld world, DergonHandler handler, ILocation targetLocation, int dergonID)
	{
		super(ObjectUnwrapper.getMinecraft(world));
		this.handler = handler;
		this.targetLocation = targetLocation;
		this.targetWorld = targetLocation.getWorld();
		this.dergonID = dergonID;
	}

	/**
	 * Selects someone new for the dergon to eat.
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
			flyOffLocation = targetWorld.getLocation(getDergonX(), getDergonY(), getDergonZ());// Store the target fly-off location.
			return;
		}
		else
		{
			List<IPlayer> players = targetLocation.getPlayersInRange(200); // Grab all players in 200 blocks.
			List<IPlayer> targets = new ArrayList<IPlayer>(0);

			for (IPlayer player : players)
			{
				// Skip the player if we're vanished or in creative mode.
				if (player.isVanished()
					|| player.getGameMode() == GameMode.CREATIVE
					|| isRidingPlayer(player.getName())
					|| player.getGameMode() == GameMode.SPECTATOR
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
	 * Update function for Dergons.
	 * Names of this function in various spigot versions:
	 * v1_7_R3: e
	 * v1_8_R3: m
	 * v1_9_R2: n
	 */
	@Override
	public void m()
	{
		// Throw a player off its back if we're high up.
		if (ridingPlayer != null && locY >= 90)
		{
			ridingPlayer.mount(null);
			ridingPlayer = null;
		}

		// Update the health bar to show the percentage of the dergon
		long healthPercentage = Math.round((getHealth() / getMaxHealth()) * 100);
		setCustomName("Dergon (" + healthPercentage + "%)");

		ILocation dergonLocation = targetWorld.getLocation(locX, locY, locZ);
		if (targetEntity != null && dergonLocation != null && random.nextFloat() < 0.2F)
			((RunsafeFallingBlock) targetWorld.spawnFallingBlock(dergonLocation, Item.Unavailable.Fire)).setDropItem(false);

		if (world.isClientSide)
		{
			float floatValue0 = MathHelper.cos(bv * PI_FLOAT * 2.0F);
			float floatValue1 = MathHelper.cos(bu * PI_FLOAT * 2.0F);
			if (floatValue1 <= -0.3F && floatValue0 >= -0.3F)
				world.a(locX, locY, locZ, "mob.enderdragon.wings", 5.0F, 0.8F + random.nextFloat() * 0.3F, false);
		}

		bu = bv;

		if (getHealth() <= 0.0F) // Check if the dragon is dead.
		{
			// If we're dead, play a random explosion effect at a random offset to it's corpse.
			world.addParticle(
					EnumParticle.EXPLOSION_LARGE,
					locX + (double) ((random.nextFloat() - 0.5F) * 8.0F),
					locY + (double) ((random.nextFloat() - 0.5F) * 4.0F) + 2.0D,
					locZ + (double) ((random.nextFloat() - 0.5F) * 8.0F),
					0.0D,
					0.0D,
					0.0D
			);
		}
		else
		{ // Function ends right after this code block.
			//this.bN();
			/* Was: this.bN()/CC()  Should be: this.bP()/.n()/.cW()
			 * However .bP appears to deal with ender crystals, since the dergon
			 * doesn't interact with ender crystals it's safe to disable it.
			 */
			float floatValue0 = 0.2F / (MathHelper.sqrt(motX * motX + motZ * motZ) * 10.0F + 1.0F);
			floatValue0 *= (float) Math.pow(2.0D, motY);
			bv += (bx ? floatValue0 * 0.5F : floatValue0);

			yaw = trimDegrees(yaw);
			if (bl < 0)
			{
				for (int forLoopIndex = 0; forLoopIndex < bk.length; ++forLoopIndex)
				{
					bk[forLoopIndex][0] = (double) yaw;
					bk[forLoopIndex][1] = locY;
				}
			}

			if (++bl == bk.length)
				bl = 0;

			bk[bl][0] = (double) yaw;
			bk[bl][1] = locY;
			double valueX;
			double valueY;
			double valueZ;

			if (world.isClientSide)
			{
				if (bc > 0)
				{
					valueX = locX + (bd - locX) / bc;
					valueY = locY + (be - locY) / bc;
					valueZ = locZ + (bf - locZ) / bc;
					double yawAdjustment = trimDegrees(bg - (double) yaw);//Redundant?
					yaw = (float) ((double) yaw + yawAdjustment / bc);
					pitch = (float) ((double) pitch + (bh - (double) pitch) / bc);
					--bc;
					setPosition(valueX, valueY, valueZ);
					setYawPitch(yaw, pitch);
				}
			}
			else
			{
				valueX = getDergonX() - locX;
				valueY = getDergonY() - locY;
				valueZ = getDergonZ() - locZ;
				double doubleValue3 = valueX * valueX + valueY * valueY + valueZ * valueZ;
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

				if (bw || doubleValue3 < 100.0D || doubleValue3 > 22500.0D || positionChanged || F)
					updateCurrentTarget();

				valueY /= (double) MathHelper.sqrt(valueX * valueX + valueZ * valueZ);
				final double Y_LIMIT = 0.6D;
				if (valueY < (-Y_LIMIT))
					valueY = (-Y_LIMIT);

				if (valueY > Y_LIMIT)
					valueY = Y_LIMIT;

				motY += valueY * 0.10000000149011612D;
				yaw = trimDegrees(yaw);
				double doubleValue8 = 180.0D - Math.atan2(valueX, valueZ) * 180.0D / PI_DOUBLE;
				double doubleValue9 = trimDegrees(doubleValue8 - (double) yaw);

				if (doubleValue9 > 50.0D)
					doubleValue9 = 50.0D;

				if (doubleValue9 < -50.0D)
					doubleValue9 = -50.0D;

				float yawRadians = yaw * PI_FLOAT / 180.0F; //Convert yaw from degrees to radians
				Vec3D vec3d = new Vec3D(getDergonX() - locX, getDergonY() - locY, getDergonZ() - locZ).a();
				Vec3D vec3d1 = new Vec3D((double) MathHelper.sin(yawRadians), motY, (double) (-MathHelper.cos(yawRadians))).a();
				float floatValue4 = (float) (vec3d1.b(vec3d) + 0.5D) / 1.5F;

				if (floatValue4 < 0.0F)
					floatValue4 = 0.0F;

				bb *= 0.8F;
				float floatValue5 = MathHelper.sqrt(motX * motX + motZ * motZ) * 1.0F + 1.0F;
				double doubleValue10 = Math.sqrt(motX * motX + motZ * motZ) * 1.0D + 1.0D;

				if (doubleValue10 > 40.0D)
					doubleValue10 = 40.0D;

				bb = (float) ((double) bb + doubleValue9 * (0.699999988079071D / doubleValue10 / (double) floatValue5));
				yaw += bb * 0.1F;
				float floatValue6 = (float) (2.0D / (doubleValue10 + 1.0D));
				float floatValue7 = 0.06F;

				a(0.0F, -1.0F, floatValue7 * (floatValue4 * floatValue6 + (1.0F - floatValue6)));
				if (bx)
					move(motX * 0.800000011920929D, motY * 0.800000011920929D, motZ * 0.800000011920929D);
				else
					move(motX, motY, motZ);

				Vec3D vec3d2 = new Vec3D(motX, motY, motZ).a();
				float floatValue8 = (float) (vec3d2.b(vec3d1) + 1.0D) / 2.0F;

				floatValue8 = 0.8F + 0.15F * floatValue8;
				motX *= (double) floatValue8;
				motZ *= (double) floatValue8;
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
			float floatValue1 = (float) (b(5, 1.0F)[1] - b(10, 1.0F)[1]) * 10.0F / 180.0F * PI_FLOAT;
			float cosFloat1 = MathHelper.cos(floatValue1);
			float negSinFloat1 = -MathHelper.sin(floatValue1);
			float yawRadians = yaw * PI_FLOAT / 180.0F; //Convert yaw from degrees to radians
			float sinYaw = MathHelper.sin(yawRadians);
			float cosYaw = MathHelper.cos(yawRadians);

			/*
			 * Names of obfuscated function in various spigot versions:
			 * v1_7_R3: .h()
			 * v1_8_R3: .t_()
			 * v1_9_R2: .m()   <--Behaves slightly differently, but probably the same function.
			 */

			//Move body
			dergonBody.t_();
			dergonBody.setPositionRotation(
					locX + (double) (sinYaw * 0.5F),
					locY,
					locZ - (double) (cosYaw * 0.5F),
					0.0F,
					0.0F
			);

			//Move wing
			dergonWing0.t_();
			dergonWing0.setPositionRotation(
					locX + (double) (cosYaw * 4.5F),
					locY + 2.0D,
					locZ + (double) (sinYaw * 4.5F),
					0.0F,
					0.0F
			);

			//Move wing
			dergonWing1.t_();
			dergonWing1.setPositionRotation(
					locX - (double) (cosYaw * 4.5F),
					locY + 2.0D,
					locZ - (double) (sinYaw * 4.5F),
					0.0F,
					0.0F
			);

			if (!world.isClientSide && hurtTicks == 0)
			{
				launchEntities(world.getEntities(this, dergonWing0.getBoundingBox().grow(4.0D, 2.0D, 4.0D).shrink(0.0D, -2.0D, 0.0D)));
				launchEntities(world.getEntities(this, dergonWing1.getBoundingBox().grow(4.0D, 2.0D, 4.0D).shrink(0.0D, -2.0D, 0.0D)));
				hitEntities(world.getEntities(this, dergonHead.getBoundingBox().grow(1.0D, 1.0D, 1.0D)));
			}

			double[] adouble = b(5, 1.0F);
			double[] adouble1 = b(0, 1.0F);

			float floatValue3 = MathHelper.sin(yawRadians - bc * 0.01F);
			float floatValue13 = MathHelper.cos(yawRadians - bc * 0.01F);

			//Move head
			dergonHead.t_();
			dergonHead.setPositionRotation(
					locX + (double) (floatValue3 * 5.5F * cosFloat1),
					locY + (double) (negSinFloat1 * 5.5F) + (adouble1[1] - adouble[1]) * 1.0D,
					locZ - (double) (floatValue13 * 5.5F * cosFloat1),
					0.0F,
					0.0F
			);

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
				float float14 = yawRadians + (float) trimDegrees(adouble2[0] - adouble[0]) * (PI_FLOAT / 180.0F) * 1.0F;
				float sinFloat14 = MathHelper.sin(float14);
				float cosFloat14 = MathHelper.cos(float14);
				final float ONE_DOT_FIVE = 1.5F;
				float floatValue15 = (float) (tailNumber + 1) * 2.0F; // 2, 4, 6

				tailSection.t_();
				tailSection.setPositionRotation(
						locX - (double) ((sinYaw * ONE_DOT_FIVE + sinFloat14 * floatValue15) * cosFloat1),
						locY - (double) ((floatValue15 + ONE_DOT_FIVE) * negSinFloat1) + ((adouble2[1] - adouble[1]) * 1.0D)+ 1.5D,
						locZ + (double) ((cosYaw * ONE_DOT_FIVE + cosFloat14 * floatValue15) * cosFloat1),
						0.0F,
						0.0F
				);
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

				entity.g(//Change Velocity
						xDistance / distanceSquared * 4.0D,
						0.20000000298023224D,
						zDistance / distanceSquared  * 4.0D
				);
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
	private float trimDegrees(float degreeValue)
	{
		return MathHelper.g(degreeValue);//.g trims degree value to within -180 to 180
	}

	/**
	 * Trims down a degree value to between -180 and 180.
	 * @param degreeValue Number to trim.
	 * @return Trimmed degree value.
	 */
	private double trimDegrees(double degreeValue)
	{
		return MathHelper.g(degreeValue);//.g trims degree value to within -180 to 180
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
	 * Handle dergon death.
	 * Names of this function in various spigot versions:
	 * v1_7_R3: aE
	 * v1_8_R3: aZ
 	 */
	@Override
	protected void aZ()
	{
		super.aZ();
		if (by == 200)
			handler.handleDergonDeath(this);
	}

	/**
	 * Gets the world the dergon is in.
	 * getWorld() would be a better name, but 1.8 does not like that name.
	 * @return World the dergon is in.
	 */
	public IWorld getDergonWorld()
	{
		return targetWorld;
	}

	/**
	 * Check if dergon is riding a particular player.
	 * @param playerName Check if this player is being ridden by the dergon.
	 * @return True if dergon is riding input player.
	 */
	private boolean isRidingPlayer(String playerName)
	{
		return ridingPlayer != null && ridingPlayer.getName().equals(playerName);
	}

	/**
	 * Get the dergon's ID.
	 * @return Dergon's ID.
	 */
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
