package no.runsafe.dergons;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.item.meta.RunsafeLeatherArmor;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class DergonArmour implements IInventoryClick
{
	public DergonArmour(IServer server)
	{
		this.server = server;

		RunsafeLeatherArmor head = sort(Item.Combat.Helmet.Leather.getItem());
		head.setDisplayName("Dergonbone Helmet");

		RunsafeLeatherArmor chest = sort(Item.Combat.Chestplate.Leather.getItem());
		chest.setDisplayName("Dergonbone Chestplate");

		RunsafeLeatherArmor legs = sort(Item.Combat.Leggings.Leather.getItem());
		legs.setDisplayName("Dergonbone Leggings");

		RunsafeLeatherArmor boots = sort(Item.Combat.Boots.Leather.getItem());
		boots.setDisplayName("Dergonbone Boots");

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

	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		RunsafeInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.WORKBENCH)
		{
			IPlayer player = event.getWhoClicked();
			for (int i = 1; i < inventory.getSize(); i++)
			{
				RunsafeMeta slotItem = inventory.getItemInSlot(i);
				if (slotItem != null)
					player.sendColouredMessage(i + ": " + slotItem.getNormalName());
			}
		}
	}

	private final IServer server;
}
