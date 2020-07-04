package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
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
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

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
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vector3d hitVec)
    {
        float damage = (entity instanceof BlazeEntity) ? 3 + cast.getDamageForce() : cast.getDamageForce();

        entity.attackEntityFrom(DamageSource.causeThrownDamage(cast.projectile, cast.player), damage);
        entity.setFire(cast.getDamageForce());
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vector3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vector3d hitVec)
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

    private void burnEntities(InitializedSpellcast cast, Vector3d hitVec, List<? extends Entity> living)
    {
        SmallFireballEntity ef = EntityType.SMALL_FIREBALL.create(cast.world);

        for (Entity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getPosX() - hitVec.x;
            double dy = e.getPosY() - hitVec.y;
            double dz = e.getPosZ() - hitVec.z;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.getDamageForce() - ll);

            boolean canAttack = e.attackEntityFrom(DamageSource.func_233547_a_(ef, cast.player), 5.0F);

            if (canAttack)
            {
                if (!e.func_230279_az_())
                {
                    e.setFire((int) lv);
                }
            }
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK)
        {
            blockPos = blockPos.offset(((BlockRayTraceResult) mop).getFace());
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (currentState.isAir(cast.world, blockPos))
        {
            cast.world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, RayTraceResult mop)
    {
        Vector3d hitVec = mop.getHitVec();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }
}
