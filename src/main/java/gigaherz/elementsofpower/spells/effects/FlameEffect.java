package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

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
                getHitVec().x - cast.getDamageForce(),
                getHitVec().y - cast.getDamageForce(),
                getHitVec().z - cast.getDamageForce(),
                getHitVec().x + cast.getDamageForce(),
                getHitVec().y + cast.getDamageForce(),
                getHitVec().z + cast.getDamageForce());

        burnEntities(cast, getHitVec(), cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb));
        burnEntities(cast, getHitVec(), cast.world.getEntitiesWithinAABB(ItemEntity.class, aabb));
    }

    private void burnEntities(Spellcast cast, Vec3d hitVec, List<? extends Entity> living)
    {
        DamagingProjectileEntity ef = new SmallFireballEntity(cast.world);

        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - getHitVec().x;
            double dy = e.posY - getHitVec().y;
            double dz = e.posZ - getHitVec().z;

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
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
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
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
    }
}
