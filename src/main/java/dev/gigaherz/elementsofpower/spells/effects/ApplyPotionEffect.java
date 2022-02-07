package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
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
    private final MobEffect instant;
    private final MobEffect overTime;

    public ApplyPotionEffect(MobEffect instant, MobEffect overTime)
    {
        this.instant = instant;
        this.overTime = overTime;
    }

    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0xA0E0FF;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 8;
    }

    private void applyEffects(InitializedSpellcast cast, Vec3 hitVec, List<? extends LivingEntity> living)
    {
        for (LivingEntity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffects(cast, distance, e);
        }
    }

    private void applyEffects(InitializedSpellcast cast, double distance, LivingEntity e)
    {
        double lv = Math.max(0, cast.getDamageForce() - distance);

        int emp = cast.getEmpowering();

        if (-emp < lv)
            causePotionEffect(cast, e, instant, 0, (lv + emp) * 0.5, 0.0);

        if (emp < lv)
            causePotionEffect(cast, e, overTime, 0, (lv - emp), 100.0);
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec)
    {
        if (entity instanceof LivingEntity)
            applyEffects(cast, 0, (LivingEntity) entity);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec)
    {
        AABB aabb = new AABB(
                hitVec.x - cast.getDamageForce(),
                hitVec.y - cast.getDamageForce(),
                hitVec.z - cast.getDamageForce(),
                hitVec.x + cast.getDamageForce(),
                hitVec.y + cast.getDamageForce(),
                hitVec.z + cast.getDamageForce());

        List<LivingEntity> living = cast.world.getEntitiesOfClass(LivingEntity.class, aabb);
        applyEffects(cast, hitVec, living);
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable HitResult mop)
    {

    }
}
