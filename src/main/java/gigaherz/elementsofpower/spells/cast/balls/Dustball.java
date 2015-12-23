package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.blocks.BlockDust;
import gigaherz.elementsofpower.spells.SpellBall;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;

public class Dustball extends BallBase
{
    public Dustball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            world.setBlockState(blockPos, ElementsOfPower.dust.getDefaultState().withProperty(BlockDust.DENSITY, 16));
        }
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
}
