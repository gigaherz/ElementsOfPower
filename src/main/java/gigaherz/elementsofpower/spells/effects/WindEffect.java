package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

        if ((!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity))
                || !entity.isEntityAlive())
            return;

        applyVelocity(cast, force, getHitVec(), entity, false);
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        int force = cast.getDamageForce();

        AxisAlignedBB aabb = new AxisAlignedBB(
                getHitVec().x - force,
                getHitVec().y - force,
                getHitVec().z - force,
                getHitVec().x + force,
                getHitVec().y + force,
                getHitVec().z + force);

        List<LivingEntity> living = cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb);
        pushEntities(cast, force, getHitVec(), living);

        List<ItemEntity> items = cast.world.getEntitiesWithinAABB(ItemEntity.class, aabb);
        pushEntities(cast, force, getHitVec(), items);

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

            applyVelocity(cast, force, getHitVec(), e, true);
        }
    }

    private void applyVelocity(Spellcast cast, int force, Vec3d hitVec, Entity e, boolean distanceForce)
    {
        double vx = 0, vy = 0, vz = 0;

        if (e == cast.player && !distanceForce)
        {
            Vec3d look = e.getLookVec();

            vx += force * look.x;
            vy += force * look.y;
            vz += force * look.z;
        }
        else
        {
            double dx = e.posX - getHitVec().x;
            double dy = e.posY - getHitVec().y;
            double dz = e.posZ - getHitVec().z;

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
        if (e instanceof ServerPlayerEntity)
        {
            ElementsOfPowerMod.channel.sendTo(new AddVelocityPlayer(vx, vy, vz), (ServerPlayerEntity) e);
        }
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        if (cast.getDamageForce() >= 5)
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_HUGE,
                    mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
        }
        else if (cast.getDamageForce() >= 2)
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
        }
        else
        {
            cast.spawnRandomParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
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
