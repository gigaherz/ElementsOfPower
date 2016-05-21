package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
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
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        int force = cast.getDamageForce();

        if ((!(entity instanceof EntityLivingBase) && !(entity instanceof EntityItem))
                || !entity.isEntityAlive())
            return;

        applyVelocity(cast, force, hitVec, entity, false);
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
        pushEntities(cast, force, hitVec, living);

        List<EntityItem> items = cast.world.getEntitiesWithinAABB(EntityItem.class, aabb);
        pushEntities(cast, force, hitVec, items);

        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
    }

    private void pushEntities(Spellcast cast, int force, Vec3d hitVec, List<? extends Entity> entities)
    {
        for (Entity e : entities)
        {
            if (!e.isEntityAlive())
                continue;

            applyVelocity(cast, force, hitVec, e, true);
        }
    }

    private void applyVelocity(Spellcast cast, int force, Vec3d hitVec, Entity e, boolean distanceForce)
    {
        double vx = 0, vy = 0, vz = 0;

        if (e == cast.player && !distanceForce)
        {
            Vec3d look = e.getLookVec();

            vx += force * look.xCoord;
            vy += force * look.yCoord;
            vz += force * look.zCoord;
        }
        else
        {
            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = distanceForce ? Math.max(0, force - ll) : force;

            if (lv > 0.0001f)
            {
                vx = dx * ll / lv;
                vy = dy * ll / lv;
                vz = dz * ll / lv;
            }
        }

        e.addVelocity(vx, vy, vz);
        if (e instanceof EntityPlayerMP)
        {
            ElementsOfPower.channel.sendTo(new AddVelocityPlayer(vx, vy, vz), (EntityPlayerMP) e);
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
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.FIRE)
        {
            cast.world.setBlockToAir(blockPos);
        }
        else if (block == Blocks.FLOWING_WATER || block == Blocks.WATER)
        {
            if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
            {
                cast.world.setBlockToAir(blockPos);
            }
        }
        else if (!currentState.getMaterial().blocksMovement() && !currentState.getMaterial().isLiquid())
        {
            block.dropBlockAsItem(cast.world, blockPos, currentState, 0);
            cast.world.setBlockToAir(blockPos);
        }
    }
}
