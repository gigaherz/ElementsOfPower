package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.blocks.BlockMist;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class MistEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 10;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3 hitVec)
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
            cast.world.setBlockState(blockPos, ElementsOfPower.mist.getDefaultState().withProperty(BlockMist.DENSITY, 16));
        }
    }
}
