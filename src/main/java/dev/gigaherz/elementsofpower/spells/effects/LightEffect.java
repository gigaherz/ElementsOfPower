package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.ElementsOfPowerBlocks;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class LightEffect extends SpellEffect
{
    @Override
    public int getColor(SpellcastState cast)
    {
        return 0xFFFFFF;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 5;
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
            cast.spawnRandomParticle(ParticleTypes.CRIT, hitVec.x, hitVec.y, hitVec.z);
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

        int density = Mth.clamp((int) (16 - 16 * r), 1, 16);

        Block block = currentState.getBlock();

        if (currentState.isAir())
        {
            cast.level().setBlockAndUpdate(blockPos, ElementsOfPowerBlocks.LIGHT.get().defaultBlockState().setValue(LightBlock.DENSITY, density));
        }
        else if (block == ElementsOfPowerBlocks.LIGHT.get())
        {
            ElementsOfPowerBlocks.LIGHT.get().resetCooldown(cast.level(), blockPos, currentState, density);
        }
    }
}
