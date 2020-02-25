package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.entities.EssenceEntity;
import gigaherz.elementsofpower.items.MagicOrbItem;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CocoonBlock extends Block implements IWaterLoggable
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Element type;

    public CocoonBlock(Element type, Properties properties)
    {
        super(properties);
        this.type = type;
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    public Element getType()
    {
        return type;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new CocoonTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public IFluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getDefaultState() : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        switch(state.get(FACING))
        {
            case UP: return Block.makeCuboidShape(3,7, 3, 13, 16, 13);
            case DOWN: return Block.makeCuboidShape(3,0, 3, 13, 9, 13);
            case EAST: return Block.makeCuboidShape(7,3, 3, 16, 13, 13);
            case WEST: return Block.makeCuboidShape(0,3, 3, 9, 13, 13);
            case SOUTH: return Block.makeCuboidShape(3,3, 7, 13, 13, 16);
            case NORTH: return Block.makeCuboidShape(3,3, 0, 13, 13, 9);
        }
        return Block.makeCuboidShape(3,0, 3, 13, 9, 13);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        IFluidState fluid = context.getWorld().getFluidState(context.getPos());
        return getDefaultState().with(FACING, context.getFace().getOpposite()).with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random)
    {
        if (!worldIn.isRemote)
        {
            CocoonTileEntity te = (CocoonTileEntity) worldIn.getTileEntity(pos);

            assert te != null;

            if (!te.essenceContained.isEmpty())
            {
                MagicAmounts am = te.essenceContained;
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    am = am.with(i, (float) Math.floor(am.get(i) * random.nextFloat()));
                }

                if (!am.isEmpty())
                {
                    EssenceEntity e = new EssenceEntity(worldIn, am);

                    BlockPos p = pos.offset(worldIn.getBlockState(pos).get(FACING).getOpposite());

                    e.setLocationAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);

                    worldIn.addEntity(e);
                }
            }
        }

        super.randomTick(state, worldIn, pos, random);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        ItemStack heldItem = player.getHeldItem(hand);

        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof MagicOrbItem)
        {
            TileEntity te = worldIn.getTileEntity(pos);

            if (!(te instanceof CocoonTileEntity))
                return ActionResultType.FAIL;

            ((CocoonTileEntity) te).addEssences(heldItem);

            if (!player.abilities.isCreativeMode)
                heldItem.shrink(1);

            return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, worldIn, pos, player, hand, rayTraceResult);
    }

}
