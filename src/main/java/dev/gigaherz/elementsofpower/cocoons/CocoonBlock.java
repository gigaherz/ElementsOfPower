package dev.gigaherz.elementsofpower.cocoons;

import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CocoonBlock extends Block implements SimpleWaterloggedBlock, EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Element type;

    public CocoonBlock(Element type, Properties properties)
    {
        super(properties);
        this.type = type;
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    public Element getType()
    {
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CocoonTileEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    @Deprecated
    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        switch (state.getValue(FACING))
        {
            case UP:
                return Block.box(3, 7, 3, 13, 16, 13);
            case DOWN:
                return Block.box(3, 0, 3, 13, 9, 13);
            case EAST:
                return Block.box(7, 3, 3, 16, 13, 13);
            case WEST:
                return Block.box(0, 3, 3, 9, 13, 13);
            case SOUTH:
                return Block.box(3, 3, 7, 13, 13, 16);
            case NORTH:
                return Block.box(3, 3, 0, 13, 13, 9);
        }
        return Block.box(3, 0, 3, 13, 9, 13);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Deprecated
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof MagicOrbItem)
        {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (!(te instanceof CocoonTileEntity))
                return InteractionResult.FAIL;

            ((CocoonTileEntity) te).addEssences(heldItem);

            if (!player.getAbilities().instabuild)
                heldItem.shrink(1);

            return InteractionResult.SUCCESS;
        }

        return super.use(state, worldIn, pos, player, hand, rayTraceResult);
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
            super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
