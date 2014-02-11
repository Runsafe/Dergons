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
	}
}
