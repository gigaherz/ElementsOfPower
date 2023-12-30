package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class WitherEffect extends SpellEffect
{
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

    private void witherEntities(SpellcastState cast, Vec3 hitVec, List<? extends LivingEntity> living, Entity directEntity)
    {
        for (LivingEntity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffectsToEntity(cast, distance, e, directEntity);
        }
    }

    private void applyEffectsToEntity(SpellcastState cast, double distance, LivingEntity e, Entity directEntity)
    {
        double lv = Math.max(0, cast.damageForce() - distance);

        causePotionEffect(cast, directEntity, e, MobEffects.WITHER, 0, lv, 100.0);
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
        if (entity instanceof LivingEntity)
            applyEffectsToEntity(cast, 0, (LivingEntity) entity, directEntity);
    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        AABB aabb = new AABB(
                hitVec.x - cast.damageForce(),
                hitVec.y - cast.damageForce(),
                hitVec.z - cast.damageForce(),
                hitVec.x + cast.damageForce(),
                hitVec.y + cast.damageForce(),
                hitVec.z + cast.damageForce());

        List<LivingEntity> living = cast.level().getEntitiesOfClass(LivingEntity.class, aabb);
        witherEntities(cast, hitVec, living, directEntity);
    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.GRASS_BLOCK)
        {
            cast.level().setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
        }
        else if (block == Blocks.DIRT)
        {
            cast.level().setBlockAndUpdate(blockPos, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }
}
