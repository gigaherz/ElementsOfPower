package gigaherz.elementsofpower.spells.cast.effects;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;

public class FireEffect extends SpellEffect
{
    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 20 * cast.getDamageForce();
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 4;
    }

    @Override
    public int getColor(Spellcast cast)
    {
        return 0x0000ff;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {
        float damage = (e instanceof EntityBlaze) ? 3 + cast.getDamageForce() : cast.getDamageForce();

        e.attackEntityFrom(DamageSource.causeThrownDamage(cast.projectile, cast.player), damage);

        e.setFire(cast.getDamageForce());
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - cast.getDamageForce(),
                hitVec.yCoord - cast.getDamageForce(),
                hitVec.zCoord - cast.getDamageForce(),
                hitVec.xCoord + cast.getDamageForce(),
                hitVec.yCoord + cast.getDamageForce(),
                hitVec.zCoord + cast.getDamageForce());

        burnEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb));
        burnEntities(cast, hitVec, cast.world.getEntitiesWithinAABB(EntityItem.class, aabb));
    }

    private void burnEntities(Spellcast cast, Vec3 hitVec, List<? extends Entity> living)
    {
        EntityFireball ef = new EntitySmallFireball(cast.world);

        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

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
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            cast.world.setBlockState(blockPos, Blocks.fire.getDefaultState());
        }
    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
    }
}
