package steve_gall.minecolonies_compatibility.module.common.farmersdelight.building.module;

import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import steve_gall.minecolonies_compatibility.module.common.farmersdelight.network.PlatingOpenTeachMessage;

public class PlatingCraftingModuleView extends CraftingModuleView
{
	public PlatingCraftingModuleView()
	{

	}

	@Override
	public void openCraftingGUI()
	{
		ClientPacketDistributor.sendToServer(new PlatingOpenTeachMessage(this));
	}

}
