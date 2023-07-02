package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
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

import javax.annotation.Nullable;
import java.util.List;

public class HarmEffect extends SpellEffect
{
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

    private void witherEntities(InitializedSpellcast cast, Vec3 hitVec, List<? extends LivingEntity> living, Entity directCause)
    {
        for (LivingEntity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffectsToEntity(cast, distance, e, directCause);
        }
    }

    private void applyEffectsToEntity(InitializedSpellcast cast, double distance, LivingEntity e, Entity directCause)
    {
        double lv = Math.max(0, cast.getDamageForce() - distance);

        causePotionEffect(cast, directCause, e, MobEffects.HARM, 0, lv, 0.0);
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
        if (entity instanceof LivingEntity)
            applyEffectsToEntity(cast, 0, (LivingEntity) entity, cast.player);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
        AABB aabb = new AABB(
                hitVec.x - cast.getDamageForce(),
                hitVec.y - cast.getDamageForce(),
                hitVec.z - cast.getDamageForce(),
                hitVec.x + cast.getDamageForce(),
                hitVec.y + cast.getDamageForce(),
                hitVec.z + cast.getDamageForce());

        List<LivingEntity> living = cast.level.getEntitiesOfClass(LivingEntity.class, aabb);
        witherEntities(cast, hitVec, living, directEntity);
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.GRASS_BLOCK)
        {
            cast.level.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
        }
        else if (block == Blocks.DIRT)
        {
            cast.level.setBlockAndUpdate(blockPos, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }
}
