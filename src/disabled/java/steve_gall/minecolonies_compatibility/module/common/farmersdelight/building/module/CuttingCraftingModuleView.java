package steve_gall.minecolonies_compatibility.module.common.farmersdelight.building.module;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.network.CuttingOpenTeachMessage;

public class CuttingCraftingModuleView extends CraftingModuleView
{
	private EquipmentTypeEntry toolType;

	public CuttingCraftingModuleView()
	{
		this.toolType = ModEquipmentTypes.none.get();
	}

	@Override
	public void deserialize(RegistryFriendlyByteBuf buf)
	{
		super.deserialize(buf);

		this.toolType = IMinecoloniesAPI.getInstance().getEquipmentTypeRegistry().getValue(buf.readIdentifier());
	}

	@Override
	public void openCraftingGUI()
	{
		ClientPacketDistributor.sendToServer(new CuttingOpenTeachMessage(this, this.getToolType()));
	}

	public EquipmentTypeEntry getToolType()
	{
		return this.toolType;
	}

}
