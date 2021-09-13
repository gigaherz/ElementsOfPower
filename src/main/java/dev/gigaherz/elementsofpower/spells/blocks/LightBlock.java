package dev.gigaherz.elementsofpower.spells.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Random;

public class LightBlock extends Block
{
    public static final IntegerProperty DENSITY = IntegerProperty.create("density", 1, 16);

    public LightBlock(Properties properties)
    {
        super(properties);

        //this(name, Material.REDSTONE_LIGHT);
        //setHardness(0.1F);
        //setBlockUnbreakable();
        registerDefaultState(this.getStateDefinition().any().setValue(DENSITY, 16));
    }

    @Deprecated
    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        return 0;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        return Math.min(15, state.getValue(DENSITY) * 4);
    }

    public int tickRate(LevelReader worldIn)
    {
        return 200;
    }

    @Deprecated
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, worldIn, pos, oldState, isMoving);

        worldIn.getBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
    }

    public void resetCooldown(Level worldIn, BlockPos pos, BlockState state, int density)
    {
        worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
        worldIn.setBlockAndUpdate(pos, state.setValue(DENSITY, density));
    }

    @Deprecated
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand)
    {
        worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DENSITY);
    }

    @Deprecated
    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand)
    {
        if (rand.nextInt(16) <= (3 + stateIn.getValue(DENSITY)))
        {
            double theta = rand.nextDouble() * 2.0 * Math.PI;
            double phi = rand.nextDouble() * Math.PI;
            double r = rand.nextDouble() * 0.5;
            double dx = r * Math.sin(phi) * Math.cos(theta);
            double dy = r * Math.sin(phi) * Math.sin(theta);
            double dz = r * Math.cos(phi);
            worldIn.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + dx, pos.getY() + 0.5 + dy, pos.getZ() + 0.5 + dz, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
        }
    }
}
