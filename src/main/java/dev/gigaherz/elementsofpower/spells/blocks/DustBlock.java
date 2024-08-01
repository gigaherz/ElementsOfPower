package dev.gigaherz.elementsofpower.spells.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;

public class DustBlock extends Block
{
    public static final IntegerProperty DENSITY = SpellBlockProperties.DENSITY;

    public DustBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(DENSITY, 16));
    }

    @Deprecated
    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        if (state.getBlock() != this)
            return 16;
        return state.getValue(DENSITY);
    }

    @Deprecated
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, worldIn, pos, oldState, isMoving);

        rescheduleUpdate(worldIn, pos, worldIn.random);
    }

    private void rescheduleUpdate(Level worldIn, BlockPos pos, RandomSource rand)
    {
        worldIn.scheduleTick(pos, this, 12 + rand.nextInt(12));
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
    {
        // explicitly don't call super
    }

    @Deprecated
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
    {
        int density = state.getValue(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (Direction f : Direction.values())
        {
            BlockPos bp = pos.relative(f);
            BlockState neighbour = worldIn.getBlockState(bp);
            if (neighbour.isAir()
                    || neighbour.getBlock() == Blocks.FIRE)
            {
                boolean given = false;
                if (density > maxGive)
                {
                    int d = rand.nextInt(maxGive);
                    if (d > 0)
                    {
                        worldIn.setBlockAndUpdate(bp, defaultBlockState().setValue(DENSITY, d));
                        density -= d;
                        given = true;
                    }
                }

                if (!given)
                    worldIn.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            }
            else if (neighbour.getBlock() == this)
            {
                if (density > maxGive)
                {
                    int od = neighbour.getValue(DENSITY);
                    if (od < 16)
                    {
                        int d = rand.nextInt(Math.min(16 - od, maxGive));
                        if (d > 0)
                        {
                            worldIn.setBlockAndUpdate(bp, defaultBlockState().setValue(DENSITY, od + d));
                            density -= d;
                        }
                    }
                }
            }
        }

        if (density <= 0)
        {
            worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        else
        {
            worldIn.setBlockAndUpdate(pos, state.setValue(DENSITY, density));
        }

        rescheduleUpdate(worldIn, pos, rand);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }
}
