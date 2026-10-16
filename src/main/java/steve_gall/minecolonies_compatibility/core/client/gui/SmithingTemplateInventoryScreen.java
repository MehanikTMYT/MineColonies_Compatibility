package steve_gall.minecolonies_compatibility.core.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.core.common.inventory.SmithingTemplateInventoryMenu;
import steve_gall.minecolonies_tweaks.core.client.gui.CloseableContainerScreenExtension;

public class SmithingTemplateInventoryScreen extends AbstractContainerScreen<SmithingTemplateInventoryMenu> implements CloseableContainerScreenExtension
{
	public static final Identifier TEXTURE = MineColoniesCompatibility.rl("textures/gui/smithing_template_inventory.png");

	private Screen parent;

	public SmithingTemplateInventoryScreen(SmithingTemplateInventoryMenu menu, Inventory inventory, Component title)
	{
		super(menu, inventory, title);
	}

	@Override
	public void minecolonies_tweaks$onInit(int leftPos, int topPos, int imageWidth, int imageHeight, addCloseButton addCloseButton)
	{
		addCloseButton.invoke(leftPos + imageWidth - 20, topPos - 5, 20, 20);
	}

	@Override
	public void minecolonies_tweaks$setParent(Screen screen)
	{
		this.parent = screen;
	}

	@Override
	public Screen minecolonies_tweaks$getParent()
	{
		return this.parent;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

}
