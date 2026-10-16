package steve_gall.minecolonies_compatibility.api.common.inventory;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.gamerules.GameRules;
import com.minecolonies.api.crafting.VanillaRecipeUtils;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeLookup;
import net.minecraft.world.level.Level;
import steve_gall.minecolonies_compatibility.core.common.item.ItemStackHelper;

public abstract class MenuRecipeValidatorRecipe<RECIPE extends Recipe<RECIPE_INPUT>, RECIPE_INPUT extends RecipeInput> implements IMenuRecipeValidator<RecipeHolder<RECIPE>, RECIPE_INPUT>
{
	public static final String TAG_ID = "id";

	@NotNull
	protected final Level level;

	public MenuRecipeValidatorRecipe(@NotNull Level level)
	{
		this.level = level;
	}

	@Override
	public List<RecipeHolder<RECIPE>> findAll(Container container, ServerPlayer player)
	{
		return RecipeLookup.byType(this.level, this.getRecipeType()).stream().filter(recipeHolder ->
		{
			var recipe = recipeHolder.value();

			if (this.test(recipeHolder, container, player))
			{
				return recipe.isSpecial() || !player.level().getGameRules().get(GameRules.LIMITED_CRAFTING) || player.getRecipeBook().contains(recipeHolder.id()) || player.isCreative();
			}

			return false;
		}).toList();
	}

	protected boolean test(RecipeHolder<RECIPE> recipeHolder, Container container, ServerPlayer player)
	{
		var input = this.getInput(container, recipeHolder);
		return recipeHolder.value().matches(input, player.level());
	}

	protected boolean matchesWithIngredientsCount(RECIPE recipe, RECIPE_INPUT input)
	{
		if (!recipe.matches(input, this.level))
		{
			return false;
		}

		var ingredientsSize = recipe.placementInfo().ingredients().size();
		var inputsSize = ItemStackHelper.filterNotEmpty(RecipeInputHelper.getItemStacks(input)).size();
		return ingredientsSize == inputsSize;
	}

	@Override
	public CompoundTag serialize(HolderLookup.Provider provider, IFactoryController controller, RecipeHolder<RECIPE> recipeHolder)
	{
		var tag = new CompoundTag();
		tag.putString(TAG_ID, recipeHolder.id().identifier().toString());
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecipeHolder<RECIPE> deserialize(HolderLookup.Provider provider, IFactoryController controller, CompoundTag tag)
	{
		var recipeId = Identifier.parse(tag.getStringOr(TAG_ID, ""));
		return (RecipeHolder<RECIPE>) RecipeLookup.byKey(this.level, recipeId).orElse(null);
	}

	@Override
	public ItemStack getResultItem(RecipeHolder<RECIPE> recipe, HolderLookup.Provider provider)
	{
		return VanillaRecipeUtils.getResultItem(recipe.value(), this.level);
	}

	@Override
	public Identifier getRecipeId(RecipeHolder<RECIPE> recipe)
	{
		return recipe.id().identifier();
	}

	public abstract RecipeType<RECIPE> getRecipeType();
}
