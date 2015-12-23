package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.spells.SpellBall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;

public class Lavaball extends BallBase
{
    boolean spawnSourceBlocks = false;

    public Lavaball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void spawnBallParticles(MovingObjectPosition mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            if (spawnSourceBlocks)
            {
                world.setBlockState(blockPos, Blocks.flowing_lava.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 0));
            }
            else
            {
                world.setBlockState(blockPos, Blocks.flowing_lava.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 15));
            }
        }
    }

    @Override
    public int getDamageForce()
    {
        int sub = 0;
        if (world.provider.doesWaterVaporize())
        {
            sub = -1;
        }
        return Math.max(super.getDamageForce() - sub, 0);
    }
}
