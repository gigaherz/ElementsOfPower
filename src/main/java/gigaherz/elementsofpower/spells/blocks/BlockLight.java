package gigaherz.elementsofpower.spells.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.BlockRenderType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockLight extends Block
{
    public static final IntegerProperty DENSITY = IntegerProperty.create("density", 1, 16);

    public BlockLight(Properties properties)
    {
        super(properties);

        //this(name, Material.REDSTONE_LIGHT);
        //setHardness(0.1F);
        //setBlockUnbreakable();
        setDefaultState(this.getStateContainer().getBaseState().with(DENSITY, 16));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return Math.min(15,state.get(DENSITY));
    }

    @Override
    public int tickRate(IWorldReader worldIn)
    {
        return 30;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);

        worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
    }

    public void resetCooldown(World worldIn, BlockPos pos, BlockState state, int density)
    {
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
        worldIn.setBlockState(pos, state.with(DENSITY, density));
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand)
    {
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }

    @Deprecated
    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.INVISIBLE;
    }
}
