package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class SpellEffect
{
    public abstract int getColor(InitializedSpellcast cast);

    public abstract int getDuration(InitializedSpellcast cast);

    public abstract int getInterval(InitializedSpellcast cast);

    public int getForceModifier(InitializedSpellcast cast)
    {
        return 0;
    }

    public abstract void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec);

    public abstract boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec);

    public abstract void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec);

    public abstract void spawnBallParticles(InitializedSpellcast cast, HitResult mop);

    public void processBlockAtBeamEnd(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, @Nullable HitResult mop)
    {
        processBlockWithinRadius(cast, blockPos, currentState, 0, mop);
    }

    public abstract void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable HitResult mop);

    protected static void causePotionEffect(InitializedSpellcast cast, LivingEntity e, MobEffect potion, int amplifier, double distance, double durationBase)
    {
        if (potion.isInstantenous())
        {
            potion.applyInstantenousEffect(cast.getProjectile(), cast.player, e, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                e.addEffect(new MobEffectInstance(potion, j, amplifier));
            }
        }
    }
}
