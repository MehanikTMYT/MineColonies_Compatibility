package steve_gall.minecolonies_compatibility.module.client.create;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import steve_gall.minecolonies_compatibility.core.client.gui.NetworkStorageViewScreenUtils;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.module.common.create.AddressMessage;
import steve_gall.minecolonies_compatibility.module.common.create.CitizenStockKeeperMenu;

public class CitizenStockKeeperScreen extends AbstractContainerScreen<CitizenStockKeeperMenu>
{
	private static final Identifier TEXTURE = MineColoniesCompatibility.rl("textures/gui/citizen_stock_keeper.png");

	private EditBox addressBox;
	private String address;

	public CitizenStockKeeperScreen(CitizenStockKeeperMenu menu, Inventory inventory, Component title)
	{
		super(menu, inventory, title, 176, 147);

		this.titleLabelX = 7;
		this.titleLabelY = 7;
		this.inventoryLabelX = 7;
		this.inventoryLabelY = 53;

		this.address = menu.getBlockEntity().getAddress();
	}

	@Override
	protected void init()
	{
		super.init();

		var blockEntity = this.getMenu().getBlockEntity();
		this.addressBox = new EditBox(this.font, this.leftPos + this.titleLabelX, this.topPos + 35, 120, 15, Component.literal(blockEntity.toString()));
		this.addressBox.setValue(this.address);
		this.addressBox.setHint(CreateLang.translate("gui.stock_keeper.package_adress").style(ChatFormatting.ITALIC).style(ChatFormatting.DARK_GRAY).component());
		this.addRenderableWidget(this.addressBox);
	}

	@Override
	protected void containerTick()
	{
		super.containerTick();

		var newAddress = this.addressBox.getValue();

		if (!this.address.equals(newAddress))
		{
			this.address = newAddress;
			ClientPacketDistributor.sendToServer(new AddressMessage(this.getMenu().getBlockEntity(), newAddress));
		}

	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks)
	{
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

		var blockEntity = this.getMenu().getBlockEntity();
		guiGraphics.text(this.font, NetworkStorageViewScreenUtils.getModuleText(blockEntity.getNetworkStorageView().getLinkedPos()), this.leftPos + 14, this.topPos + 21, 0xFF404040, false);

	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks)
	{
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY)
	{
		super.extractTooltip(guiGraphics, mouseX, mouseY);

		if (!this.addressBox.isFocused() && this.addressBox.isHovered())
		{
			guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(CreateLang.translate("gui.factory_panel.restocker_address").color(AbstractSimiWidget.HEADER_RGB).component(), CreateLang.translate("gui.schedule.lmb_edit").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC).component()), mouseX, mouseY);
		}

	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		var pKeyCode = event.key();

		if (this.getFocused() == this.addressBox && this.addressBox.isFocused())
		{
			if (this.minecraft.options.keyInventory.matches(event))
			{
				return true;
			}
			else if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_KP_ENTER)
			{
				this.addressBox.setFocused(false);
				return true;
			}

		}

		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		var pMouseX = event.x();
		var pMouseY = event.y();
		boolean rmb = event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;

		if (rmb && this.addressBox.isMouseOver(pMouseX, pMouseY))
		{
			this.addressBox.setValue("");
			return true;
		}

		if (this.addressBox.isFocused())
		{
			boolean result = this.addressBox.mouseClicked(event, doubleClick);
			if (this.addressBox.isHovered() || result)
			{
				return result;
			}

			this.addressBox.setFocused(false);
		}

		return super.mouseClicked(event, doubleClick);
	}

}
