package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.network.AddVelocityToPlayer;
import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

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
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vector3d hitVec)
    {
        int force = cast.getDamageForce();

        if ((!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity))
                || !entity.isAlive())
            return;

        applyVelocity(cast, force, hitVec, entity, false);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vector3d hitVec)
    {
        int force = cast.getDamageForce();

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.x - force,
                hitVec.y - force,
                hitVec.z - force,
                hitVec.x + force,
                hitVec.y + force,
                hitVec.z + force);

        List<LivingEntity> living = cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb);
        pushEntities(cast, force, hitVec, living);

        List<ItemEntity> items = cast.world.getEntitiesWithinAABB(ItemEntity.class, aabb);
        pushEntities(cast, force, hitVec, items);

        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vector3d hitVec)
    {
    }

    private void pushEntities(InitializedSpellcast cast, int force, Vector3d hitVec, List<? extends Entity> entities)
    {
        for (Entity e : entities)
        {
            if (!e.isAlive())
                continue;

            applyVelocity(cast, force, hitVec, e, true);
        }
    }

    private void applyVelocity(InitializedSpellcast cast, int force, Vector3d hitVec, Entity e, boolean distanceForce)
    {
        double vx = 0, vy = 0, vz = 0;

        if (e == cast.player && !distanceForce)
        {
            Vector3d look = e.getLookVec();

            vx += force * look.x;
            vy += force * look.y;
            vz += force * look.z;
        }
        else
        {
            double dx = e.getPosX() - hitVec.x;
            double dy = e.getPosY() - hitVec.y;
            double dz = e.getPosZ() - hitVec.z;

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
            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e), new AddVelocityToPlayer(vx, vy, vz));
        }
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, RayTraceResult mop)
    {
        Vector3d hitVec = mop.getHitVec();
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
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK)
        {
            blockPos = blockPos.offset(((BlockRayTraceResult) mop).getFace());
            currentState = cast.world.getBlockState(blockPos);
        }

        Block block = currentState.getBlock();

        if (block == Blocks.FIRE)
        {
            cast.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
        }
        else if (block == Blocks.WATER)
        {
            if (!cast.world.getFluidState(blockPos).isSource())
            {
                cast.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            }
        }
        else if (!currentState.getMaterial().blocksMovement() && !currentState.getMaterial().isLiquid())
        {
            dropBlockAsItem(cast.world, blockPos, currentState);
            cast.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
        }
    }

    private void dropBlockAsItem(World world, BlockPos pos, BlockState state)
    {
        TileEntity tileentity = state.hasTileEntity() ? world.getTileEntity(pos) : null;
        Block.spawnDrops(state, world, pos, tileentity);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }
}
