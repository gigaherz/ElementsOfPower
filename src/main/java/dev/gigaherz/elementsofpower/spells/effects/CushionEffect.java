package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.ElementsOfPowerBlocks;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class CushionEffect extends SpellEffect
{
    @Override
    public int getColor(SpellcastState cast)
    {
        return 0x000000;
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
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.level().getBlockState(blockPos);
        }

        if (currentState.isAir())
        {
            cast.level().setBlockAndUpdate(blockPos, ElementsOfPowerBlocks.CUSHION.get().defaultBlockState().setValue(CushionBlock.DENSITY, 16));
        }
    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(ParticleTypes.SPLASH, hitVec.x, hitVec.y, hitVec.z);
        }
    }
}
