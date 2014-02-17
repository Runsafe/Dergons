package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.*;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Item;
import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dergon extends EntityEnderDragon
{
	public Dergon(IWorld world, DergonHandler handler, ILocation targetLocation)
	{
		super(ObjectUnwrapper.getMinecraft(world));
		this.handler = handler;
		this.targetLocation = targetLocation;
		this.targetWorld = targetLocation.getWorld();
	}

	private void bN()
	{
		if (bC != null) // Check we have an ender crystal selected.
		{
			if (bC.dead) // Is the ender crystal we have selected dead?
			{
				if (!world.isStatic)
					a(bq, DamageSource.explosion(null), 10.0F); // Damage the dragon for 10 by explosion.

				bC = null; // Void the selected crystal.
			}
			else if (ticksLived % 10 == 0 && getHealth() < getMaxHealth())
			{
				setHealth(getHealth() + 1.0F); // Regen the dragon from the crystal.
			}
		}

		if (random.nextInt(10) == 0) // 1 in ten chance.
		{
			float f = 32.0F;
			List list = world.a(EntityEnderCrystal.class, boundingBox.grow((double) f, (double) f, (double) f)); // Grab all crystals in 32 blocks.
			EntityEnderCrystal entityendercrystal = null;
			double d0 = Double.MAX_VALUE;

			for (Object rawCrystal : list) // Loop every crystal.
			{
				EntityEnderCrystal crystal = (EntityEnderCrystal) rawCrystal;
				double d1 = crystal.e(this);

				if (d1 < d0)
				{
					d0 = d1;
					entityendercrystal = crystal;
				}
			}
			bC = entityendercrystal; // Set the selected crystal as this one.
		}
	}

	private void bO()
	{
		bz = false;

		ILocation dergonLocation = targetWorld.getLocation(locX, locY, locZ);

		if (targetEntity != null && dergonLocation != null && random.nextFloat() < 0.5F)
			targetWorld.spawnFallingBlock(dergonLocation, Item.Unavailable.Fire);

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
				EntityHuman rawChum = ObjectUnwrapper.getMinecraft(unluckyChum);

				if (rawChum != null)
				{
					rawChum.setPassengerOf(this);
					ridingPlayer = rawChum;
					handler.handleDergonMount(ridingPlayer.getName());
				}
			}

			targetEntity = null;
			h = locX + random.nextInt(400) + -200;
			i = random.nextInt(100) + 70; // Somewhere above 70 to prevent floor clipping.
			j = locZ + random.nextInt(400) + -200;
			flyOffLocation = targetWorld.getLocation(h, i, j); // Store the target fly-off location.
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
		h = targetLocation.getX();
		i = targetLocation.getY();
		j = targetLocation.getZ();

		targetEntity = null;
	}

	@Override
	public void e()
	{
		// Throw a player off it's back if we're high up.
		if (ridingPlayer != null && locY >= 90)
		{
			ridingPlayer.setPassengerOf(null);
			ridingPlayer = null;
		}


		// Update the health bar to show the percentage of the dergon
		long pct = Math.round((getHealth() / getMaxHealth()) * 100);
		setCustomName("Dergon (" + pct + "%)");

		float f;
		float f1;

		if (world.isStatic)
		{
			f = MathHelper.cos(by * 3.1415927F * 2.0F);
			f1 = MathHelper.cos(bx * 3.1415927F * 2.0F);
			if (f1 <= -0.3F && f >= -0.3F)
				world.a(locX, locY, locZ, "mob.enderdragon.wings", 5.0F, 0.8F + random.nextFloat() * 0.3F, false);
		}

		bx = by;
		float f2;

		if (getHealth() <= 0.0F) // Check if the dragon is dead.
		{
			// If we're dead, play a random explosion effect at a random offset to it's corpse.
			f = (random.nextFloat() - 0.5F) * 8.0F;
			f1 = (random.nextFloat() - 0.5F) * 4.0F;
			f2 = (random.nextFloat() - 0.5F) * 8.0F;
			world.addParticle("largeexplode", locX + (double) f, locY + 2.0D + (double) f1, locZ + (double) f2, 0.0D, 0.0D, 0.0D);
		}
		else
		{
			this.bN();
			f = 0.2F / (MathHelper.sqrt(motX * motX + motZ * motZ) * 10.0F + 1.0F);
			f *= (float) Math.pow(2.0D, motY);
			by += (bA ? f * 0.5F : f);

			yaw = MathHelper.g(yaw);
			if (bo < 0)
			{
				for (int d05 = 0; d05 < bn.length; ++d05)
				{
					bn[d05][0] = (double) yaw;
					bn[d05][1] = locY;
				}
			}

			if (++bo == bn.length)
				bo = 0;

			bn[bo][0] = (double) yaw;
			bn[bo][1] = locY;
			double d0;
			double d1;
			double d2;
			double d3;
			float f3;

			if (world.isStatic)
			{
				if (bh > 0)
				{
					d0 = locX + (bi - locX) / (double) bh;
					d1 = locY + (bj - locY) / (double) bh;
					d2 = locZ + (bk - locZ) / (double) bh;
					d3 = MathHelper.g(bl - (double) yaw);
					yaw = (float) ((double) yaw + d3 / (double) bh);
					pitch = (float) ((double) pitch + (bm - (double) pitch) / (double) bh);
					--bh;
					setPosition(d0, d1, d2);
					b(yaw, pitch);
				}
			}
			else
			{
				d0 = h - locX;
				d1 = i - locY;
				d2 = j - locZ;
				d3 = d0 * d0 + d1 * d1 + d2 * d2;
				if (targetEntity != null)
				{
					h = targetEntity.locX;
					j = targetEntity.locZ;
					double d4 = h - locX;
					double d5 = j - locZ;
					double d6 = Math.sqrt(d4 * d4 + d5 * d5);
					double d7 = 0.4000000059604645D + d6 / 80.0D - 1.0D;

					if (d7 > 10.0D)
						d7 = 10.0D;

					i = targetEntity.boundingBox.b + d7;
				}
				else
				{
					h += random.nextGaussian() * 2.0D;
					j += random.nextGaussian() * 2.0D;
				}

				if (bz || d3 < 100.0D || d3 > 22500.0D || positionChanged || G)
					bO();

				d1 /= (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
				f3 = 0.6F;
				if (d1 < (double) (-f3))
					d1 = (double) (-f3);

				if (d1 > (double) f3)
					d1 = (double) f3;

				motY += d1 * 0.10000000149011612D;
				yaw = MathHelper.g(yaw);
				double d8 = 180.0D - Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D;
				double d9 = MathHelper.g(d8 - (double) yaw);

				if (d9 > 50.0D)
					d9 = 50.0D;

				if (d9 < -50.0D)
					d9 = -50.0D;

				Vec3D vec3d = world.getVec3DPool().create(h - locX, i - locY, j - locZ).a();
				Vec3D vec3d1 = world.getVec3DPool().create((double) MathHelper.sin(yaw * 3.1415927F / 180.0F), motY, (double) (-MathHelper.cos(yaw * 3.1415927F / 180.0F))).a();
				float f4 = (float) (vec3d1.b(vec3d) + 0.5D) / 1.5F;

				if (f4 < 0.0F)
					f4 = 0.0F;

				bg *= 0.8F;
				float f5 = MathHelper.sqrt(motX * motX + motZ * motZ) * 1.0F + 1.0F;
				double d10 = Math.sqrt(motX * motX + motZ * motZ) * 1.0D + 1.0D;

				if (d10 > 40.0D)
					d10 = 40.0D;

				bg = (float) ((double) bg + d9 * (0.699999988079071D / d10 / (double) f5));
				yaw += bg * 0.1F;
				float f6 = (float) (2.0D / (d10 + 1.0D));
				float f7 = 0.06F;

				a(0.0F, -1.0F, f7 * (f4 * f6 + (1.0F - f6)));
				if (bA)
					move(motX * 0.800000011920929D, motY * 0.800000011920929D, motZ * 0.800000011920929D);
				else
					move(motX, motY, motZ);

				Vec3D vec3d2 = world.getVec3DPool().create(motX, motY, motZ).a();
				float f8 = (float) (vec3d2.b(vec3d1) + 1.0D) / 2.0F;

				f8 = 0.8F + 0.15F * f8;
				motX *= (double) f8;
				motZ *= (double) f8;
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
			f1 = (float) (b(5, 1.0F)[1] - b(10, 1.0F)[1]) * 10.0F / 180.0F * 3.1415927F;
			f2 = MathHelper.cos(f1);
			float f9 = -MathHelper.sin(f1);
			float f10 = yaw * 3.1415927F / 180.0F;
			float f11 = MathHelper.sin(f10);
			float f12 = MathHelper.cos(f10);

			br.h();
			br.setPositionRotation(locX + (double) (f11 * 0.5F), locY, locZ - (double) (f12 * 0.5F), 0.0F, 0.0F);
			bv.h();
			bv.setPositionRotation(locX + (double) (f12 * 4.5F), locY + 2.0D, locZ + (double) (f11 * 4.5F), 0.0F, 0.0F);
			bw.h();
			bw.setPositionRotation(locX - (double) (f12 * 4.5F), locY + 2.0D, locZ - (double) (f11 * 4.5F), 0.0F, 0.0F);

			if (!world.isStatic && hurtTicks == 0)
			{
				a(world.getEntities(this, bv.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				a(world.getEntities(this, bw.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				b(world.getEntities(this, bq.boundingBox.grow(1.0D, 1.0D, 1.0D)));
			}

			double[] adouble = b(5, 1.0F);
			double[] adouble1 = b(0, 1.0F);

			f3 = MathHelper.sin(yaw * 3.1415927F / 180.0F - bg * 0.01F);
			float f13 = MathHelper.cos(yaw * 3.1415927F / 180.0F - bg * 0.01F);

			bq.h();
			bq.setPositionRotation(locX + (double) (f3 * 5.5F * f2), locY + (adouble1[1] - adouble[1]) * 1.0D + (double) (f9 * 5.5F), locZ - (double) (f13 * 5.5F * f2), 0.0F, 0.0F);

			for (int j = 0; j < 3; ++j)
			{
				EntityComplexPart entitycomplexpart = null;

				if (j == 0)
					entitycomplexpart = bs;

				if (j == 1)
					entitycomplexpart = bt;

				if (j == 2)
					entitycomplexpart = bu;

				double[] adouble2 = b(12 + j * 2, 1.0F);
				float f14 = yaw * 3.1415927F / 180.0F + b(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F * 1.0F;
				float f15 = MathHelper.sin(f14);
				float f16 = MathHelper.cos(f14);
				float f17 = 1.5F;
				float f18 = (float) (j + 1) * 2.0F;

				entitycomplexpart.h();
				entitycomplexpart.setPositionRotation(locX - (double) ((f11 * f17 + f15 * f18) * f2), locY + (adouble2[1] - adouble[1]) * 1.0D - (double) ((f18 + f17) * f9) + 1.5D, locZ + (double) ((f12 * f17 + f16 * f18) * f2), 0.0F, 0.0F);
			}

			if (!world.isStatic)
				bA = a(bq.boundingBox) | a(br.boundingBox);
		}
	}

	private void a(List list)
	{
		double d0 = (br.boundingBox.a + br.boundingBox.d) / 2.0D;
		double d1 = (br.boundingBox.c + br.boundingBox.f) / 2.0D;

		for (Object rawEntity : list)
		{
			Entity entity = (Entity) rawEntity;
			if (entity instanceof EntityLiving)
			{
				double d2 = entity.locX - d0;
				double d3 = entity.locZ - d1;
				double d4 = d2 * d2 + d3 * d3;

				entity.g(d2 / d4 * 4.0D, 0.20000000298023224D, d3 / d4 * 4.0D);
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

	@Override
	protected void d(DamageSource source, float f)
	{
		if (ridingPlayer == null || !isRidingPlayer(source.getEntity().getName()))
			super.d(source, handler.handleDergonDamage(source, f));
	}

	@Override
	protected void aF()
	{
		super.aF();
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

	private Entity targetEntity;
	private final DergonHandler handler;
	private final ILocation targetLocation;
	private ILocation flyOffLocation;
	private final IWorld targetWorld;
	private final Random random = new Random();
	private EntityHuman ridingPlayer = null;
}
