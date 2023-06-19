package dev.gigaherz.elementsofpower.spells.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;

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
        registerDefaultState(this.getStateDefinition().any().setValue(DENSITY, 16));
    }

    @Deprecated
    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        if (state.getBlock() != this)
            return 16;
        return state.getValue(DENSITY) / 4;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand)
    {
        int density = state.getValue(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (Direction f : Direction.values())
        {
            BlockPos bp = pos.relative(f);
            BlockState neighbour = world.getBlockState(bp);
            if (neighbour.isAir()
                    || neighbour.getBlock() == Blocks.FIRE)
            {
                boolean given = false;
                if (density > maxGive)
                {
                    int d = rand.nextInt(maxGive);
                    if (d > 0)
                    {
                        world.setBlockAndUpdate(bp, defaultBlockState().setValue(DENSITY, d));
                        density -= d;
                        given = true;
                    }
                }

                if (!given)
                    world.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
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
                            world.setBlockAndUpdate(bp, defaultBlockState().setValue(DENSITY, od + d));
                            density -= d;
                        }
                    }
                }
            }
        }

        if (density <= 0)
        {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        else
        {
            world.setBlockAndUpdate(pos, state.setValue(DENSITY, density));
        }

        world.scheduleTick(pos, this, rand.nextInt(10));
    }

    @Deprecated
    @Override
    public PushReaction getPistonPushReaction(BlockState state)
    {
        return PushReaction.IGNORE;
    }
}
