package steve_gall.minecolonies_compatibility.module.common.refinedstorage;

import net.minecraft.resources.Identifier;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import steve_gall.minecolonies_compatibility.module.common.refinedstorage.init.ModuleBlockEntities;

public class CitizenGridBlock extends AbstractDirectionalBlock<OrientedDirection> implements BlockItemProvider<BaseBlockItem>, EntityBlock
{
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	private static final AbstractBlockEntityTicker<CitizenGridBlockEntity> TICKER = new NetworkNodeBlockEntityTicker<>(ModuleBlockEntities.CITIZEN_GRID::get, ACTIVE);

	private final Identifier id;

	public CitizenGridBlock(Identifier id)
	{
		super(BlockProperties.stone(id));
		this.id = id;
	}

	@Override
	protected BlockState getDefaultState()
	{
		return super.getDefaultState().setValue(ACTIVE, false);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE);
	}

	@Override
	public BaseBlockItem createBlockItem()
	{
		return new BaseBlockItem(this.id, this);
	}

	@Override
	protected DirectionType<OrientedDirection> getDirectionType()
	{
		return OrientedDirectionType.INSTANCE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new CitizenGridBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TICKER.get(level, type);
	}

}
