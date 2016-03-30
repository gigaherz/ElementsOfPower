package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;

public class WindEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xAAFFFF;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 2;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3 hitVec)
    {
        int force = cast.getDamageForce();

        if ((!(entity instanceof EntityLivingBase) && !(entity instanceof EntityItem))
                || !entity.isEntityAlive())
            return;

        double dx = entity.posX - hitVec.xCoord;
        double dy = entity.posY - hitVec.yCoord;
        double dz = entity.posZ - hitVec.zCoord;

        double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double vx = 0, vy = 0, vz = 0;
        if (ll > 0.0001f)
        {
            vx = dx * force / ll;
            vy = dy * force / ll;
            vz = dz * force / ll;
        }
        entity.addVelocity(vx, vy + force, vz);
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        int force = cast.getDamageForce();

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - force,
                hitVec.yCoord - force,
                hitVec.zCoord - force,
                hitVec.xCoord + force,
                hitVec.yCoord + force,
                hitVec.zCoord + force);

        List<EntityLivingBase> living = cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        pushEntities(force, hitVec, living);

        List<EntityItem> items = cast.world.getEntitiesWithinAABB(EntityItem.class, aabb);
        pushEntities(force, hitVec, items);

        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {
    }

    private void pushEntities(int force, Vec3 hitVec, List<? extends Entity> living)
    {
        for (Entity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, force - ll);

            double vx = 0, vy = 0, vz = 0;
            if (ll > 0.0001f)
            {
                vx = dx * lv / ll;
                vy = dy * lv / ll;
                vz = dz * lv / ll;
            }

            e.addVelocity(vx, vy + lv, vz);
        }
    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        if (cast.getDamageForce() >= 5)
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_HUGE,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
        else if (cast.getDamageForce() >= 2)
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
        else
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, MovingObjectPosition mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.fire)
        {
            cast.world.setBlockToAir(blockPos);
        }
        else if (block == Blocks.flowing_water || block == Blocks.water)
        {
            if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
            {
                cast.world.setBlockToAir(blockPos);
            }
        }
        else if (!block.getMaterial().blocksMovement() && !block.getMaterial().isLiquid())
        {
            block.dropBlockAsItem(cast.world, blockPos, currentState, 0);
            cast.world.setBlockToAir(blockPos);
        }
    }
}
