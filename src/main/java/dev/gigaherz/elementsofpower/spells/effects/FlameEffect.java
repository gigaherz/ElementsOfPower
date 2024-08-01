package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FlameEffect extends SpellEffect
{
    @Override
    public int getDuration(SpellcastState cast)
    {
        return 20 * cast.damageForce();
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 4;
    }

    @Override
    public int getColor(SpellcastState cast)
    {
        return 0x0000ff;
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
        float damage = (entity instanceof Blaze) ? 3 + cast.damageForce() : cast.damageForce();

        entity.hurt(entity.damageSources().thrown(cast.player(), cast.player()), damage);
        entity.setRemainingFireTicks(cast.damageForce());
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

        burnEntities(cast, hitVec, cast.level().getEntitiesOfClass(LivingEntity.class, aabb));
        burnEntities(cast, hitVec, cast.level().getEntitiesOfClass(ItemEntity.class, aabb));
    }

    private void burnEntities(SpellcastState cast, Vec3 hitVec, List<? extends Entity> living)
    {
        SmallFireball ef = EntityType.SMALL_FIREBALL.create(cast.level());

        for (Entity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.damageForce() - ll);

            boolean canAttack = e.hurt(e.damageSources().fireball(ef, cast.player()), 5.0F);

            if (canAttack)
            {
                if (!e.fireImmune())
                {
                    e.setRemainingFireTicks((int) lv);
                }
            }
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

        if (currentState.isAir())
        {
            cast.level().setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
        }
    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }
}
