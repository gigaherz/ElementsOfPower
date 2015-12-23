package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.spells.SpellBall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;

public class Airball extends BallBase
{
    public Airball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void processEntitiesAroundBefore(Vec3 hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - getDamageForce(),
                hitVec.yCoord - getDamageForce(),
                hitVec.zCoord - getDamageForce(),
                hitVec.xCoord + getDamageForce(),
                hitVec.yCoord + getDamageForce(),
                hitVec.zCoord + getDamageForce());

        List<EntityLivingBase> living = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        pushEntities(hitVec, living);

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, aabb);
        pushEntities(hitVec, items);
    }

    private void pushEntities(Vec3 hitVec, List<? extends Entity> living)
    {
        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (ll < 0.0001f)
                continue;

            double lv = Math.max(0, getDamageForce() - ll);

            double vx = dx * lv / ll;
            double vy = dy * lv / ll;
            double vz = dz * lv / ll;
            e.addVelocity(vx, vy, vz);
        }
    }

    @Override
    protected void spawnBallParticles(MovingObjectPosition mop)
    {
        if (getDamageForce() >= 5)
        {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
        else if (getDamageForce() >= 2)
        {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
        else
        {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.fire)
        {
            world.setBlockToAir(blockPos);
        }
        else if (block == Blocks.flowing_water || block == Blocks.water)
        {
            if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
            {
                world.setBlockToAir(blockPos);
            }
        }
        else if (!block.getMaterial().blocksMovement() && !block.getMaterial().isLiquid())
        {
            block.dropBlockAsItem(world, blockPos, currentState, 0);
            world.setBlockToAir(blockPos);
        }
    }
}
