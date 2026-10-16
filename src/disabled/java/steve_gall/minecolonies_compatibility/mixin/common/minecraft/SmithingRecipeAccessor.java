package steve_gall.minecolonies_compatibility.core.common.crafting;

import net.minecraft.world.item.crafting.Ingredient;

public interface SmithingRecipeAccessor
{
	Ingredient getTemplate();

	Ingredient getBase();

	Ingredient getAddition();
}
