package steve_gall.minecolonies_compatibility.core.common.init;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.core.common.block.CommonNetworkStorageBlock;

public class ModBlocks
{
	public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(Registries.BLOCK, MineColoniesCompatibility.MOD_ID);
	public static final DeferredHolder<Block, CommonNetworkStorageBlock> COMMON_NETWORK_STORAGE = REGISTER.register("common_network_storage", id -> new CommonNetworkStorageBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)).mapColor(MapColor.WOOD).strength(1.0F)));

	private ModBlocks()
	{

	}

}
