package steve_gall.minecolonies_compatibility.module.common.farmersdelight.crafting;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import steve_gall.minecolonies_compatibility.api.common.crafting.SimpleContainerGenericRecipe;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public class CookingGenericRecipe extends SimpleContainerGenericRecipe
{
	public CookingGenericRecipe(RecipeHolder<CookingPotRecipe> holder, HolderLookup.Provider provider)
	{
		super(holder.id().identifier(), steve_gall.minecolonies_compatibility.core.common.crafting.IngredientHelper.getStacksList(holder.value().getIngredients()), Collections.singletonList(holder.value().getOutputContainer()), holder.value().getResultItem());
	}

	public CookingGenericRecipe(Identifier recipeId, List<List<ItemStack>> ingredients, List<ItemStack> container, ItemStack output)
	{
		super(recipeId, ingredients, container, output);
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack)
	{
		if (!steve_gall.minecolonies_compatibility.core.common.item.ItemStackHelper.getCraftingRemainder(stack).isEmpty())
		{
			return steve_gall.minecolonies_compatibility.core.common.item.ItemStackHelper.getCraftingRemainder(stack);
		}
		else if (CookingPotBlockEntity.INGREDIENT_REMAINDER_OVERRIDES.containsKey(stack.getItem()))
		{
			return CookingPotBlockEntity.INGREDIENT_REMAINDER_OVERRIDES.get(stack.getItem()).getDefaultInstance();
		}
		else
		{
			return ItemStack.EMPTY;
		}

	}

	@Override
	public @NotNull Block getIntermediate()
	{
		return ModBlocks.COOKING_POT.get();
	}

}
