package steve_gall.minecolonies_compatibility.core.common.building.module;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;

import steve_gall.minecolonies_compatibility.core.common.network.message.BucketFillingOpenTeachMessage;

public class BucketFillingCraftingModuleView extends CraftingModuleView
{
	public BucketFillingCraftingModuleView()
	{

	}

	@Override
	public void openCraftingGUI()
	{
		ClientPacketDistributor.sendToServer(new BucketFillingOpenTeachMessage(this));
	}

}
