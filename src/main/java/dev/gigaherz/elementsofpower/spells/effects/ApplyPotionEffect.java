package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ApplyPotionEffect extends SpellEffect
{
    @Nullable
    private final Holder<MobEffect> instant;
    @Nullable
    private final Holder<MobEffect> overTime;

    public ApplyPotionEffect(@Nullable Holder<MobEffect> instant, @Nullable Holder<MobEffect> overTime)
    {
        this.instant = instant;
        this.overTime = overTime;
    }

    @Override
    public int getColor(SpellcastState cast)
    {
        return 0xA0E0FF;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 8;
    }

    private void applyEffects(SpellcastState cast, Vec3 hitVec, List<? extends LivingEntity> living, Entity directSource)
    {
        for (LivingEntity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffects(cast, distance, e, directSource);
        }
    }

    private void applyEffects(SpellcastState cast, double distance, LivingEntity e, Entity directSource)
    {
        double lv = Math.max(0, cast.damageForce() - distance);

        int emp = cast.empowering();

        if (-emp < lv && instant != null)
            causePotionEffect(cast, directSource, e, instant, 0, (lv + emp) * 0.5, 0.0);

        if (emp < lv && overTime != null)
            causePotionEffect(cast, directSource, e, overTime, 0, (lv - emp), 100.0);
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
        if (entity instanceof LivingEntity)
            applyEffects(cast, 0, (LivingEntity) entity, cast.player());
    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        int force = cast.damageForce();

        AABB aabb = new AABB(
                hitVec.x - force,
                hitVec.y - force,
                hitVec.z - force,
                hitVec.x + force,
                hitVec.y + force,
                hitVec.z + force);

        List<LivingEntity> living = cast.level().getEntitiesOfClass(LivingEntity.class, aabb);
        applyEffects(cast, hitVec, living, directEntity);
    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {
    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable HitResult mop)
    {

    }
}
