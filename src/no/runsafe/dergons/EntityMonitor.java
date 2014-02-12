package no.runsafe.dergons;

import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;

public class EntityMonitor implements IEntityDamageByEntityEvent
{
	public EntityMonitor(DergonHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		RunsafeEntity entity = event.getEntity();
		if (entity.getEntityType() == LivingEntity.EnderDragon)
		{
			RunsafeEntity attacker = event.getDamageActor();
			if (attacker.getEntityType() == ProjectileEntity.Arrow)
			{
				for (Dergon dergon : handler.getDergons())
				{
					if (dergon.isDergon(entity))
					{
						attacker.remove();
						dergon.getDragon().damage(5.0D);
						event.cancel();
					}
				}
			}
		}
	}

	private final DergonHandler handler;
}
