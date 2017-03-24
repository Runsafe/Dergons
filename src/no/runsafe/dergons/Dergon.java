package no.runsafe.dergons;

import net.minecraft.server.v1_7_R3.*;
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

public class Dergon extends EntityEnderDragon
{
	//Dergon X Accessor and Mutator
	private double getDergonX()
	{
		return h;
	}

	private void setDergonX(double x)
	{
		h = x;
	}

	//Dergon Y Accessor and Mutator
	private double getDergonY()
	{
		return i;
	}

	private void setDergonY(double y)
	{
		i = y;
	}

	//Dergon Z Accessor and Mutator
	private double getDergonZ()
	{
		return bm;
	}

	private void setDergonZ(double z)
	{
		bm = z;
	}

    //Unknown float0 in EntityInsentient
	private float unknownFloat0 = f;

	private void setUnknownFloat0(float x)
	{
		f = x;
	}

	public Dergon(IWorld world, DergonHandler handler, ILocation targetLocation, int dergonID)
	{
		super(ObjectUnwrapper.getMinecraft(world));
		this.handler = handler;
		this.targetLocation = targetLocation;
		this.targetWorld = targetLocation.getWorld();
		this.dergonID = dergonID;
	}

	private void updateCurrentTarget()
	{
		bz = false;

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

				if (!unluckyChum.isVanished() && !unluckyChum.isDead() && unluckyChum.getGameMode() != GameMode.CREATIVE)
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
				if (player.isVanished() || player.getGameMode() == GameMode.CREATIVE || isRidingPlayer(player.getName()))
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

	/*
	* Update function for Dergons.
	* Names of this function in various spigot versions:
	* v1_7_R3: e
	* v1_8_R3: m
	 */
	@Override
	public void e()
	{
		// Throw a player off it's back if we're high up.
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

		float floatValue0;
		float floatValue1;

		if (world.isStatic)
		{
			setUnknownFloat0(MathHelper.cos(by * 3.1415927F * 2.0F));
			floatValue1 = MathHelper.cos(bx * 3.1415927F * 2.0F);
			if (floatValue1 <= -0.3F && unknownFloat0 >= -0.3F)
				world.a(locX, locY, locZ, "mob.enderdragon.wings", 5.0F, 0.8F + random.nextFloat() * 0.3F, false);
		}

		bx = by;
		float floatValue2;

		if (getHealth() <= 0.0F) // Check if the dragon is dead.
		{
			// If we're dead, play a random explosion effect at a random offset to it's corpse.
			floatValue0 = (random.nextFloat() - 0.5F) * 8.0F;
			floatValue1 = (random.nextFloat() - 0.5F) * 4.0F;
			floatValue2 = (random.nextFloat() - 0.5F) * 8.0F;
			world.addParticle(
					"largeexplode",
					locX + (double) floatValue0,
					locY + 2.0D + (double) floatValue1,
					locZ + (double) floatValue2,
					0.0D,
					0.0D,
					0.0D
			);
		}
		else
		{
			this.bN();//Function changed to .cc() in 1.8
			setUnknownFloat0(0.2F / (MathHelper.sqrt(motX * motX + motZ * motZ) * 10.0F + 1.0F));
			setUnknownFloat0(unknownFloat0 * (float) Math.pow(2.0D, motY));
			by += (bA ? unknownFloat0 * 0.5F : unknownFloat0);

			yaw = MathHelper.g(yaw);
			if (bo < 0)
			{
				for (int forLoopIndex = 0; forLoopIndex < bn.length; ++forLoopIndex)
				{
					bn[forLoopIndex][0] = (double) yaw;
					bn[forLoopIndex][1] = locY;
				}
			}

			if (++bo == bn.length)
				bo = 0;

			bn[bo][0] = (double) yaw;
			bn[bo][1] = locY;
			double doubleValue0;
			double doubleValue1;
			double doubleValue2;
			double doubleValue3;
			float floatValue3;

			if (world.isStatic)
			{
				if (bg > 0)
				{
					doubleValue0 = locX + (bh - locX) / bg;
					doubleValue1 = locY + (bi - locY) / bg;
					doubleValue2 = locZ + (bj - locZ) / bg;
					doubleValue3 = MathHelper.g(bk - (double) yaw);
					yaw = (float) ((double) yaw + doubleValue3 / bg);
					pitch = (float) ((double) pitch + (bl - (double) pitch) / bg);
					--bg;
					setPosition(doubleValue0, doubleValue1, doubleValue2);
					b(yaw, pitch);
				}
			}
			else
			{
				doubleValue0 = getDergonX() - locX;
				doubleValue1 = getDergonY() - locY;
				doubleValue2 = getDergonZ() - locZ;
				doubleValue3 = doubleValue0 * doubleValue0 + doubleValue1 * doubleValue1 + doubleValue2 * doubleValue2;
				if (targetEntity != null)
				{
					setDergonX(targetEntity.locX);
					setDergonZ(targetEntity.locZ);
					double doubleValue4 = getDergonX() - locX;
					double doubleValue5 = getDergonZ() - locZ;
					double doubleValue6 = Math.sqrt(doubleValue4 * doubleValue4 + doubleValue5 * doubleValue5);
					double doubleValue7 = 0.4000000059604645D + doubleValue6 / 80.0D - 1.0D;

					if (doubleValue7 > 10.0D)
						doubleValue7 = 10.0D;

					setDergonY(targetEntity.boundingBox.b + doubleValue7);
				}
				else
				{
					setDergonX(getDergonX() + random.nextGaussian() * 2.0D);
					bm += random.nextGaussian() * 2.0D;
				}

				if (bz || doubleValue3 < 100.0D || doubleValue3 > 22500.0D || positionChanged || G)
					updateCurrentTarget();

				doubleValue1 /= (double) MathHelper.sqrt(doubleValue0 * doubleValue0 + doubleValue2 * doubleValue2);
				floatValue3 = 0.6F;
				if (doubleValue1 < (double) (-floatValue3))
					doubleValue1 = (double) (-floatValue3);

				if (doubleValue1 > (double) floatValue3)
					doubleValue1 = (double) floatValue3;

				motY += doubleValue1 * 0.10000000149011612D;
				yaw = MathHelper.g(yaw);
				double doubleValue8 = 180.0D - Math.atan2(doubleValue0, doubleValue2) * 180.0D / 3.1415927410125732D;
				double doubleValue9 = MathHelper.g(doubleValue8 - (double) yaw);

				if (doubleValue9 > 50.0D)
					doubleValue9 = 50.0D;

				if (doubleValue9 < -50.0D)
					doubleValue9 = -50.0D;

				Vec3D vec3d = Vec3D.a(getDergonX() - locX, getDergonY() - locY, bm - locZ).a();
				Vec3D vec3d1 = Vec3D.a((double) MathHelper.sin(yaw * 3.1415927F / 180.0F), motY, (double) (-MathHelper.cos(yaw * 3.1415927F / 180.0F))).a();
				float floatValue4 = (float) (vec3d1.b(vec3d) + 0.5D) / 1.5F;

				if (floatValue4 < 0.0F)
					floatValue4 = 0.0F;

				bf *= 0.8F;
				float floatValue5 = MathHelper.sqrt(motX * motX + motZ * motZ) * 1.0F + 1.0F;
				double doubleValue10 = Math.sqrt(motX * motX + motZ * motZ) * 1.0D + 1.0D;

				if (doubleValue10 > 40.0D)
					doubleValue10 = 40.0D;

				bf = (float) ((double) bf + doubleValue9 * (0.699999988079071D / doubleValue10 / (double) floatValue5));
				yaw += bf * 0.1F;
				float floatValue6 = (float) (2.0D / (doubleValue10 + 1.0D));
				float floatValue7 = 0.06F;

				a(0.0F, -1.0F, floatValue7 * (floatValue4 * floatValue6 + (1.0F - floatValue6)));
				if (bA)
					move(motX * 0.800000011920929D, motY * 0.800000011920929D, motZ * 0.800000011920929D);
				else
					move(motX, motY, motZ);

				Vec3D vec3d2 = Vec3D.a(motX, motY, motZ).a();
				float floatValue8 = (float) (vec3d2.b(vec3d1) + 1.0D) / 2.0F;

				floatValue8 = 0.8F + 0.15F * floatValue8;
				motX *= (double) floatValue8;
				motZ *= (double) floatValue8;
				motY *= 0.9100000262260437D;
			}

			aN = yaw;
			bq.width = bq.length = 3.0F;
			bs.width = bs.length = 2.0F;
			bt.width = bt.length = 2.0F;
			bu.width = bu.length = 2.0F;
			br.length = 3.0F;
			br.width = 5.0F;
			bv.length = 2.0F;
			bv.width = 4.0F;
			bw.length = 3.0F;
			bw.width = 4.0F;
			floatValue1 = (float) (b(5, 1.0F)[1] - b(10, 1.0F)[1]) * 10.0F / 180.0F * 3.1415927F;
			floatValue2 = MathHelper.cos(floatValue1);
			float floatValue9 = -MathHelper.sin(floatValue1);
			float floatValue10 = yaw * 3.1415927F / 180.0F;
			float floatValue11 = MathHelper.sin(floatValue10);
			float floatValue12 = MathHelper.cos(floatValue10);

			br.h();
			br.setPositionRotation(
					locX + (double) (floatValue11 * 0.5F),
					locY, locZ - (double) (floatValue12 * 0.5F),
					0.0F,
					0.0F
			);
			bv.h();
			bv.setPositionRotation(
					locX + (double) (floatValue12 * 4.5F),
					locY + 2.0D,
					locZ + (double) (floatValue11 * 4.5F),
					0.0F,
					0.0F
			);
			bw.h();
			bw.setPositionRotation(
					locX - (double) (floatValue12 * 4.5F),
					locY + 2.0D,
					locZ - (double) (floatValue11 * 4.5F),
					0.0F,
					0.0F
			);

			if (!world.isStatic && hurtTicks == 0)
			{
				a(world.getEntities(this, bv.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				a(world.getEntities(this, bw.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				b(world.getEntities(this, bq.boundingBox.grow(1.0D, 1.0D, 1.0D)));
			}

			double[] adouble = b(5, 1.0F);
			double[] adouble1 = b(0, 1.0F);

			floatValue3 = MathHelper.sin(yaw * 3.1415927F / 180.0F - bg * 0.01F);
			float floatValue13 = MathHelper.cos(yaw * 3.1415927F / 180.0F - bg * 0.01F);

			bq.h();
			bq.setPositionRotation(
					locX + (double) (floatValue3 * 5.5F * floatValue2),
					locY + (adouble1[1] - adouble[1]) * 1.0D + (double) (floatValue9 * 5.5F),
					locZ - (double) (floatValue13 * 5.5F * floatValue2),
					0.0F,
					0.0F
			);

			for (int forLoopIndex = 0; forLoopIndex < 3; ++forLoopIndex)
			{
				EntityComplexPart entitycomplexpart = null;

				if (forLoopIndex == 0)
					entitycomplexpart = bs;

				if (forLoopIndex == 1)
					entitycomplexpart = bt;

				if (forLoopIndex == 2)
					entitycomplexpart = bu;

				double[] adouble2 = b(12 + forLoopIndex * 2, 1.0F);
				float floatValue14 = yaw * 3.1415927F / 180.0F + b(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F * 1.0F;
				float floatValue15 = MathHelper.sin(floatValue14);
				float floatValue16 = MathHelper.cos(floatValue14);
				float floatValue17 = 1.5F;
				float floatValue18 = (float) (forLoopIndex + 1) * 2.0F;

				entitycomplexpart.h();
				entitycomplexpart.setPositionRotation(
						locX - (double) ((floatValue11 * floatValue17 + floatValue15 * floatValue18) * floatValue2),
						locY + (adouble2[1] - adouble[1]) * 1.0D - (double) ((floatValue18 + floatValue17) * floatValue9) + 1.5D,
						locZ + (double) ((floatValue12 * floatValue17 + floatValue16 * floatValue18) * floatValue2),
						0.0F,
						0.0F
				);
			}

			if (!world.isStatic)
				bA = a(bq.boundingBox) | a(br.boundingBox);
		}
	}

	private void a(List list)
	{
		double doubleValue0 = (br.boundingBox.a + br.boundingBox.d) / 2.0D;
		double doubleValue1 = (br.boundingBox.c + br.boundingBox.f) / 2.0D;

		for (Object rawEntity : list)
		{
			Entity entity = (Entity) rawEntity;
			if (entity instanceof EntityLiving)
			{
				double doubleValue2 = entity.locX - doubleValue0;
				double doubleValue3 = entity.locZ - doubleValue1;
				double doubleValue4 = doubleValue2 * doubleValue2 + doubleValue3 * doubleValue3;

				entity.g(
						doubleValue2 / doubleValue4 * 4.0D,
						0.20000000298023224D,
						doubleValue3 / doubleValue4 * 4.0D
				);
			}
		}
	}

	private void b(List list)
	{
		for (Object rawEntity : list)
		{
			Entity entity = (Entity) rawEntity;

			if (entity instanceof EntityLiving)
				entity.damageEntity(DamageSource.mobAttack(this), 20.0F);
		}
	}

	private boolean a(AxisAlignedBB axisalignedbb)
	{
		return false;
	}

	private float b(double d0)
	{
		return (float) MathHelper.g(d0);
	}

	/*
	* Names of this function in various spigot versions:
	* v1_7_R3: d, returns void
	* v1_8_R3: d, returns boolean
	 */
	@Override
	protected void d(DamageSource source, float f)
	{
		if (ridingPlayer == null || !isRidingPlayer(source.getEntity().getName()))
			super.d(source, handler.handleDergonDamage(this, source, f));
	}

	@Override
	protected void aE()
	{
		super.aE();
		if (this.bB == 200)
			handler.handleDergonDeath(this);
	}

	public IWorld getWorld()
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
