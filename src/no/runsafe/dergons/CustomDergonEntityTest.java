package no.runsafe.dergons;

import net.minecraft.server.v1_7_R1.EntityEnderDragon;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;

public class CustomDergonEntityTest extends EntityEnderDragon
{
	public CustomDergonEntityTest(IWorld world)
	{
		super(ObjectUnwrapper.getMinecraft(world));
	}
}
