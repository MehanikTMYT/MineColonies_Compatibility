package steve_gall.minecolonies_compatibility.core.common.building.module;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;

import steve_gall.minecolonies_compatibility.core.common.network.message.SmithingOpenTeachMessage;

public class SmithingCraftingModuleView extends CraftingModuleView
{
	public SmithingCraftingModuleView()
	{

	}

	@Override
	public void openCraftingGUI()
	{
		ClientPacketDistributor.sendToServer(new SmithingOpenTeachMessage(this));
	}

}
