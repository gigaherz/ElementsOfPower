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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

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
    public void processDirectHit(Spellcast cast, Entity e)
    {
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
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
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
    }

    private void pushEntities(int force, Vec3d hitVec, List<? extends Entity> living)
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

            double lv = Math.max(0, force - ll);

            double vx = dx * lv / ll;
            double vy = dy * lv / ll;
            double vz = dz * lv / ll;
            e.addVelocity(vx, vy, vz);
        }
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
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
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, RayTraceResult mop)
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
        else if (!block.getMaterial(currentState).blocksMovement() && !block.getMaterial(currentState).isLiquid())
        {
            block.dropBlockAsItem(cast.world, blockPos, currentState, 0);
            cast.world.setBlockToAir(blockPos);
        }
    }
}
