package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;

import javax.annotation.Nullable;
import java.util.List;

public class FlameEffect extends SpellEffect
{
    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 4;
    }

    @Override
    public int getColor(Spellcast cast)
    {
        return 0x0000ff;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        float damage = (entity instanceof BlazeEntity) ? 3 + cast.getDamageForce() : cast.getDamageForce();

        entity.attackEntityFrom(DamageSource.causeThrownDamage(cast.projectile, cast.player), damage);
        entity.setFire(cast.getDamageForce());
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.x - cast.getDamageForce(),
                hitVec.y - cast.getDamageForce(),
                hitVec.z - cast.getDamageForce(),
                hitVec.x + cast.getDamageForce(),
                hitVec.y + cast.getDamageForce(),
                hitVec.z + cast.getDamageForce());

        burnEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb));
        burnEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(ItemEntity.class, aabb));
    }

    private void burnEntities(Spellcast cast, Vec3d hitVec, List<? extends Entity> living)
    {
        DamagingProjectileEntity ef = EntityType.SMALL_FIREBALL.create(cast.world);

        for (Entity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getPosX() - hitVec.x;
            double dy = e.getPosY() - hitVec.y;
            double dz = e.getPosZ() - hitVec.z;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.getDamageForce() - ll);

            boolean canAttack = e.attackEntityFrom(DamageSource.causeFireballDamage(ef, cast.player), 5.0F);

            if (canAttack)
            {
                if (!e.isImmuneToFire())
                {
                    e.setFire((int) lv);
                }
            }
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK)
        {
            blockPos = blockPos.offset(((BlockRayTraceResult) mop).getFace());
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.AIR)
        {
            cast.world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        Vec3d hitVec = mop.getHitVec();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }
}
