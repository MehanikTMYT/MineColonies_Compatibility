package steve_gall.minecolonies_compatibility.module.client.jei;

import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.module.common.OptionalModule;

public abstract class AbstractModulePlugin implements IModPlugin
{
	@Override
	public ResourceLocation getPluginUid()
	{
		return MineColoniesCompatibility.rl(this.getModule().getModId());
	}

	public boolean isLoaded()
	{
		return this.getModule().isLoaded();
	}

	public abstract OptionalModule<?> getModule();
}
