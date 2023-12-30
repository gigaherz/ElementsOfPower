package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class LavaEffect extends SpellEffect
{
    boolean spawnSourceBlocks;

    public LavaEffect(boolean spawnSourceBlocks)
    {
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    public int getColor(SpellcastState cast)
    {
        return 0x8080FF;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 10;
    }

    @Override
    public int getForceModifier(SpellcastState cast)
    {
        return cast.level().dimensionType().ultraWarm() ? +1 : 0;
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            Vec3 hitVec = mop.getLocation();
            cast.spawnRandomParticle(ParticleTypes.LAVA, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.level().getBlockState(blockPos);
        }

        if (currentState.isAir())
        {
            if (spawnSourceBlocks)
            {
                cast.level().setBlockAndUpdate(blockPos, Fluids.LAVA.defaultFluidState().createLegacyBlock());
            }
            else
            {
                cast.level().setBlockAndUpdate(blockPos, Fluids.FLOWING_LAVA.defaultFluidState().setValue(LavaFluid.LEVEL, 8).createLegacyBlock());
            }
        }
    }
}
