package no.runsafe.dergons;

import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class DergonItems
{
	public static RunsafeMeta getBones(int amount)
	{
		if (bones == null)
		{
			bones = Item.Miscellaneous.Bone.getItem();
			bones.setDisplayName("Dergon Bones");
			bones.addLore("§3Impressive and heavy bones from the corpse of a Dergon.");
		}
		bones.setAmount(amount);
		return bones;
	}

	public static RunsafeMeta getEgg(int amount)
	{
		if (egg == null)
		{
			egg = Item.Special.DragonEgg.getItem();
			egg.setDisplayName("Dergon Egg");
			egg.addLore("§3A heavy egg that seems to hum with unnatural energy.");
		}
		egg.setAmount(amount);
		return egg;
	}

	private static RunsafeMeta bones;
	private static RunsafeMeta egg;
}
