package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class LavaEffect extends SpellEffect
{
    boolean spawnSourceBlocks;

    public LavaEffect(boolean spawnSourceBlocks)
    {
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x8080FF;
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
    public int getForceModifier(InitializedSpellcast cast)
    {
        return cast.level.dimensionType().ultraWarm() ? +1 : 0;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            Vec3 hitVec = mop.getLocation();
            cast.spawnRandomParticle(ParticleTypes.LAVA, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.level.getBlockState(blockPos);
        }

        if (currentState.isAir())
        {
            if (spawnSourceBlocks)
            {
                cast.level.setBlockAndUpdate(blockPos, Fluids.LAVA.defaultFluidState().createLegacyBlock());
            }
            else
            {
                cast.level.setBlockAndUpdate(blockPos, Fluids.FLOWING_LAVA.defaultFluidState().setValue(LavaFluid.LEVEL, 8).createLegacyBlock());
            }
        }
    }
}
