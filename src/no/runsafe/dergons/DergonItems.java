package no.runsafe.dergons;

import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.List;

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

	public static RunsafeMeta getDergonHead(int amount)
	{
		if (dergonHead == null)
		{
			dergonHead = Item.Decoration.Head.Dragon.getItem();
			dergonHead.setDisplayName("Dergon Head");
			dergonHead.addLore("§3The head of a mighty Dergon.");
		}
		dergonHead.setAmount(amount);
		return dergonHead;
	}

	public static boolean isEgg(RunsafeMeta item)
	{
		if (!item.is(Item.Special.DragonEgg))
			return false;

		String displayName = item.getDisplayName();
		if (displayName == null || !displayName.equals("Dergon Egg"))
			return false;

		List<String> lore = item.getLore();
		return lore != null && !lore.isEmpty() && lore.get(0).equals("§3A heavy egg that seems to hum with unnatural energy.");
	}

	public static boolean isDergonHead(RunsafeMeta item)
	{
		if (!item.is(Item.Decoration.Head.Dragon))
			return false;

		String displayName = item.getDisplayName();
		if (displayName == null || !displayName.equals("Dergon Head"))
			return false;

		List<String> lore = item.getLore();
		return lore != null && !lore.isEmpty() && lore.get(0).equals("§3The head of a mighty Dergon.");
	}

	private static RunsafeMeta bones;
	private static RunsafeMeta egg;
	private static RunsafeMeta dergonHead;
}
