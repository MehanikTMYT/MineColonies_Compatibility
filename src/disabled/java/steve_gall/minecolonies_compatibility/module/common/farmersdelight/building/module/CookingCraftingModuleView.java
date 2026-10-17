package steve_gall.minecolonies_compatibility.module.common.farmersdelight.building.module;

import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.network.CookingOpenTeachMessage;

public class CookingCraftingModuleView extends CraftingModuleView
{
	public CookingCraftingModuleView()
	{

	}

	@Override
	public void openCraftingGUI()
	{
		ClientPacketDistributor.sendToServer(new CookingOpenTeachMessage(this));
	}

}
