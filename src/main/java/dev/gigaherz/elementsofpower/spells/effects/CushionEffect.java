package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.ElementsOfPowerBlocks;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CushionEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 10;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.world.getBlockState(blockPos);
        }

        if (currentState.isAir())
        {
            cast.world.setBlockAndUpdate(blockPos, ElementsOfPowerBlocks.CUSHION.get().defaultBlockState().setValue(CushionBlock.DENSITY, 16));
        }
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(ParticleTypes.SPLASH, hitVec.x, hitVec.y, hitVec.z);
        }
    }
}
