package no.runsafe.dergons.recipes;

import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.CustomRecipe;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class DergonboneHelmet extends CustomRecipe
{
	public DergonboneHelmet()
	{
		RunsafeMeta helm = Item.Combat.Helmet.Leather.getItem();
		helm.setAmount(1);
		helm.setDisplayName("Dergonbone Helmet");
		setResult(helm);

		RunsafeMeta bone = Item.Miscellaneous.Bone.getItem();
		bone.setDisplayName("Dergon Bone");

		addIngredient(1, bone);
		addIngredient(2, bone);
		addIngredient(3, bone);
		addIngredient(4, bone);
		addIngredient(6, bone);
	}
}
