package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.network.AddVelocityToPlayer;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class WindEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0xAAFFFF;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 2;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
        int force = cast.getDamageForce();

        if ((!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity))
                || !entity.isAlive())
            return;

        applyVelocity(cast, force, hitVec, entity, false);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
        int force = cast.getDamageForce();

        AABB aabb = new AABB(
                hitVec.x - force,
                hitVec.y - force,
                hitVec.z - force,
                hitVec.x + force,
                hitVec.y + force,
                hitVec.z + force);

        List<LivingEntity> living = cast.level.getEntitiesOfClass(LivingEntity.class, aabb);
        pushEntities(cast, force, hitVec, living);

        List<ItemEntity> items = cast.level.getEntitiesOfClass(ItemEntity.class, aabb);
        pushEntities(cast, force, hitVec, items);

        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
    }

    private void pushEntities(InitializedSpellcast cast, int force, Vec3 hitVec, List<? extends Entity> entities)
    {
        for (Entity e : entities)
        {
            if (!e.isAlive())
                continue;

            applyVelocity(cast, force, hitVec, e, true);
        }
    }

    private void applyVelocity(InitializedSpellcast cast, int force, Vec3 hitVec, Entity e, boolean distanceForce)
    {
        double vx = 0, vy = 0, vz = 0;

        if (e == cast.player && !distanceForce)
        {
            Vec3 look = e.getLookAngle();

            vx += force * look.x;
            vy += force * look.y;
            vz += force * look.z;
        }
        else
        {
            double dx = e.getX() - hitVec.x;
            double dy = e.getY() - hitVec.y;
            double dz = e.getZ() - hitVec.z;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = distanceForce ? Math.max(0, force - ll) : force;

            if (lv > 0.0001f)
            {
                vx = dx * ll / lv;
                vy = dy * ll / lv;
                vz = dz * ll / lv;
            }
        }

        e.push(vx, vy, vz);
        if (e instanceof ServerPlayer)
        {
            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new AddVelocityToPlayer(vx, vy, vz));
        }
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        Vec3 hitVec = mop.getLocation();
        if (cast.getDamageForce() >= 5)
        {
            // FIXME: huge
            cast.spawnRandomParticle(ParticleTypes.EXPLOSION, hitVec.x, hitVec.y, hitVec.z);
        }
        else if (cast.getDamageForce() >= 2)
        {
            // FIXME: large
            cast.spawnRandomParticle(ParticleTypes.EXPLOSION, hitVec.x, hitVec.y, hitVec.z);
        }
        else
        {
            // FIXME: normal
            cast.spawnRandomParticle(ParticleTypes.EXPLOSION, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.level.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.FIRE)
        {
            cast.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        }
        else if (block == Blocks.WATER)
        {
            if (!cast.level.getFluidState(blockPos).isSource())
            {
                cast.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }
        else if (!currentState.blocksMotion() && !currentState.liquid())
        {
            dropBlockAsItem(cast.level, blockPos, currentState);
            cast.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        }
    }

    private void dropBlockAsItem(Level world, BlockPos pos, BlockState state)
    {
        BlockEntity tileentity = world.getBlockEntity(pos);
        Block.dropResources(state, world, pos, tileentity);
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
}
