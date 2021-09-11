package gigaherz.elementsofpower.spells.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        setDefaultState(this.getStateContainer().getBaseState().with(DENSITY, 16));
    }

    @Deprecated
    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return 0;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return Math.min(15, state.get(DENSITY) * 4);
    }

    public int tickRate(IWorldReader worldIn)
    {
        return 200;
    }

    @Deprecated
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

    @Deprecated
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

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(16) <= (3+stateIn.get(DENSITY)))
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
