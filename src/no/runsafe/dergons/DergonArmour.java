package no.runsafe.dergons;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.meta.RunsafeLeatherArmor;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class DergonArmour
{
	public DergonArmour(IServer server)
	{
		this.server = server;

		RunsafeLeatherArmor head = sort(Item.Combat.Helmet.Leather.getItem());
		head.setDisplayName("Dergonbone Helmet");
		processRecipe(head, "BBB", "BXB", "XXX");

		RunsafeLeatherArmor chest = sort(Item.Combat.Chestplate.Leather.getItem());
		chest.setDisplayName("Dergonbone Chestplate");
		processRecipe(chest, "BXV", "BBB", "BBB");

		RunsafeLeatherArmor legs = sort(Item.Combat.Leggings.Leather.getItem());
		legs.setDisplayName("Dergonbone Leggings");
		processRecipe(legs, "BBB", "BXB", "BXB");

		RunsafeLeatherArmor boots = sort(Item.Combat.Boots.Leather.getItem());
		boots.setDisplayName("Dergonbone Boots");
		processRecipe(boots, "XXX", "BXB", "BXB");

	}

	private RunsafeLeatherArmor sort(RunsafeMeta raw)
	{
		RunsafeLeatherArmor armour = (RunsafeLeatherArmor) raw;
		armour.setColor(219, 219, 211);
		armour.setAmount(1);
		return armour;
	}

	private void processRecipe(RunsafeMeta item, String a, String b, String c)
	{
		ShapedRecipe recipe = new ShapedRecipe(ObjectUnwrapper.convert(item)).shape(a, b, c);
		recipe.setIngredient('B', new MaterialData(Item.Miscellaneous.Bone.getType(), (byte) 42));
		recipe.setIngredient('X', new MaterialData(Item.Unavailable.Air.getType()));
		server.addRecipe(recipe);
	}

	private final IServer server;
}
