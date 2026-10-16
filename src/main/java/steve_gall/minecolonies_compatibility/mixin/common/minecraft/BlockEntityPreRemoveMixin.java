package steve_gall.minecolonies_compatibility.mixin.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import steve_gall.minecolonies_compatibility.core.common.block.entity.INetworkStorageViewHolder;

/**
 * fleet 26.1.2: was an @Inject into BlockBehaviour#onRemove (removed in 26.1). BlockEntity#preRemoveSideEffects runs on
 * the server exactly when the block at the position changes and the block entity is about to be removed.
 */
@Mixin(value = BlockEntity.class, remap = true)
public abstract class BlockEntityPreRemoveMixin
{
	@Inject(method = "preRemoveSideEffects", at = @At(value = "HEAD"))
	private void minecolonies_compatibility$preRemoveSideEffects(BlockPos pos, BlockState state, CallbackInfo ci)
	{
		if ((Object) this instanceof INetworkStorageViewHolder viewHolder)
		{
			viewHolder.getNetworkStorageView().unlink();
		}

	}

}
