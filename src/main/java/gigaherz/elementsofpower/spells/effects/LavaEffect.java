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

public class LavaEffect extends SpellEffect
{
    boolean spawnSourceBlocks;

    public LavaEffect(boolean spawnSourceBlocks)
    {
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    public int getColor(Spellcast cast)
    {
        return 0x8080FF;
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
    public int getForceModifier(Spellcast cast)
    {
        return cast.world.provider.doesWaterVaporize() ? +1 : 0;
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
            cast.spawnRandomParticle(EnumParticleTypes.LAVA,
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
                cast.world.setBlockState(blockPos, Blocks.flowing_lava.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 0));
            }
            else
            {
                cast.world.setBlockState(blockPos, Blocks.flowing_lava.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 15));
            }
        }
    }
}
