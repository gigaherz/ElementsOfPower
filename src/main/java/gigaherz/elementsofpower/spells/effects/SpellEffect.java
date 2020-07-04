package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

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

    public abstract void processDirectHit(InitializedSpellcast cast, Entity entity, Vector3d hitVec);

    public abstract boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vector3d hitVec);

    public abstract void processEntitiesAroundAfter(InitializedSpellcast cast, Vector3d hitVec);

    public abstract void spawnBallParticles(InitializedSpellcast cast, RayTraceResult mop);

    public void processBlockAtBeamEnd(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, @Nullable RayTraceResult mop)
    {
        processBlockWithinRadius(cast, blockPos, currentState, 0, mop);
    }

    public abstract void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable RayTraceResult mop);

    protected static void causePotionEffect(InitializedSpellcast cast, LivingEntity e, Effect potion, int amplifier, double distance, double durationBase)
    {
        if (potion.isInstant())
        {
            potion.affectEntity(cast.projectile, cast.player, e, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                e.addPotionEffect(new EffectInstance(potion, j, amplifier));
            }
        }
    }
}
