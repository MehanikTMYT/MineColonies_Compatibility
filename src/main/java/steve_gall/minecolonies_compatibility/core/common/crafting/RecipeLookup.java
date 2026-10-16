package steve_gall.minecolonies_compatibility.core.common.crafting;

import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.context.ContextMap;
import net.minecraft.core.HolderLookup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.minecolonies.core.client.recipe.ClientRecipeCache;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * fleet 26.1.2: {@code Level.getRecipeManager()} is gone and clients no longer receive every recipe. Server code reads
 * {@link ServerLevel#recipeAccess()}; client code reads the snapshot MineColonies keeps from {@code RecipesReceivedEvent}
 * ({@link ClientRecipeCache}), which also holds the extra types requested in {@link #onDatapackSync}.
 */
public final class RecipeLookup
{
	private RecipeLookup()
	{
	}

	public static RecipeMap recipes(Level level)
	{
		if (level instanceof ServerLevel serverLevel)
		{
			return serverLevel.recipeAccess().recipeMap();
		}

		return ClientRecipeCache.getRecipes();
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(Level level, RecipeType<T> type)
	{
		return recipes(level).byType(type);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> listByType(Level level, RecipeType<T> type)
	{
		return List.copyOf(byType(level, type));
	}

	public static Optional<RecipeHolder<?>> byKey(Level level, Identifier id)
	{
		return Optional.ofNullable(recipes(level).byKey(ResourceKey.create(Registries.RECIPE, id)));
	}

	public static Optional<RecipeHolder<?>> byKey(Level level, ResourceKey<Recipe<?>> key)
	{
		return Optional.ofNullable(recipes(level).byKey(key));
	}

	/** Recipe types the teaching screens need on the client (crafting/smelting are already sent by MineColonies). */
	public static void onDatapackSync(OnDatapackSyncEvent event)
	{
		event.sendRecipes(RecipeType.SMITHING, RecipeType.STONECUTTING);
	}
	/** Result a recipe shows to recipe books/viewers (26.1: Recipe#getResultItem is gone). */
	public static ItemStack resultItem(Recipe<?> recipe, HolderLookup.Provider provider)
	{
		var displays = recipe.display();

		if (displays.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		var context = new ContextMap.Builder().withParameter(SlotDisplayContext.REGISTRIES, provider).create(SlotDisplayContext.CONTEXT);
		return displays.getFirst().result().resolveForFirstStack(context);
	}

	/** Ingredients in placement order (26.1: Recipe#getIngredients is gone). */
	public static List<Ingredient> ingredients(Recipe<?> recipe)
	{
		return recipe.placementInfo().ingredients();
	}
}
