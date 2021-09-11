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
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class MistBlock extends Block
{
    public static final IntegerProperty DENSITY = IntegerProperty.create("density", 1, 16);

    public MistBlock(Properties properties)
    {
        super(properties);
         /*super(name, Material.AIR);
        setHardness(0.1F);
        setBlockUnbreakable();
        setSoundType(SoundType.CLOTH);
        setTickRandomly(true);
        setLightOpacity(0);
         */
        setDefaultState(this.getStateContainer().getBaseState().with(DENSITY, 16));
    }

    @Deprecated
    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        if (state.getBlock() != this)
            return 16;
        return state.get(DENSITY) / 4;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand)
    {
        int density = state.get(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (Direction f : Direction.values())
        {
            BlockPos bp = pos.offset(f);
            BlockState neighbour = world.getBlockState(bp);
            if (neighbour.getBlock().isAir(neighbour, world, bp)
                    || neighbour.getBlock() == Blocks.FIRE)
            {
                boolean given = false;
                if (density > maxGive)
                {
                    int d = rand.nextInt(maxGive);
                    if (d > 0)
                    {
                        world.setBlockState(bp, getDefaultState().with(DENSITY, d));
                        density -= d;
                        given = true;
                    }
                }

                if (!given)
                    world.setBlockState(bp, Blocks.AIR.getDefaultState());
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
                            world.setBlockState(bp, getDefaultState().with(DENSITY, od + d));
                            density -= d;
                        }
                    }
                }
            }
        }

        if (density <= 0)
        {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
        else
        {
            world.setBlockState(pos, state.with(DENSITY, density));
        }

        world.getPendingBlockTicks().scheduleTick(pos, this, rand.nextInt(10));
    }

    @Deprecated
    @Override
    public PushReaction getPushReaction(BlockState state)
    {
        return PushReaction.IGNORE;
    }
}
