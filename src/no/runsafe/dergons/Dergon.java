package no.runsafe.dergons;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.minecraft.entity.LivingEntity;

public class Dergon
{
	public void spawn(ILocation location)
	{
		entity = (ILivingEntity) LivingEntity.EnderDragon.spawn(location);
		entity.setCustomName("Dergon");
	}

	public void remove()
	{
		entity.remove();
	}

	private ILivingEntity entity;
}
