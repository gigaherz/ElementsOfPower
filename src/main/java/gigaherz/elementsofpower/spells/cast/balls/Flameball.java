package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.spells.SpellBall;
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

public class Flameball extends BallBase
{
    public Flameball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void processDirectHit(Entity e)
    {
        int b0 = getDamageForce();

        if (e instanceof EntityBlaze)
        {
            b0 = 3 + getDamageForce();
        }

        e.attackEntityFrom(DamageSource.causeThrownDamage(projectile, player), (float) b0);
    }

    @Override
    protected void processEntitiesAroundAfter(Vec3 hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - getDamageForce(),
                hitVec.yCoord - getDamageForce(),
                hitVec.zCoord - getDamageForce(),
                hitVec.xCoord + getDamageForce(),
                hitVec.yCoord + getDamageForce(),
                hitVec.zCoord + getDamageForce());

        List<EntityLivingBase> living = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        burnEntities(hitVec, living);

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, aabb);
        burnEntities(hitVec, items);
    }

    private void burnEntities(Vec3 hitVec, List<? extends Entity> living)
    {
        EntityFireball ef = new EntitySmallFireball(world);

        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, getDamageForce() - ll);

            boolean canAttack = e.attackEntityFrom(DamageSource.causeFireballDamage(ef, player), 5.0F);

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
    protected void spawnBallParticles(MovingObjectPosition mop)
    {
        world.spawnParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            world.setBlockState(blockPos, Blocks.fire.getDefaultState());
        }
    }
}
