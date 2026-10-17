package steve_gall.minecolonies_compatibility.module.client.farmersdelight;

import java.util.ArrayList;
import java.util.List;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.registry.CraftingType;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import steve_gall.minecolonies_compatibility.core.client.gui.TeachCraftingRecipeScreen;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.FarmersDelightModule;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.crafting.CuttingChanceResult;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.crafting.CuttingRecipeStorage;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.init.ModuleCraftingTypes;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.menu.CuttingTeachMenu;
import steve_gall.minecolonies_tweaks.api.common.crafting.ICustomizedRecipeStorage;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

public class CuttingTeachScreen extends TeachCraftingRecipeScreen<CuttingTeachMenu, RecipeHolder<CuttingBoardRecipe>>
{
	public static final Identifier TEXTURE = MineColoniesCompatibility.rl("textures/gui/farmers_cutting_teach.png");

	public CuttingTeachScreen(CuttingTeachMenu menu, Inventory inventory, Component title)
	{
		super(menu, inventory, title);

	}

	@Override
	public Identifier getTexture()
	{
		return TEXTURE;
	}

	@Override
	public CraftingType getCraftingType()
	{
		return ModuleCraftingTypes.CUTTING.get();
	}

	@Override
	protected ICustomizedRecipeStorage createRecipeStorage(RecipeHolder<CuttingBoardRecipe> recipe, List<ItemStorage> input)
	{
		var results = this.menu.getResults().stream().map(CuttingChanceResult::new).toList();
		return new CuttingRecipeStorage(recipe.id().identifier(), input, results, this.menu.getToolType());
	}

	@Override
	protected void renderSlotTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Slot slot)
	{
		var item = slot.getItem();
		var tooltip = new ArrayList<>(getTooltipFromItem(this.minecraft, item));

		var resultIndex = this.menu.getResultSlots().indexOf(slot);

		if (resultIndex > -1)
		{
			tooltip.addAll(1, FarmersDelightModule.getChanceTooltip(this.menu.getResults().get(resultIndex).chance()));
		}

		graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);

		var results = this.menu.getResults();
		var resultSlots = this.menu.getResultSlots();

		for (int i = 0; i < results.size(); i++)
		{
			var slot = resultSlots.get(i);
			var alt = results.get(i).chance() < 1.0F;
			graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth + (alt ? 18 : 0), 0, 18, 18, 256, 256);
		}

	}

	@Override
	public int getSwitchButtonX()
	{
		return 44;
	}

	@Override
	public int getSwitchButtonY()
	{
		return 16;
	}

}
