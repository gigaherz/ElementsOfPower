package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public abstract class SpellEffect
{
    public abstract int getColor(SpellcastState cast);

    public abstract int getDuration(SpellcastState cast);

    public abstract int getInterval(SpellcastState cast);

    public int getForceModifier(SpellcastState cast)
    {
        return 0;
    }

    public abstract void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity);

    public abstract boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity);

    public abstract void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity);

    public abstract void spawnBallParticles(SpellcastState cast, HitResult mop);

    public void processBlockAtBeamEnd(SpellcastState cast, BlockPos blockPos, BlockState currentState, @Nullable HitResult mop)
    {
        processBlockWithinRadius(cast, blockPos, currentState, 0, mop);
    }

    public abstract void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable HitResult mop);

    protected static void causePotionEffect(SpellcastState cast, Entity directEntity, LivingEntity target, MobEffect potion, int amplifier, double distance, double durationBase)
    {
        if (potion.isInstantenous())
        {
            potion.applyInstantenousEffect(directEntity, cast.player(), target, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                target.addEffect(new MobEffectInstance(potion, j, amplifier));
            }
        }
    }
}
