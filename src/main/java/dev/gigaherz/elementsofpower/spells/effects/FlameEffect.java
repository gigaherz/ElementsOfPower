package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class FlameEffect extends SpellEffect
{
    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 4;
    }

    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x0000ff;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec)
    {
        float damage = (entity instanceof Blaze) ? 3 + cast.getDamageForce() : cast.getDamageForce();

        entity.hurt(DamageSource.thrown(cast.projectile, cast.player), damage);
        entity.setSecondsOnFire(cast.getDamageForce());
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

        burnEntities(cast, hitVec, cast.world.getEntitiesOfClass(LivingEntity.class, aabb));
        burnEntities(cast, hitVec, cast.world.getEntitiesOfClass(ItemEntity.class, aabb));
    }

    private void burnEntities(InitializedSpellcast cast, Vec3 hitVec, List<? extends Entity> living)
    {
        SmallFireball ef = EntityType.SMALL_FIREBALL.create(cast.world);

        for (Entity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.getDamageForce() - ll);

            boolean canAttack = e.hurt(DamageSource.fireball(ef, cast.player), 5.0F);

            if (canAttack)
            {
                if (!e.fireImmune())
                {
                    e.setSecondsOnFire((int) lv);
                }
            }
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.world.getBlockState(blockPos);
        }

        if (currentState.isAir())
        {
            cast.world.setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
        }
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }
}
