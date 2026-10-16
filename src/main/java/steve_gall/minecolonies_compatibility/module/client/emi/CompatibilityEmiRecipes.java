package steve_gall.minecolonies_compatibility.module.client.emi;

import java.util.ArrayList;
import java.util.List;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.constant.TranslationConstants;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import steve_gall.minecolonies_compatibility.api.common.butcher.ButcherableIconCache;
import steve_gall.minecolonies_compatibility.api.common.crafting.ToolOrIngredientStack;
import steve_gall.minecolonies_compatibility.api.common.plant.FruitIconCache;

/**
 * EMI displays of the former JEI categories (fleet 26.1.2). Layout follows MineColonies' own EMI port (slot rows and an
 * arrow, tools as catalysts) instead of the JEI citizen-model background.
 */
final class CompatibilityEmiRecipes
{
	private static final int SLOT = 18;

	private CompatibilityEmiRecipes()
	{
	}

	static EmiIngredient stacks(List<ItemStack> stacks)
	{
		var list = new ArrayList<EmiStack>();

		for (var stack : stacks)
		{
			if (!stack.isEmpty())
			{
				list.add(EmiStack.of(stack));
			}

		}

		return list.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(list);
	}

	/** Every known item of an equipment type, weakest first (the JEI categories' tool slot). */
	static EmiIngredient equipment(EquipmentTypeEntry type)
	{
		var stacks = IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream()
				.filter(type::checkIsEquipment)
				.sorted(java.util.Comparator.comparing(type::getMiningLevel))
				.map(EmiStack::of)
				.toList();
		return stacks.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(stacks);
	}

	/** One global research: requirements (buildings / prior research) and the item cost. */
	static final class Research extends BasicEmiRecipe
	{
		private final ResearchCache cache;
		private final List<EmiIngredient> requirements = new ArrayList<>();

		Research(EmiRecipeCategory category, Identifier id, ResearchCache cache)
		{
			super(category, id, 9 * SLOT + 2, 22 + SLOT * Math.max(1, rows(cache.requirements.size())) + 4 + SLOT * Math.max(1, rows(cache.costs.size())));
			this.cache = cache;

			for (var tuple : cache.requirements)
			{
				this.requirements.add(stacks(tuple.getB()));
			}

			for (var cost : cache.costs)
			{
				this.inputs.add(stacks(cost));
			}

			this.catalysts.addAll(this.requirements);
		}

		private static int rows(int count)
		{
			return (count + 8) / 9;
		}

		@Override
		public void addWidgets(WidgetHolder widgets)
		{
			widgets.addText(this.cache.header, 0, 0, 0xFF404040, false);
			widgets.addText(this.cache.name, 0, 10, 0xFF404040, false);
			widgets.addTooltipText(this.cache.tooltip, 0, 0, this.width, 20);

			var y = 22;

			for (var i = 0; i < this.requirements.size(); i++)
			{
				var description = this.cache.requirements.get(i).getA().getDesc();
				widgets.addSlot(this.requirements.get(i), SLOT * (i % 9), y + SLOT * (i / 9)).catalyst(true)
						.appendTooltip(description);
			}

			y += SLOT * Math.max(1, rows(this.requirements.size())) + 4;

			for (var i = 0; i < this.inputs.size(); i++)
			{
				widgets.addSlot(this.inputs.get(i), SLOT * (i % 9), y + SLOT * (i / 9));
			}

		}

		@Override
		public boolean supportsRecipeTree()
		{
			return false;
		}

	}

	/** Orchardist: fruit plant (block icons) + harvest tool -> fruit items. */
	static final class Fruit extends BasicEmiRecipe
	{
		private final EmiIngredient tool;

		Fruit(EmiRecipeCategory category, Identifier id, FruitIconCache cache)
		{
			super(category, id, 24 + 4 * SLOT + 30 + 4 * SLOT, SLOT * 2);

			for (var block : cache.getBlockIcons())
			{
				this.inputs.add(EmiStack.of(block));
			}

			for (var item : cache.getItemIcons())
			{
				this.outputs.add(EmiStack.of(item));
			}

			var harvestTool = cache.getFruit().getHarvestToolType();
			this.tool = harvestTool != null ? equipment(harvestTool) : EmiStack.EMPTY;

			if (!this.tool.isEmpty())
			{
				this.catalysts.add(this.tool);
			}

		}

		@Override
		public void addWidgets(WidgetHolder widgets)
		{
			widgets.addSlot(this.tool, 0, 0).catalyst(true);

			for (var i = 0; i < this.inputs.size() && i < 8; i++)
			{
				widgets.addSlot(this.inputs.get(i), 24 + SLOT * (i % 4), SLOT * (i / 4));
			}

			var arrowX = 24 + 4 * SLOT + 3;
			widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, SLOT / 2);

			for (var i = 0; i < this.outputs.size() && i < 8; i++)
			{
				widgets.addSlot(this.outputs.get(i), arrowX + 27 + SLOT * (i % 4), SLOT * (i / 4)).recipeContext(this);
			}

		}

	}

	/** Butcher: butcherable item(s) + tools / table -> products (up to 12 slots, cycling like the JEI category). */
	static final class Butcher extends BasicEmiRecipe
	{
		private static final int COLS = 6;
		private final List<EmiIngredient> tools = new ArrayList<>();
		private final List<EmiIngredient> products = new ArrayList<>();
		private final List<Component> tableTips = new ArrayList<>();

		Butcher(EmiRecipeCategory category, Identifier id, ButcherableIconCache cache)
		{
			super(category, id, COLS * SLOT, SLOT * 2 + 4 + SLOT * 2);

			this.inputs.add(stacks(cache.getItemIcons()));

			for (var tool : cache.getButcherable().getToolsForIcon())
			{
				if (tool.isEmpty())
				{
					continue;
				}

				this.tools.add(tool.isToolType() ? equipment(tool.toolType()) : ingredient(tool));
			}

			for (var table : cache.getTableIcons())
			{
				this.tools.add(EmiStack.of(table.getBlock()));
				this.tableTips.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "intermediate.tip", table.getBlock().getName()));
			}

			this.catalysts.addAll(this.tools);

			// JEI spread the outputs over 12 slots, cycling when there are more: same here.
			var outputs = cache.getOutputIcons();
			var slots = Math.min(12, Math.max(1, outputs.size()));

			for (var s = 0; s < slots; s++)
			{
				var cycle = new ArrayList<EmiIngredient>();

				for (var i = s; i < outputs.size(); i += slots)
				{
					cycle.add(EmiIngredient.of(outputs.get(i)));
				}

				if (!cycle.isEmpty())
				{
					this.products.add(cycle.size() == 1 ? cycle.get(0) : EmiIngredient.of(cycle));
				}

			}

			for (var product : this.products)
			{
				this.outputs.addAll(product.getEmiStacks());
			}

		}

		private static EmiIngredient ingredient(ToolOrIngredientStack tool)
		{
			Ingredient ingredient = tool.stack().ingredient();
			return ingredient != null ? EmiIngredient.of(ingredient) : EmiStack.EMPTY;
		}

		@Override
		public void addWidgets(WidgetHolder widgets)
		{
			widgets.addSlot(this.inputs.get(0), 0, SLOT / 2);
			widgets.addTexture(EmiTexture.EMPTY_ARROW, SLOT + 4, SLOT / 2 + 1);

			for (var i = 0; i < this.tools.size(); i++)
			{
				var slot = widgets.addSlot(this.tools.get(i), this.width - SLOT * (this.tools.size() - i), 0).catalyst(true);
				var tableIndex = i - (this.tools.size() - this.tableTips.size());

				if (tableIndex >= 0)
				{
					slot.appendTooltip(this.tableTips.get(tableIndex));
				}

			}

			var y = SLOT * 2 + 4;

			for (var i = 0; i < this.products.size(); i++)
			{
				widgets.addSlot(this.products.get(i), SLOT * (i % COLS), y + SLOT * (i / COLS)).recipeContext(this);
			}

		}

	}

}
