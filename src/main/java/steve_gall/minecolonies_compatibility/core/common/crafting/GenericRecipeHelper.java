package steve_gall.minecolonies_compatibility.core.common.crafting;

import steve_gall.minecolonies_compatibility.core.common.item.ItemStackHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.ItemStack;

public class GenericRecipeHelper
{
	public static List<ItemStack> getAdditionalOutputs(List<List<ItemStack>> inputs)
	{
		return getAdditionalOutputs(inputs, ItemStackHelper::getCraftingRemainder);
	}

	public static List<ItemStack> getAdditionalOutputs(List<List<ItemStack>> inputs, Function<ItemStack, ItemStack> getCraftingRemainingStack)
	{
		var list = new ArrayList<ItemStack>();

		for (var input : inputs)
		{
			var allRemaining = ItemStack.EMPTY;

			for (var stack : input)
			{
				var remain = getCraftingRemainingStack.apply(stack);

				if (remain.isEmpty() || (!allRemaining.isEmpty() && !ItemStack.matches(allRemaining, remain)))
				{
					allRemaining = ItemStack.EMPTY;
					break;
				}
				else
				{
					allRemaining = remain;
				}

			}

			if (!allRemaining.isEmpty())
			{
				list.add(allRemaining);
			}

		}

		return list;
	}

	private GenericRecipeHelper()
	{

	}

}
