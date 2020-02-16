package gigaherz.elementsofpower.spells.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class DustBlock extends Block
{
    public static final IntegerProperty DENSITY = IntegerProperty.create("density", 1, 16);

    public DustBlock(Properties properties)
    {
        super(properties);
        setDefaultState(this.getStateContainer().getBaseState().with(DENSITY, 16));
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        if (state.getBlock() != this)
            return 16;
        return state.get(DENSITY);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);

        rescheduleUpdate(worldIn, pos, worldIn.rand);
    }

    private void rescheduleUpdate(World worldIn, BlockPos pos, Random rand)
    {
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, 12 + rand.nextInt(12));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random)
    {
        // explicitly don't call super
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand)
    {
        int density = state.get(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (Direction f : Direction.values())
        {
            BlockPos bp = pos.offset(f);
            BlockState neighbour = worldIn.getBlockState(bp);
            if (neighbour.getBlock().isAir(neighbour, worldIn, bp)
                    || neighbour.getBlock() == Blocks.FIRE)
            {
                boolean given = false;
                if (density > maxGive)
                {
                    int d = rand.nextInt(maxGive);
                    if (d > 0)
                    {
                        worldIn.setBlockState(bp, getDefaultState().with(DENSITY, d));
                        density -= d;
                        given = true;
                    }
                }

                if (!given)
                    worldIn.setBlockState(bp, Blocks.AIR.getDefaultState());
            }
            else if (neighbour.getBlock() == this)
            {
                if (density > maxGive)
                {
                    int od = neighbour.get(DENSITY);
                    if (od < 16)
                    {
                        int d = rand.nextInt(Math.min(16 - od, maxGive));
                        if (d > 0)
                        {
                            worldIn.setBlockState(bp, getDefaultState().with(DENSITY, od + d));
                            density -= d;
                        }
                    }
                }
            }
        }

        if (density <= 0)
        {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
        else
        {
            worldIn.setBlockState(pos, state.with(DENSITY, density));
        }

        rescheduleUpdate(worldIn, pos, rand);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }

    @Deprecated
    @Override
    public PushReaction getPushReaction(BlockState state)
    {
        return PushReaction.IGNORE;
    }
}
