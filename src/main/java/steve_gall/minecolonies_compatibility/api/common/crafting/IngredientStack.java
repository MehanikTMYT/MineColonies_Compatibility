package steve_gall.minecolonies_compatibility.api.common.crafting;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientStack
{
	// fleet 26.1.2: Ingredient.EMPTY no longer exists (ingredients cannot be empty) -> null ingredient = empty stack
	public static final IngredientStack EMPTY = new IngredientStack(null, 0);

	public static @NotNull IngredientStack of(@NotNull Ingredient ingredient)
	{
		return of(ingredient, 1);
	}

	public static @NotNull IngredientStack of(@NotNull Ingredient ingredient, int count)
	{
		return new IngredientStack(ingredient, count);
	}

	@Nullable
	private final Ingredient ingredient;
	private final int count;
	private final boolean isEmpty;

	public IngredientStack(@Nullable Ingredient ingredient, int count)
	{
		this.ingredient = ingredient;
		this.count = ingredient == null || ingredient.isEmpty() ? 0 : Math.max(count, 0);
		this.isEmpty = this.count <= 0;
	}

	public boolean testType(@NotNull ItemStack item)
	{
		return this.ingredient != null && this.ingredient.test(item);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.ingredient, this.count);
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof IngredientStack other)
		{
			return Objects.equals(this.ingredient, other.ingredient) && this.count == other.count;
		}

		return false;
	}

	public boolean isEmpty()
	{
		return this.isEmpty;
	}

	/** Null for {@link #EMPTY}. */
	public @Nullable Ingredient ingredient()
	{
		return this.ingredient;
	}

	public int count()
	{
		return this.count;
	}

}
