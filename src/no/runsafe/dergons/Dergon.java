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

	private void bO()
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
			flyOffLocation = targetWorld.getLocation(getDergonX(), getDergonY(), getDergonZ()); // Store the target fly-off location.
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
		long pct = Math.round((getHealth() / getMaxHealth()) * 100);
		setCustomName("Dergon (" + pct + "%)");

		ILocation dergonLocation = targetWorld.getLocation(locX, locY, locZ);
		if (targetEntity != null && dergonLocation != null && random.nextFloat() < 0.2F)
			((RunsafeFallingBlock) targetWorld.spawnFallingBlock(dergonLocation, Item.Unavailable.Fire)).setDropItem(false);

		float f;
		float f1;

		if (world.isClientSide)
		{
			f = MathHelper.cos(bv * 3.1415927F * 2.0F);
			f1 = MathHelper.cos(bu * 3.1415927F * 2.0F);
			if (f1 <= -0.3F && f >= -0.3F)
				world.a(locX, locY, locZ, "mob.enderdragon.wings", 5.0F, 0.8F + random.nextFloat() * 0.3F, false);
		}

		bu = bv;
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
			bv += (bx ? f * 0.5F : f);

			yaw = MathHelper.g(yaw);
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
			double d0;
			double d1;
			double d2;
			double d3;
			float f3;

			if (world.isClientSide)
			{
				if (bc > 0)
				{
					d0 = locX + (bd - locX) / bc;
					d1 = locY + (be - locY) / bc;
					d2 = locZ + (bf - locZ) / bc;
					d3 = MathHelper.g(bg - (double) yaw);
					yaw = (float) ((double) yaw + d3 / bc);
					pitch = (float) ((double) pitch + (bh - (double) pitch) / bc);
					--bc;
					setPosition(d0, d1, d2);
					b(yaw, pitch);
				}
			}
			else
			{
				d0 = getDergonX() - locX;
				d1 = getDergonY() - locY;
				d2 = getDergonZ() - locZ;
				d3 = d0 * d0 + d1 * d1 + d2 * d2;
				if (targetEntity != null)
				{
					setDergonX(targetEntity.locX);
					setDergonZ(targetEntity.locZ);
					double d4 = getDergonX() - locX;
					double d5 = getDergonZ() - locZ;
					double d6 = Math.sqrt(d4 * d4 + d5 * d5);
					double d7 = 0.4000000059604645D + d6 / 80.0D - 1.0D;

					if (d7 > 10.0D)
						d7 = 10.0D;

					setDergonY(targetEntity.getBoundingBox().b + ascendDistance);
				}
				else
				{
					setDergonX(getDergonX() + random.nextGaussian() * 2.0D);
					setDergonZ(getDergonZ() + random.nextGaussian() * 2.0D);
				}

				if (bw || d3 < 100.0D || d3 > 22500.0D || positionChanged || F)
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

				Vec3D vec3d = Vec3D.a(getDergonX() - locX, getDergonY() - locY, getDergonZ() - locZ).a();
				Vec3D vec3d1 = Vec3D.a((double) MathHelper.sin(yaw * 3.1415927F / 180.0F), motY, (double) (-MathHelper.cos(yaw * 3.1415927F / 180.0F))).a();
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

				Vec3D vec3d2 = Vec3D.a(motX, motY, motZ).a();
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
			f1 = (float) (b(5, 1.0F)[1] - b(10, 1.0F)[1]) * 10.0F / 180.0F * 3.1415927F;
			f2 = MathHelper.cos(f1);
			float f9 = -MathHelper.sin(f1);
			float f10 = yaw * 3.1415927F / 180.0F;
			float f11 = MathHelper.sin(f10);
			float f12 = MathHelper.cos(f10);

			dergonBody.h();
			dergonBody.setPositionRotation(locX + (double) (f11 * 0.5F), locY, locZ - (double) (f12 * 0.5F), 0.0F, 0.0F);
			dergonWing0.h();
			dergonWing0.setPositionRotation(locX + (double) (f12 * 4.5F), locY + 2.0D, locZ + (double) (f11 * 4.5F), 0.0F, 0.0F);
			dergonWing1.h();
			dergonWing1.setPositionRotation(locX - (double) (f12 * 4.5F), locY + 2.0D, locZ - (double) (f11 * 4.5F), 0.0F, 0.0F);

			if (!world.isClientSide && hurtTicks == 0)
			{
				a(world.getEntities(this, dergonWing0.getBoundingBox().grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				a(world.getEntities(this, dergonWing1.getBoundingBox().grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D)));
				b(world.getEntities(this, dergonHead.getBoundingBox().grow(1.0D, 1.0D, 1.0D)));
			}

			double[] adouble = b(5, 1.0F);
			double[] adouble1 = b(0, 1.0F);

			f3 = MathHelper.sin(yaw * 3.1415927F / 180.0F - bc * 0.01F);
			float f13 = MathHelper.cos(yaw * 3.1415927F / 180.0F - bc * 0.01F);

			dergonHead.h();
			dergonHead.setPositionRotation(locX + (double) (f3 * 5.5F * f2), locY + (adouble1[1] - adouble[1]) * 1.0D + (double) (f9 * 5.5F), locZ - (double) (f13 * 5.5F * f2), 0.0F, 0.0F);

			for (int j = 0; j < 3; ++j)
			{
				EntityComplexPart entitycomplexpart = null;

				if (j == 0)
					entitycomplexpart = dergonTailSection0;

				if (j == 1)
					entitycomplexpart = dergonTailSection1;

				if (j == 2)
					entitycomplexpart = dergonTailSection2;

				double[] adouble2 = b(12 + j * 2, 1.0F);
				float f14 = yaw * 3.1415927F / 180.0F + b(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F * 1.0F;
				float f15 = MathHelper.sin(f14);
				float f16 = MathHelper.cos(f14);
				float f17 = 1.5F;
				float f18 = (float) (j + 1) * 2.0F;

				entitycomplexpart.h();
				entitycomplexpart.setPositionRotation(locX - (double) ((f11 * f17 + f15 * f18) * f2), locY + (adouble2[1] - adouble[1]) * 1.0D - (double) ((f18 + f17) * f9) + 1.5D, locZ + (double) ((f12 * f17 + f16 * f18) * f2), 0.0F, 0.0F);
			}

			if (!world.isClientSide)
				bx = a(dergonHead.getBoundingBox()) | a(dergonBody.getBoundingBox());
		}
	}

	private void a(List list)
	{
		double d0 = (dergonBody.getBoundingBox().a + dergonBody.getBoundingBox().d) / 2.0D;
		double d1 = (dergonBody.getBoundingBox().c + dergonBody.getBoundingBox().f) / 2.0D;

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
			super.d(source, handler.handleDergonDamage(this, source, f));
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
