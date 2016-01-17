package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class WaterEffect extends SpellEffect
{
    boolean spawnSourceBlocks;

    public WaterEffect(boolean spawnSourceBlocks)
    {
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    public int getColor(Spellcast cast)
    {
        return 0xFF0000;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 4;
    }

    @Override
    public int getForceModifier(Spellcast cast)
    {
        return cast.world.provider.doesWaterVaporize() ? -3 : 0;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.WATER_SPLASH,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, MovingObjectPosition mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            if (spawnSourceBlocks)
            {
                cast.world.setBlockState(blockPos, Blocks.flowing_water.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 0));
            }
            else
            {
                cast.world.setBlockState(blockPos, Blocks.flowing_water.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 15));
            }
        }
    }
}
