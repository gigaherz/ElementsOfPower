package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WaterEffect extends SpellEffect
{
    boolean spawnSourceBlocks;

    public WaterEffect(boolean spawnSourceBlocks)
    {
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x00006F;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 4;
    }

    @Override
    public int getForceModifier(InitializedSpellcast cast)
    {
        return cast.world.dimensionType().ultraWarm() ? -3 : 0;
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
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            Vec3 hitVec = mop.getLocation();
            cast.spawnRandomParticle(ParticleTypes.SPLASH, hitVec.x, hitVec.y, hitVec.z);
        }
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
            if (spawnSourceBlocks)
            {
                cast.world.setBlockAndUpdate(blockPos, Fluids.WATER.defaultFluidState().createLegacyBlock());
            }
            else
            {
                cast.world.setBlockAndUpdate(blockPos, Fluids.FLOWING_WATER.defaultFluidState().setValue(WaterFluid.LEVEL, 8).createLegacyBlock());
            }
        }
    }
}
